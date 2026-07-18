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
        if (interfaceGui != null) interfaceGui.atualizar();
    }

    public void carregarPartida() {
        carregarSave();
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

    public void executarMovimento(Direcao direcao) {
        if (estado != EstadoJogo.CONTINUAR) return;

        if (dinossauros.isEmpty()) {
            menu.mensagemVitoria();
            if (interfaceGui != null) interfaceGui.onVitoria();
            return;
        }

        ResultadoMovimento resMovimento = jogador.mover(direcao, tabuleiro);
        menu.avisoMovimento(resMovimento);

        if (resMovimento == ResultadoMovimento.BLOQUEADO) {
            if (interfaceGui != null) interfaceGui.atualizar();
            return;
        }

        processarResultadoMovimento(resMovimento, direcao);
        if (!modoGui && estado != EstadoJogo.CONTINUAR) return;

        tabuleiro.atualizar(jogador, dinossauros, caixas);
        moverDinossauros();

        if (interfaceGui != null) interfaceGui.atualizar();
    }

    public void reiniciarPartida() {
        reiniciarPartidaInterno();
        if (interfaceGui != null) interfaceGui.atualizar();
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
        if (surpresa != null) {
            dinossauros.add(surpresa);
            processarResultadoCombate(surpresa, true, null);
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
        if (res == ResultadoCombate.VENCEU) dinossauros.remove(d);
        return res;
    }

    private void tratarDerrota() {
        menu.mensagemDerrota();
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
