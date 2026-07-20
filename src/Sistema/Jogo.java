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
    private final GerenciadorThreads gerenciadorThreads;
    private final GerenciadorEncontros gerenciadorEncontros;
    private final EstadoExecucao execucao;
    private final ArrayList<Dinossauro> dinossauros;
    private final ArrayList<Caixa> caixas;
    private final boolean modoGui;

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
        execucao = new EstadoExecucao();
        gerenciadorEncontros = new GerenciadorEncontros(sistemaCombate, sistemaItens, menu,
                leitorDeInput, tabuleiro, execucao, gerenciadorThreads, modoGui);
        jogador = new Jogador(Macros.SIMB_JOGADOR, Macros.SAUDE_JOGADOR, Macros.PERCEPCAO_INICIAL);
        dinossauros = new ArrayList<>();
        caixas = new ArrayList<>();
    }

    public void setInterfaceGui(InterfaceGui interfaceGui) {
        this.interfaceGui = interfaceGui;
        gerenciadorEncontros.setInterfaceGui(interfaceGui);
    }

    public EstadoJogo getEstado() {
        return execucao.getEstado();
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public boolean isDebugMode() {
        return execucao.isDebug();
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
        execucao.alternarDebug();
        menu.mensagem(execucao.isDebug() ? "MODO DEBUG ATIVADO" : "MODO DEBUG DESATIVADO");
        atualizarGui();
    }

    public void usarKitMedico() {
        Item kit = jogador.pegarItem(KitMedico.class);
        if (kit == null) return;
        kit.usar(jogador, null, menu);
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
            if (!execucao.podeJogar()) return;
            if (!execucao.cooldownPronto()) return;
            execucao.registrarMovimentoJogador();

            if (dinossauros.isEmpty()) {
                gerenciadorEncontros.tratarVitoria();
                return;
            }

            ResultadoMovimento res = jogador.mover(direcao, tabuleiro);
            menu.avisoMovimento(res);

            if (res == ResultadoMovimento.BLOQUEADO) {
                atualizarGui();
                return;
            }

            if (res == ResultadoMovimento.ENCONTROU_CAIXA) {
                Compsognato surpresa = gerenciadorEncontros.abrirCaixa(jogador, caixas, dinossauros);
                if (surpresa != null) {
                    dinoCombate = surpresa;
                    dinoAtacouPrimeiro = true;
                    execucao.setPausado(true);
                }
            } else if (res != ResultadoMovimento.LIVRE) {
                Dinossauro d = gerenciadorEncontros.acharDinoNaDirecao(jogador, direcao, dinossauros);
                if (d != null) {
                    dinoCombate = d;
                    dirCombate = direcao;
                    execucao.setPausado(true);
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
            if (!execucao.podeJogar()) return;
            if (!dinossauros.contains(d) || !d.estaVivo()) return;

            encontrouJogador = d.mover(jogador, tabuleiro, random);
            tabuleiro.atualizar(jogador, dinossauros, caixas);

            if (encontrouJogador) {
                execucao.setPausado(true);
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
            res = gerenciadorEncontros.executarCombate(jogador, dino, dinoAtacouPrimeiro, dinossauros, caixas);
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
                gerenciadorEncontros.liberarPausaSeJogavel(jogador, dinossauros);
                gerenciadorEncontros.verificarVitoria(dinossauros);
                atualizarGui();
            }
        }
    }

    private void resetarEstadoPartida() {
        execucao.resetarParaPartida();
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
        if (execucao.getEstado() == EstadoJogo.REINICIAR) {
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
                    execucao.setEstado(EstadoJogo.SAIR);
                    return;
                }
            }
        }

        resetarEstadoPartida();
        loopJogo();
    }

    private void loopJogo() {
        while (execucao.getEstado() == EstadoJogo.CONTINUAR) {
            menu.menuPrincipal();
            int inputOpcoes = leitorDeInput.lerInput(1, 5);

            if (inputOpcoes == 1) loopMovimento();
            else if (inputOpcoes == 2) usarKitMedico();
            else if (inputOpcoes == 3) {
                execucao.setDebug(true);
                menu.mensagem("MODO DEBUG ATIVADO");
            }
            else if (inputOpcoes == 4) salvarPartida();
            else if (inputOpcoes == 5) sairDoJogo();
        }
    }

    private void sairDoJogo() {
        menu.menuSaida();
        int inputOpcoes = leitorDeInput.lerInput(1, 3);

        if (inputOpcoes == 1) execucao.setEstado(EstadoJogo.REINICIAR);
        else if (inputOpcoes == 2) execucao.setEstado(EstadoJogo.NOVO_JOGO);
        else {
            menu.mensagemSaida();
            execucao.setEstado(EstadoJogo.SAIR);
        }
    }

    private void salvarEstadoInicial() {
        jogadorInicial = jogador.copia();
        dinossaurosIniciais = copiarLista(dinossauros);
        caixasIniciais = copiarLista(caixas);
    }

    private void reiniciarPartidaInterno() {
        jogador = jogadorInicial.copia();
        dinossauros.clear();
        dinossauros.addAll(copiarLista(dinossaurosIniciais));
        caixas.clear();
        caixas.addAll(copiarLista(caixasIniciais));
        tabuleiro.setGrid(tabuleiro.getPosicoesIniciais());
        tabuleiro.atualizar(jogador, dinossauros, caixas);
    }

    private <T extends Copiavel<T>> ArrayList<T> copiarLista(ArrayList<T> lista) {
        ArrayList<T> copia = new ArrayList<>();
        for (T item : lista) copia.add(item.copia());
        return copia;
    }

    private void loopMovimento() {
        int inputMovimento = 0;
        while (inputMovimento != 5 && execucao.getEstado() == EstadoJogo.CONTINUAR) {
            if (dinossauros.isEmpty()) {
                gerenciadorEncontros.tratarVitoria();
                sairDoJogo();
                return;
            }

            menu.statusJogador(jogador);
            if (execucao.isDebug()) menu.mostrarTabuleiroDEBUG(tabuleiro);
            else menu.mostrarTabuleiroJogador(tabuleiro, jogador);
            menu.opcoesMovimento();

            inputMovimento = leitorDeInput.lerInput(1, 5);
            if (inputMovimento == 5) continue;

            executarMovimento(leitorDeInput.lerDirecao(inputMovimento));
            if (execucao.getEstado() != EstadoJogo.CONTINUAR) return;
        }
    }

    private void moverDinossaurosConsole() {
        for (Dinossauro d : new ArrayList<>(dinossauros)) {
            boolean encontrou = d.mover(jogador, tabuleiro, random);
            tabuleiro.atualizar(jogador, dinossauros, caixas);
            if (encontrou) {
                menu.avisoDinossauroEncontrou(d);
                gerenciadorEncontros.executarCombate(jogador, d, true, dinossauros, caixas);
                if (execucao.getEstado() != EstadoJogo.CONTINUAR) return;
            }
        }
    }
}
