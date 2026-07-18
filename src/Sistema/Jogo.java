package Sistema;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import Entidades.*;
import Entidades.Personagens.Dinossauros.Compsognato;
import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.Item;
import Itens.Consumiveis.KitMedico;
import Itens.Consumiveis.Consumivel;
import Util.*;

public class Jogo {

    public final Random random;
    private final Menu menu;
    private final Tabuleiro tabuleiro;
    private Jogador jogador;
    private final SistemaCombate sistemaCombate;
    private final SistemaItens sistemaItens;
    private final SistemaSalvamento sistemaSalvamento;
    private final LeitorDeInput leitorDeInput;
    private final CarregadorMapa carregadorMapa;
    private final ArrayList<Dinossauro> dinossauros;
    private final ArrayList<Caixa> caixas;
    private EstadoJogo estado = EstadoJogo.NOVO_JOGO;
    private Jogador jogadorInicial;
    private ArrayList<Dinossauro> dinossaurosIniciais;
    private ArrayList<Caixa> caixasIniciais;
    private boolean debugMode = false;
    private final boolean modoGui;
    private InterfaceGui interfaceGui;
    private final ArrayList<ThreadDinossauro> threadsDinos = new ArrayList<>();
    private boolean pausado = false; // true durante combate / diálogos
    private long ultimoMovimentoJogador = 0;
    private static final long INTERVALO_JOGADOR_MS = 350; // evita atravessar o mapa segurando a tecla

    public Jogo() {
        this(new Menu(), new LeitorDeInput(new Scanner(System.in)), false);
    }

    public Jogo(Menu menu, LeitorDeInput leitor, boolean modoGui) {
        this.menu = menu;
        this.leitorDeInput = leitor;
        this.modoGui = modoGui;
        random = new Random();
        carregadorMapa = new CarregadorMapa(random);
        sistemaSalvamento = new SistemaSalvamento(random);
        tabuleiro = new Tabuleiro(Macros.TAMANHO_TABULEIRO);
        sistemaCombate = new SistemaCombate(random);
        sistemaItens = new SistemaItens();
        jogador = new Jogador(Macros.SIMB_JOGADOR, Macros.SAUDE_JOGADOR, Macros.PERCEPCAO_INICIAL);
        dinossauros = new ArrayList<>();
        caixas = new ArrayList<>();
    }

    public void setInterfaceGui(InterfaceGui interfaceGui) {
        this.interfaceGui = interfaceGui;
    }

