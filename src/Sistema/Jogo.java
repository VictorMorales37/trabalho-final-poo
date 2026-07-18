package Sistema;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import Entidades.*;
import Entidades.Dinossauros.Compsognato;
import Entidades.Dinossauros.Dinossauro;
import Itens.Item;
import Itens.KitMedico;
import Itens.Consumivel;
import Sistema.Movimentacao.*;
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

    public Jogo() {
        random = new Random();
        carregadorMapa = new CarregadorMapa(random);
        sistemaSalvamento = new SistemaSalvamento(random);
        Scanner scanner = new Scanner(System.in);
        tabuleiro = new Tabuleiro(Macros.TAMANHO_TABULEIRO);
        sistemaCombate = new SistemaCombate(random);
        sistemaItens = new SistemaItens();
        menu = new Menu();
        jogador = new Jogador(Macros.SIMB_JOGADOR, Macros.SAUDE_JOGADOR, Macros.PERCEPCAO_INICIAL);
        leitorDeInput = new LeitorDeInput(scanner);
        dinossauros = new ArrayList<>();
        caixas = new ArrayList<>();
    }

    public EstadoJogo getEstado() {
        return estado;
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
            reiniciarPartida();
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
            else if (inputOpcoes == 2) {
                Item kit = jogador.pegarItem(KitMedico.class);
                if (kit == null) {
                    System.out.println("Você não tem kits médicos.");
                } else {
                    kit.usar(jogador, null);
                    if (kit instanceof Consumivel && ((Consumivel) kit).consumidoAposUso()) {
                        jogador.removerItem(kit);
                    }
                }
            }
            else if (inputOpcoes == 3) {
                debugMode = true;
                System.out.println("MODO DEBUG ATIVADO");
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

    private void reiniciarPartida() {
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

            Direcao direcao = leitorDeInput.lerDirecao(inputMovimento);
            ResultadoMovimento resMovimento = jogador.mover(direcao, tabuleiro);
            menu.avisoMovimento(resMovimento);

            processarResultadoMovimento(resMovimento, direcao);
            if (estado != EstadoJogo.CONTINUAR) return;

            tabuleiro.atualizar(jogador, dinossauros, caixas);
            moverDinossauros();
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
            processarResultadoCombate(surpresa, true);
        }
    }

    private void processarCombate(Direcao direcao) {
        for (Dinossauro d : dinossauros) {
            if (d.getPosicaoX() != jogador.getPosicaoX() + direcao.dx ||
                    d.getPosicaoY() != jogador.getPosicaoY() + direcao.dy) continue;

            ResultadoCombate res = processarResultadoCombate(d, false);
            if (res == ResultadoCombate.VENCEU) {
                tabuleiro.atualizar(jogador, dinossauros, caixas);
                jogador.mover(direcao, tabuleiro);
            }
            return;
        }
    }

    private ResultadoCombate processarResultadoCombate(Dinossauro d, boolean dinoAtacouPrimeiro) {
        if (dinoAtacouPrimeiro) {
            boolean desviou = sistemaCombate.passouTestePercepcao(jogador);
            if (!desviou) {
                jogador.setSaude(jogador.getSaude() - d.getDanoAtaque());
                System.out.println("O dinossauro te atacou! -" + d.getDanoAtaque() + " de saúde.");
                if (jogador.getSaude() <= 0) {
                    menu.mensagemDerrota();
                    sairDoJogo();
                    return ResultadoCombate.PERDEU;
                }
            } else {
                System.out.println("Você desviou do ataque inicial!");
            }
        }

        ResultadoCombate res = sistemaCombate.combate(jogador, d, menu, leitorDeInput, tabuleiro);
        if (res == ResultadoCombate.PERDEU) {
            menu.mensagemDerrota();
            sairDoJogo();
        }
        if (res == ResultadoCombate.VENCEU) dinossauros.remove(d);
        return res;
    }

    private void moverDinossauros() {

        for (Dinossauro d : new ArrayList<>(dinossauros)) {
            boolean encontrouJogador = d.mover(jogador, tabuleiro, random);
            tabuleiro.atualizar(jogador, dinossauros, caixas);

            if (encontrouJogador) {
                menu.avisoDinossauroEncontrou(d);
                processarResultadoCombate(d, true);
                if (estado != EstadoJogo.CONTINUAR) return;
            }

        }
    }
}
