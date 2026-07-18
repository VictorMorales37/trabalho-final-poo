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

/**
 * Coordena a partida: movimento, encontros e fim de jogo.
 * Combate fica em SistemaCombate; threads em GerenciadorThreads; GUI na pasta Gui.
 */
public class Jogo {

    private static final long INTERVALO_JOGADOR_MS = 350;

    public final Random random;
    private final Menu menu;
    private final Tabuleiro tabuleiro;
    private Jogador jogador;
    private final SistemaCombate sistemaCombate;
    private final SistemaItens sistemaItens;
    private final SistemaSalvamento sistemaSalvamento;
    private final LeitorDeInput leitorDeInput;
    private final CarregadorMapa carregadorMapa;
    private final GerenciadorThreads gerenciadorThreads;
    private final ArrayList<Dinossauro> dinossauros;
    private final ArrayList<Caixa> caixas;
    private final boolean modoGui;

    private EstadoJogo estado = EstadoJogo.NOVO_JOGO;
    private boolean pausado = false;
    private boolean debugMode = false;
    private long ultimoMovimentoJogador = 0;

    private Jogador jogadorInicial;
    private ArrayList<Dinossauro> dinossaurosIniciais;
    private ArrayList<Caixa> caixasIniciais;
    private InterfaceGui interfaceGui;

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
        gerenciadorThreads = new GerenciadorThreads(this);
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
        gerenciadorThreads.pararTodas();
        jogador = new Jogador(Macros.SIMB_JOGADOR, Macros.SAUDE_JOGADOR, Macros.PERCEPCAO_INICIAL);
        jogador.setPercepcao(4 - dificuldade);
        dinossauros.clear();
        caixas.clear();
        tabuleiro.limpar();
        carregadorMapa.carregar(Macros.PASTA_MAPAS + "mapa" + numeroMapa + ".txt",
                tabuleiro, jogador, dinossauros, caixas);
        finalizarCarregamentoPartida();
        resetarEstadoPartida();
        if (modoGui) gerenciadorThreads.iniciarTodas(dinossauros);
        atualizarGui();
    }

    public void carregarPartida() {
        gerenciadorThreads.pararTodas();
        carregarSave();
        resetarEstadoPartida();
        if (modoGui) gerenciadorThreads.iniciarTodas(dinossauros);
        atualizarGui();
    }

    public void salvarPartida() {
        sistemaSalvamento.salvar(Macros.ARQUIVO_SAVE, jogador, dinossauros, caixas, tabuleiro);
        menu.mensagemSalvo();
    }

    public void alternarDebug() {
        debugMode = !debugMode;
        menu.mensagem(debugMode ? "MODO DEBUG ATIVADO" : "MODO DEBUG DESATIVADO");
        atualizarGui();
    }

    public void usarKitMedico() {
        Item kit = jogador.pegarItem(KitMedico.class);
        if (kit == null) return;
        kit.usar(jogador, null);
        if (kit instanceof Consumivel && ((Consumivel) kit).consumidoAposUso()) {
            jogador.removerItem(kit);
        }
        atualizarGui();
    }

    public void executarMovimento(Direcao direcao) {
        Dinossauro dinoCombate = null;
        boolean dinoAtacouPrimeiro = false;
        Direcao dirCombate = null;

        synchronized (this) {
            if (!podeJogar()) return;
            if (!cooldownPronto()) return;
            ultimoMovimentoJogador = System.currentTimeMillis();

            if (dinossauros.isEmpty()) {
                tratarVitoria();
                return;
            }

            ResultadoMovimento res = jogador.mover(direcao, tabuleiro);
            menu.avisoMovimento(res);

            if (res == ResultadoMovimento.BLOQUEADO) {
                atualizarGui();
                return;
            }

            if (res == ResultadoMovimento.ENCONTROU_CAIXA) {
                Compsognato surpresa = abrirCaixa();
                if (surpresa != null) {
                    dinoCombate = surpresa;
                    dinoAtacouPrimeiro = true;
                    pausado = true;
                }
            } else if (res != ResultadoMovimento.LIVRE) {
                Dinossauro d = acharDinoNaDirecao(direcao);
                if (d != null) {
                    dinoCombate = d;
                    dirCombate = direcao;
                    pausado = true;
                }
            }

            tabuleiro.atualizar(jogador, dinossauros, caixas);
            if (!modoGui && dinoCombate == null) {
                moverDinossaurosConsole();
            }
            atualizarGui();
        }

        if (dinoCombate != null) {
            finalizarEncontro(dinoCombate, dinoAtacouPrimeiro, dirCombate);
        }
    }

    public void moverUmDinossauro(Dinossauro d) {
        boolean encontrouJogador = false;

        synchronized (this) {
            if (!podeJogar()) return;
            if (!dinossauros.contains(d) || !d.estaVivo()) return;

            encontrouJogador = d.mover(jogador, tabuleiro, random);
            tabuleiro.atualizar(jogador, dinossauros, caixas);

            if (encontrouJogador) {
                pausado = true;
                menu.avisoDinossauroEncontrou(d);
            }
            atualizarGui();
        }

        if (encontrouJogador) {
            finalizarEncontro(d, true, null);
        }
    }

    private void finalizarEncontro(Dinossauro dino, boolean dinoAtacouPrimeiro, Direcao dirCombate) {
        ResultadoCombate res = null;
        try {
            res = executarCombate(dino, dinoAtacouPrimeiro);
        } finally {
            synchronized (this) {
                if (res == ResultadoCombate.VENCEU && dirCombate != null && jogador.estaVivo()) {
                    tabuleiro.atualizar(jogador, dinossauros, caixas);
                    jogador.mover(dirCombate, tabuleiro);
                    tabuleiro.atualizar(jogador, dinossauros, caixas);
                }
                if (res == ResultadoCombate.FUGIU && dinossauros.contains(dino)) {
                    gerenciadorThreads.iniciarUma(dino);
                }
                liberarPausaSeJogavel();
                if (dinossauros.isEmpty() && estado == EstadoJogo.CONTINUAR) {
                    tratarVitoria();
                }
                atualizarGui();
            }
        }
    }

    private Compsognato abrirCaixa() {
        Compsognato surpresa = sistemaItens.processarCaixaNaPosicao(jogador, caixas);
        tabuleiro.atualizar(jogador, dinossauros, caixas);
        if (surpresa != null) {
            menu.mensagem("Cuidado! Um compsognato estava atrás da caixa!");
            dinossauros.add(surpresa);
            tabuleiro.atualizar(jogador, dinossauros, caixas);
        } else {
            menu.mensagem("Você encontrou um item na caixa!");
        }
        return surpresa;
    }

    private Dinossauro acharDinoNaDirecao(Direcao direcao) {
        int x = jogador.getPosicaoX() + direcao.dx;
        int y = jogador.getPosicaoY() + direcao.dy;
        for (Dinossauro d : dinossauros) {
            if (d.getPosicaoX() == x && d.getPosicaoY() == y) return d;
        }
        return null;
    }

    private ResultadoCombate executarCombate(Dinossauro dino, boolean dinoAtacouPrimeiro) {
        if (dinoAtacouPrimeiro) {
            if (!sistemaCombate.passouTestePercepcao(jogador)) {
                jogador.setSaude(jogador.getSaude() - dino.getDanoAtaque());
                menu.mensagem("O dinossauro te atacou! -" + dino.getDanoAtaque() + " de saúde.");
                if (jogador.getSaude() <= 0) {
                    tratarDerrota();
                    return ResultadoCombate.PERDEU;
                }
            } else {
                menu.mensagem("Você desviou do ataque inicial!");
            }
        }

        if (interfaceGui != null) interfaceGui.prepararCombate(jogador, dino);
        ResultadoCombate res = sistemaCombate.combate(jogador, dino, menu, leitorDeInput, tabuleiro);
        if (interfaceGui != null) interfaceGui.finalizarCombate();

        if (res == null) {
            res = dino.estaVivo() ? ResultadoCombate.FUGIU : ResultadoCombate.VENCEU;
        }

        if (res == ResultadoCombate.PERDEU) {
            tratarDerrota();
        } else if (res == ResultadoCombate.VENCEU) {
            dinossauros.remove(dino);
            gerenciadorThreads.pararUma(dino);
            tabuleiro.atualizar(jogador, dinossauros, caixas);
        } else if (res == ResultadoCombate.FUGIU) {
            separarAposFuga(dino);
        }
        return res;
    }

    private void separarAposFuga(Dinossauro d) {
        if (d.getPosicaoX() != jogador.getPosicaoX() || d.getPosicaoY() != jogador.getPosicaoY()) {
            tabuleiro.atualizar(jogador, dinossauros, caixas);
            return;
        }
        Direcao[] dirs = {Direcao.CIMA, Direcao.BAIXO, Direcao.ESQUERDA, Direcao.DIREITA};
        for (Direcao dir : dirs) {
            int nx = d.getPosicaoX() + dir.dx;
            int ny = d.getPosicaoY() + dir.dy;
            if (d.verificaMovimento(nx, ny, tabuleiro) == ResultadoMovimento.LIVRE) {
                d.setPosicaoX(nx);
                d.setPosicaoY(ny);
                break;
            }
        }
        tabuleiro.atualizar(jogador, dinossauros, caixas);
    }

    private void tratarVitoria() {
        menu.mensagemVitoria();
        estado = EstadoJogo.VITORIA;
        pausado = true;
        gerenciadorThreads.pararTodas();
        if (interfaceGui != null) interfaceGui.onVitoria();
    }

    private void tratarDerrota() {
        menu.mensagemDerrota();
        estado = EstadoJogo.DERROTA;
        pausado = true;
        gerenciadorThreads.pararTodas();
        if (modoGui && interfaceGui != null) interfaceGui.onDerrota();
    }

    private void liberarPausaSeJogavel() {
        if (estado == EstadoJogo.CONTINUAR && jogador.estaVivo() && !dinossauros.isEmpty()) {
            pausado = false;
        }
    }

    private boolean podeJogar() {
        return estado == EstadoJogo.CONTINUAR && !pausado;
    }

    private boolean cooldownPronto() {
        return System.currentTimeMillis() - ultimoMovimentoJogador >= INTERVALO_JOGADOR_MS;
    }

    private void resetarEstadoPartida() {
        estado = EstadoJogo.CONTINUAR;
        pausado = false;
        debugMode = false;
        ultimoMovimentoJogador = 0;
    }

    public void reiniciarPartida() {
        gerenciadorThreads.pararTodas();
        reiniciarPartidaInterno();
        resetarEstadoPartida();
        if (modoGui) gerenciadorThreads.iniciarTodas(dinossauros);
        atualizarGui();
    }

    public void pararThreadsDinossauros() {
        gerenciadorThreads.pararTodas();
    }

    private void atualizarGui() {
        if (interfaceGui != null) interfaceGui.atualizar();
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
                    jogador.setPercepcao(4 - leitorDeInput.lerInput(1, 3));
                    menu.escolherMapa();
                    int numeroMapa = leitorDeInput.lerInput(1, Macros.NUM_MAPAS);
                    carregadorMapa.carregar(Macros.PASTA_MAPAS + "mapa" + numeroMapa + ".txt",
                            tabuleiro, jogador, dinossauros, caixas);
                    finalizarCarregamentoPartida();
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

        resetarEstadoPartida();
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
            else if (inputOpcoes == 4) salvarPartida();
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
    }

    private void loopMovimento() {
        int inputMovimento = 0;
        while (inputMovimento != 5 && estado == EstadoJogo.CONTINUAR) {
            if (dinossauros.isEmpty()) {
                tratarVitoria();
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

    private void moverDinossaurosConsole() {
        for (Dinossauro d : new ArrayList<>(dinossauros)) {
            boolean encontrou = d.mover(jogador, tabuleiro, random);
            tabuleiro.atualizar(jogador, dinossauros, caixas);
            if (encontrou) {
                menu.avisoDinossauroEncontrou(d);
                executarCombate(d, true);
                if (estado != EstadoJogo.CONTINUAR) return;
            }
        }
    }
}