    public EstadoJogo getEstado() {
        return estado;
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public SistemaCombate getSistemaCombate() {
        return sistemaCombate;
    }

    public void iniciarNovoJogo(int dificuldade, int numeroMapa) {
        pararThreadsDinossauros();
        jogador = new Jogador(Macros.SIMB_JOGADOR, Macros.SAUDE_JOGADOR, Macros.PERCEPCAO_INICIAL);
        jogador.setPercepcao(4 - dificuldade);
        dinossauros.clear();
        caixas.clear();
        tabuleiro.limpar();
        carregadorMapa.carregar(Macros.PASTA_MAPAS + "mapa" + numeroMapa + ".txt",
                tabuleiro, jogador, dinossauros, caixas);
        finalizarCarregamentoPartida();
        debugMode = false;
        estado = EstadoJogo.CONTINUAR;
        pausado = false;
        if (modoGui) iniciarThreadsDinossauros();
        if (interfaceGui != null) interfaceGui.atualizar();
    }

    public void carregarPartida() {
        pararThreadsDinossauros();
        carregarSave();
        pausado = false;
        if (modoGui) iniciarThreadsDinossauros();
        if (interfaceGui != null) interfaceGui.atualizar();
    }

    public void salvarPartida() {
        salvarJogo();
    }

    public void alternarDebug() {
        debugMode = !debugMode;
        menu.mensagem(debugMode ? "MODO DEBUG ATIVADO" : "MODO DEBUG DESATIVADO");
        if (interfaceGui != null) interfaceGui.atualizar();
    }

    public void usarKitMedico() {
        Item kit = jogador.pegarItem(KitMedico.class);
        if (kit == null) {
            menu.mensagem("Você não tem kits médicos.");
            return;
        }
        kit.usar(jogador, null);
        if (kit instanceof Consumivel && ((Consumivel) kit).consumidoAposUso()) {
            jogador.removerItem(kit);
        }
        if (interfaceGui != null) interfaceGui.atualizar();
    }

    // movimento do jogador (chamado pelas ThreadJogador)
    public void executarMovimento(Direcao direcao) {
        Dinossauro dinoCombate = null;
        boolean dinoAtacouPrimeiro = false;
        Direcao dirCombate = null;

        synchronized (this) {
            if (estado != EstadoJogo.CONTINUAR || pausado) return;

            long agora = System.currentTimeMillis();
            if (agora - ultimoMovimentoJogador < INTERVALO_JOGADOR_MS) return;
            ultimoMovimentoJogador = agora;

            if (dinossauros.isEmpty()) {
                menu.mensagemVitoria();
                pausado = true;
                pararThreadsDinossauros();
                if (interfaceGui != null) interfaceGui.onVitoria();
                return;
            }

            ResultadoMovimento resMovimento = jogador.mover(direcao, tabuleiro);
            menu.avisoMovimento(resMovimento);

            if (resMovimento == ResultadoMovimento.BLOQUEADO) {
                if (interfaceGui != null) interfaceGui.atualizar();
                return;
            }

            if (resMovimento == ResultadoMovimento.ENCONTROU_CAIXA) {
                Compsognato surpresa = sistemaItens.processarCaixaNaPosicao(jogador, caixas);
                tabuleiro.atualizar(jogador, dinossauros, caixas);
                if (surpresa != null) {
                    menu.mensagem("Cuidado! Um compsognato estava atrás da caixa!");
                    dinossauros.add(surpresa);
                    tabuleiro.atualizar(jogador, dinossauros, caixas);
                    dinoCombate = surpresa;
                    dinoAtacouPrimeiro = true;
                    pausado = true;
                } else {
                    menu.mensagem("Você encontrou um item na caixa!");
                }
            } else if (resMovimento != ResultadoMovimento.LIVRE) {
                for (Dinossauro d : dinossauros) {
                    if (d.getPosicaoX() == jogador.getPosicaoX() + direcao.dx
                            && d.getPosicaoY() == jogador.getPosicaoY() + direcao.dy) {
                        dinoCombate = d;
                        dirCombate = direcao;
                        pausado = true;
                        break;
                    }
                }
            }

            tabuleiro.atualizar(jogador, dinossauros, caixas);

            if (!modoGui && dinoCombate == null) {
                moverDinossauros();
            }

            if (interfaceGui != null) interfaceGui.atualizar();
        }

        // combate fora do synchronized para não travar outras threads
        if (dinoCombate != null) {
            ResultadoCombate res = processarResultadoCombate(dinoCombate, dinoAtacouPrimeiro, dirCombate);
            synchronized (this) {
                if (res == ResultadoCombate.VENCEU && dirCombate != null) {
                    tabuleiro.atualizar(jogador, dinossauros, caixas);
                    jogador.mover(dirCombate, tabuleiro);
                }
                // se fugiu / sobreviveu o dino da caixa, inicia a thread dele
                if (res != ResultadoCombate.VENCEU && res != ResultadoCombate.PERDEU
                        && dinossauros.contains(dinoCombate)) {
                    iniciarThreadDinossauro(dinoCombate);
                }
                tabuleiro.atualizar(jogador, dinossauros, caixas);
                if (res != ResultadoCombate.PERDEU && estado == EstadoJogo.CONTINUAR) {
                    pausado = false;
                }
                if (dinossauros.isEmpty() && estado == EstadoJogo.CONTINUAR) {
                    menu.mensagemVitoria();
                    pausado = true;
                    pararThreadsDinossauros();
                    if (interfaceGui != null) interfaceGui.onVitoria();
                }
                if (interfaceGui != null) interfaceGui.atualizar();
            }
        }
    }

    // chamado pela ThreadDinossauro
    public void moverUmDinossauro(Dinossauro d) {
        boolean encontrouJogador = false;

        synchronized (this) {
            if (estado != EstadoJogo.CONTINUAR || pausado) return;
            if (!dinossauros.contains(d) || !d.estaVivo()) return;

            encontrouJogador = d.mover(jogador, tabuleiro, random);
            tabuleiro.atualizar(jogador, dinossauros, caixas);

            if (encontrouJogador) {
                pausado = true;
                menu.avisoDinossauroEncontrou(d);
            }

            if (interfaceGui != null) interfaceGui.atualizar();
        }

        if (encontrouJogador) {
            processarResultadoCombate(d, true, null);
            synchronized (this) {
                tabuleiro.atualizar(jogador, dinossauros, caixas);
                if (jogador.estaVivo() && estado == EstadoJogo.CONTINUAR) {
                    pausado = false;
                }
                if (dinossauros.isEmpty() && estado == EstadoJogo.CONTINUAR) {
                    menu.mensagemVitoria();
                    pausado = true;
                    pararThreadsDinossauros();
                    if (interfaceGui != null) interfaceGui.onVitoria();
                }
                if (interfaceGui != null) interfaceGui.atualizar();
            }
        }
    }

    public void reiniciarPartida() {
        pararThreadsDinossauros();
        reiniciarPartidaInterno();
        pausado = false;
        if (modoGui) iniciarThreadsDinossauros();
        if (interfaceGui != null) interfaceGui.atualizar();
    }

    public void iniciarThreadsDinossauros() {
        pararThreadsDinossauros();
        for (Dinossauro d : dinossauros) {
            // T-Rex tem velocidade 0 e não se move
            if (d.getVelocidade() <= 0) continue;
            ThreadDinossauro t = new ThreadDinossauro(this, d);
            threadsDinos.add(t);
            t.start();
        }
    }

    public void pararThreadsDinossauros() {
        for (ThreadDinossauro t : threadsDinos) {
            t.parar();
        }
        threadsDinos.clear();
    }

    // inicia thread de um dinossaur que surgiu no meio do jogo (compsognato)
    private void iniciarThreadDinossauro(Dinossauro d) {
        if (!modoGui || d.getVelocidade() <= 0) return;
        for (ThreadDinossauro t : threadsDinos) {
            if (t.getDinossauro() == d) return; // já tem thread
        }
        ThreadDinossauro t = new ThreadDinossauro(this, d);
        threadsDinos.add(t);
        t.start();
    }

    private void setDificuldade() {
        int dificuldade = leitorDeInput.lerInput(1, 3);
        jogador.setPercepcao(4 - dificuldade);
    }

    private void carregarMapaEscolhido() {
        menu.escolherMapa();
        int numeroMapa = leitorDeInput.lerInput(1, Macros.NUM_MAPAS);
        String caminho = Macros.PASTA_MAPAS + "mapa" + numeroMapa + ".txt";
        carregadorMapa.carregar(caminho, tabuleiro, jogador, dinossauros, caixas);
        finalizarCarregamentoPartida();
    }

    private void carregarSave() {
        if (!sistemaSalvamento.existeSave(Macros.ARQUIVO_SAVE)) {
            menu.mensagemSaveAusente();
            return;
        }
        sistemaSalvamento.carregar(Macros.ARQUIVO_SAVE, tabuleiro, jogador, dinossauros, caixas);
        finalizarCarregamentoPartida();
        menu.mensagemCarregado();
    }

    private void salvarJogo() {
        sistemaSalvamento.salvar(Macros.ARQUIVO_SAVE, jogador, dinossauros, caixas, tabuleiro);
        menu.mensagemSalvo();
    }

    private void finalizarCarregamentoPartida() {
        tabuleiro.atualizar(jogador, dinossauros, caixas);
        tabuleiro.salvarPosicoes();
        salvarEstadoInicial();
    }

    public void iniciarJogo() {
        if (estado == EstadoJogo.REINICIAR) {
            reiniciarPartidaInterno();
        } else {
            boolean partidaPronta = false;
            while (!partidaPronta) {
                menu.menuInicial();
                int inputOpcoes = leitorDeInput.lerInput(1, 3);
                if (inputOpcoes == 1) {
                    menu.escolherDificuldade();
                    setDificuldade();
                    carregarMapaEscolhido();
                    partidaPronta = true;
                } else if (inputOpcoes == 2) {
                    if (!sistemaSalvamento.existeSave(Macros.ARQUIVO_SAVE)) {
                        menu.mensagemSaveAusente();
                        continue;
                    }
                    carregarSave();
                    partidaPronta = true;
                } else {
                    estado = EstadoJogo.SAIR;
                    return;
                }
            }
        }

        debugMode = false;
        estado = EstadoJogo.CONTINUAR;
        loopJogo();
    }

    private void loopJogo() {
        while (estado == EstadoJogo.CONTINUAR) {
            menu.menuPrincipal();
            int inputOpcoes = leitorDeInput.lerInput(1, 5);

            if (inputOpcoes == 1) loopMovimento();
            else if (inputOpcoes == 2) usarKitMedico();
            else if (inputOpcoes == 3) {
                debugMode = true;
                menu.mensagem("MODO DEBUG ATIVADO");
            }
            else if (inputOpcoes == 4) salvarJogo();
            else if (inputOpcoes == 5) sairDoJogo();
        }
    }

    private void sairDoJogo() {
        menu.menuSaida();
        int inputOpcoes = leitorDeInput.lerInput(1, 3);

        if (inputOpcoes == 1) estado = EstadoJogo.REINICIAR;
        else if (inputOpcoes == 2) estado = EstadoJogo.NOVO_JOGO;
        else {
            menu.mensagemSaida();
            estado = EstadoJogo.SAIR;
        }
    }

    private void salvarEstadoInicial() {
        jogadorInicial = jogador.copia();

        dinossaurosIniciais = new ArrayList<>();
        for (Dinossauro d : dinossauros) dinossaurosIniciais.add(d.copia());

        caixasIniciais = new ArrayList<>();
        for (Caixa c : caixas) caixasIniciais.add(c.copia());
    }

    private void reiniciarPartidaInterno() {
        jogador = jogadorInicial.copia();
        dinossauros.clear();

        for (Dinossauro d : dinossaurosIniciais) dinossauros.add(d.copia());
        caixas.clear();

        for (Caixa c : caixasIniciais) caixas.add(c.copia());
        tabuleiro.setGrid(tabuleiro.getPosicoesIniciais());
        tabuleiro.atualizar(jogador, dinossauros, caixas);
        debugMode = false;
        estado = EstadoJogo.CONTINUAR;
    }

    private void loopMovimento() {
        int inputMovimento = 0;
        while (inputMovimento != 5 && estado == EstadoJogo.CONTINUAR) {
            if (dinossauros.isEmpty()) {
                menu.mensagemVitoria();
                sairDoJogo();
                return;
            }

            menu.statusJogador(jogador);
            if (debugMode) menu.mostrarTabuleiroDEBUG(tabuleiro);
            else menu.mostrarTabuleiroJogador(tabuleiro, jogador);
            menu.opcoesMovimento();

            inputMovimento = leitorDeInput.lerInput(1, 5);
            if (inputMovimento == 5) continue;

            executarMovimento(leitorDeInput.lerDirecao(inputMovimento));
            if (estado != EstadoJogo.CONTINUAR) return;
        }
    }

    private void processarResultadoMovimento(ResultadoMovimento resMovimento, Direcao direcao) {
        if (resMovimento == ResultadoMovimento.ENCONTROU_CAIXA) {
            processarCaixa();
        } else if (resMovimento != ResultadoMovimento.LIVRE &&
                resMovimento != ResultadoMovimento.BLOQUEADO) {
            processarCombate(direcao);
        }
    }

    private void processarCaixa() {
        Compsognato surpresa = sistemaItens.processarCaixaNaPosicao(jogador, caixas);
        tabuleiro.atualizar(jogador, dinossauros, caixas);
        if (surpresa != null) {
            dinossauros.add(surpresa);
            tabuleiro.atualizar(jogador, dinossauros, caixas);
            processarResultadoCombate(surpresa, true, null);
            if (dinossauros.contains(surpresa)) {
                iniciarThreadDinossauro(surpresa);
            }
        }
    }

    private void processarCombate(Direcao direcao) {
        for (Dinossauro d : dinossauros) {
            if (d.getPosicaoX() != jogador.getPosicaoX() + direcao.dx ||
                    d.getPosicaoY() != jogador.getPosicaoY() + direcao.dy) continue;

            ResultadoCombate res = processarResultadoCombate(d, false, direcao);
            if (res == ResultadoCombate.VENCEU) {
                tabuleiro.atualizar(jogador, dinossauros, caixas);
                jogador.mover(direcao, tabuleiro);
            }
            return;
        }
    }

    private ResultadoCombate processarResultadoCombate(Dinossauro d, boolean dinoAtacouPrimeiro, Direcao direcao) {
        if (dinoAtacouPrimeiro) {
            boolean desviou = sistemaCombate.passouTestePercepcao(jogador);
            if (!desviou) {
                jogador.setSaude(jogador.getSaude() - d.getDanoAtaque());
                menu.mensagem("O dinossauro te atacou! -" + d.getDanoAtaque() + " de saúde.");
                if (jogador.getSaude() <= 0) {
                    tratarDerrota();
                    return ResultadoCombate.PERDEU;
                }
            } else {
                menu.mensagem("Você desviou do ataque inicial!");
            }
        }

        if (interfaceGui != null) interfaceGui.prepararCombate(jogador, d);
        ResultadoCombate res = sistemaCombate.combate(jogador, d, menu, leitorDeInput, tabuleiro);
        if (interfaceGui != null) interfaceGui.finalizarCombate();

        if (res == ResultadoCombate.PERDEU) {
            tratarDerrota();
        }
        if (res == ResultadoCombate.VENCEU) {
            synchronized (this) {
                dinossauros.remove(d);
                pararThreadDoDino(d);
            }
        }
        if (res == ResultadoCombate.FUGIU) {
            // garante que jogador e dino não ficam na mesma célula
            separarAposFuga(d);
        }
        return res;
    }

    // se ainda estão juntos após fugir, empurra o dino para o lado
    private void separarAposFuga(Dinossauro d) {
        if (d.getPosicaoX() != jogador.getPosicaoX() || d.getPosicaoY() != jogador.getPosicaoY()) {
            return;
        }
        Direcao[] dirs = {Direcao.CIMA, Direcao.BAIXO, Direcao.ESQUERDA, Direcao.DIREITA};
        for (Direcao dir : dirs) {
            int nx = d.getPosicaoX() + dir.dx;
            int ny = d.getPosicaoY() + dir.dy;
            if (d.verificaMovimento(nx, ny, tabuleiro) == ResultadoMovimento.LIVRE) {
                d.setPosicaoX(nx);
                d.setPosicaoY(ny);
                return;
            }
        }
    }

    private void pararThreadDoDino(Dinossauro d) {
        for (int i = 0; i < threadsDinos.size(); i++) {
            if (threadsDinos.get(i).getDinossauro() == d) {
                threadsDinos.get(i).parar();
                threadsDinos.remove(i);
                break;
            }
        }
    }

    private void tratarDerrota() {
        menu.mensagemDerrota();
        pararThreadsDinossauros();
        pausado = true;
        if (modoGui && interfaceGui != null) {
            interfaceGui.onDerrota();
        } else {
            sairDoJogo();
        }
    }

    private void moverDinossauros() {
        for (Dinossauro d : new ArrayList<>(dinossauros)) {
            boolean encontrouJogador = d.mover(jogador, tabuleiro, random);
            tabuleiro.atualizar(jogador, dinossauros, caixas);

            if (encontrouJogador) {
                menu.avisoDinossauroEncontrou(d);
                processarResultadoCombate(d, true, null);
                if (!modoGui && estado != EstadoJogo.CONTINUAR) return;
            }
        }
    }
}
