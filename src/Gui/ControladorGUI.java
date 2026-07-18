package Gui;

import Entidades.Caixa;
import Entidades.Personagens.Dinossauros.Compsognato;
import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.Consumiveis.Consumivel;
import Itens.Consumiveis.KitMedico;
import Itens.Item;
import Sistema.*;
import Util.*;

import java.util.ArrayList;
import java.util.Random;

public class ControladorGUI {

    public interface Listener {
        void onEstadoAlterado();
        void onMensagem(String mensagem);
        void onCombateIniciado(Dinossauro dinossauro, boolean dinoAtacouPrimeiro, Direcao direcaoMovimento);
        void onVitoria();
        void onDerrota();
        void onPartidaCarregada();
    }

    private final Random random;
    private final Tabuleiro tabuleiro;
    private Jogador jogador;
    private final SistemaCombate sistemaCombate;
    private final SistemaItens sistemaItens;
    private final SistemaSalvamento sistemaSalvamento;
    private final CarregadorMapa carregadorMapa;
    private final ArrayList<Dinossauro> dinossauros;
    private final ArrayList<Caixa> caixas;

    private Jogador jogadorInicial;
    private ArrayList<Dinossauro> dinossaurosIniciais;
    private ArrayList<Caixa> caixasIniciais;

    private boolean debugMode = false;
    private boolean partidaAtiva = false;
    private Listener listener;

    public ControladorGUI() {
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

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public Tabuleiro getTabuleiro() {
        return tabuleiro;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public ArrayList<Dinossauro> getDinossauros() {
        return dinossauros;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isPartidaAtiva() {
        return partidaAtiva;
    }

    public void iniciarNovoJogo(int dificuldade, int numeroMapa) {
        jogador = new Jogador(Macros.SIMB_JOGADOR, Macros.SAUDE_JOGADOR, Macros.PERCEPCAO_INICIAL);
        jogador.setPercepcao(4 - dificuldade);
        dinossauros.clear();
        caixas.clear();
        tabuleiro.limpar();

        String caminho = Macros.PASTA_MAPAS + "mapa" + numeroMapa + ".txt";
        carregadorMapa.carregar(caminho, tabuleiro, jogador, dinossauros, caixas);
        finalizarCarregamentoPartida();
        debugMode = false;
        partidaAtiva = true;
        notificar("Nova partida iniciada!");
        notificarAlteracao();
    }

    public void carregarJogo() {
        if (!sistemaSalvamento.existeSave(Macros.ARQUIVO_SAVE)) {
            notificar("Nenhum save encontrado em " + Macros.ARQUIVO_SAVE);
            return;
        }
        dinossauros.clear();
        caixas.clear();
        sistemaSalvamento.carregar(Macros.ARQUIVO_SAVE, tabuleiro, jogador, dinossauros, caixas);
        finalizarCarregamentoPartida();
        debugMode = false;
        partidaAtiva = true;
        notificar("Jogo carregado!");
        if (listener != null) listener.onPartidaCarregada();
        notificarAlteracao();
    }

    public void salvarJogo() {
        if (!partidaAtiva) {
            notificar("Nenhuma partida em andamento para salvar.");
            return;
        }
        sistemaSalvamento.salvar(Macros.ARQUIVO_SAVE, jogador, dinossauros, caixas, tabuleiro);
        notificar("Jogo salvo em " + Macros.ARQUIVO_SAVE);
    }

    public void alternarDebugMode() {
        debugMode = !debugMode;
        notificar(debugMode ? "Modo DEBUG ativado" : "Modo DEBUG desativado");
        notificarAlteracao();
    }

    public void curar() {
        if (!partidaAtiva) return;
        Item kit = jogador.pegarItem(KitMedico.class);
        if (kit == null) {
            notificar("Você não tem kits médicos.");
            return;
        }
        kit.usar(jogador, null);
        if (kit instanceof Consumivel && ((Consumivel) kit).consumidoAposUso()) {
            jogador.removerItem(kit);
        }
        notificar("Você usou um kit médico.");
        notificarAlteracao();
    }

    public void mover(Direcao direcao) {
        if (!partidaAtiva || direcao == Direcao.INVALIDA) return;

        if (dinossauros.isEmpty()) {
            notificar("Você EXTINGUIU os dinossauros!");
            if (listener != null) listener.onVitoria();
            return;
        }

        ResultadoMovimento resultado = jogador.mover(direcao, tabuleiro);
        notificarMovimento(resultado);

        if (resultado == ResultadoMovimento.BLOQUEADO) {
            notificarAlteracao();
            return;
        }

        processarResultadoMovimento(resultado, direcao);
        if (!partidaAtiva) return;

        tabuleiro.atualizar(jogador, dinossauros, caixas);
        moverDinossauros();
        notificarAlteracao();
    }

    public void processarResultadoCombate(Dinossauro d, ResultadoCombate res, Direcao direcaoMovimento) {
        if (res == ResultadoCombate.PERDEU) {
            notificar("Você MORREU!");
            partidaAtiva = false;
            if (listener != null) listener.onDerrota();
            notificarAlteracao();
            return;
        }

        if (res == ResultadoCombate.VENCEU) {
            dinossauros.remove(d);
            tabuleiro.atualizar(jogador, dinossauros, caixas);
            if (direcaoMovimento != null && direcaoMovimento != Direcao.INVALIDA) {
                jogador.mover(direcaoMovimento, tabuleiro);
            }
            if (dinossauros.isEmpty()) {
                notificar("Você EXTINGUIU os dinossauros!");
                if (listener != null) listener.onVitoria();
            }
        }

        notificarAlteracao();
    }

    public SistemaCombate getSistemaCombate() {
        return sistemaCombate;
    }

    public void reiniciarPartida() {
        if (jogadorInicial == null) return;
        jogador = jogadorInicial.copia();
        dinossauros.clear();
        for (Dinossauro d : dinossaurosIniciais) dinossauros.add(d.copia());
        caixas.clear();
        for (Caixa c : caixasIniciais) caixas.add(c.copia());
        tabuleiro.setGrid(tabuleiro.getPosicoesIniciais());
        tabuleiro.atualizar(jogador, dinossauros, caixas);
        debugMode = false;
        partidaAtiva = true;
        notificar("Partida reiniciada!");
        notificarAlteracao();
    }

    private void finalizarCarregamentoPartida() {
        tabuleiro.atualizar(jogador, dinossauros, caixas);
        tabuleiro.salvarPosicoes();
        salvarEstadoInicial();
    }

    private void salvarEstadoInicial() {
        jogadorInicial = jogador.copia();
        dinossaurosIniciais = new ArrayList<>();
        for (Dinossauro d : dinossauros) dinossaurosIniciais.add(d.copia());
        caixasIniciais = new ArrayList<>();
        for (Caixa c : caixas) caixasIniciais.add(c.copia());
    }

    private void processarResultadoMovimento(ResultadoMovimento resMovimento, Direcao direcao) {
        if (resMovimento == ResultadoMovimento.ENCONTROU_CAIXA) {
            processarCaixa(direcao);
        } else if (resMovimento != ResultadoMovimento.LIVRE &&
                resMovimento != ResultadoMovimento.BLOQUEADO) {
            processarCombate(direcao);
        }
    }

    private void processarCaixa(Direcao direcao) {
        Caixa caixaEncontrada = null;
        for (Caixa c : caixas) {
            if (c.getPosicaoX() == jogador.getPosicaoX() && c.getPosicaoY() == jogador.getPosicaoY()) {
                caixaEncontrada = c;
                break;
            }
        }
        if (caixaEncontrada == null) return;

        if (caixaEncontrada.getItem() != null) {
            Item item = caixaEncontrada.getItem();
            jogador.receberItem(item);
            notificar("Você encontrou " + item.getNome());
            caixas.remove(caixaEncontrada);
        } else {
            Compsognato surpresa = caixaEncontrada.getCompsognato();
            caixas.remove(caixaEncontrada);
            surpresa.setPosicaoX(jogador.getPosicaoX());
            surpresa.setPosicaoY(jogador.getPosicaoY());
            dinossauros.add(surpresa);
            notificar("Cuidado! Um compsognato estava atrás da caixa!");
            iniciarCombate(surpresa, true, null);
        }
    }

    private void processarCombate(Direcao direcao) {
        for (Dinossauro d : dinossauros) {
            if (d.getPosicaoX() != jogador.getPosicaoX() + direcao.dx ||
                    d.getPosicaoY() != jogador.getPosicaoY() + direcao.dy) continue;
            iniciarCombate(d, false, direcao);
            return;
        }
    }

    private void iniciarCombate(Dinossauro d, boolean dinoAtacouPrimeiro, Direcao direcao) {
        if (dinoAtacouPrimeiro) {
            boolean desviou = sistemaCombate.passouTestePercepcao(jogador);
            if (!desviou) {
                jogador.setSaude(jogador.getSaude() - d.getDanoAtaque());
                notificar("O dinossauro te atacou! -" + d.getDanoAtaque() + " de saúde.");
                if (jogador.getSaude() <= 0) {
                    notificar("Você MORREU!");
                    partidaAtiva = false;
                    if (listener != null) listener.onDerrota();
                    return;
                }
            } else {
                notificar("Você desviou do ataque inicial!");
            }
        }
        if (listener != null) {
            listener.onCombateIniciado(d, dinoAtacouPrimeiro, direcao);
        }
    }

    private void moverDinossauros() {
        for (Dinossauro d : new ArrayList<>(dinossauros)) {
            boolean encontrouJogador = d.mover(jogador, tabuleiro, random);
            tabuleiro.atualizar(jogador, dinossauros, caixas);

            if (encontrouJogador) {
                notificar("Um " + d.getClass().getSimpleName() + " te encontrou!");
                iniciarCombate(d, true, null);
                return;
            }
        }
    }

    private void notificarMovimento(ResultadoMovimento resultado) {
        String msg = switch (resultado) {
            case BLOQUEADO -> "Movimento não permitido";
            case ENCONTROU_COMPSOGNATO -> "Encontrou compsognato!";
            case ENCONTROU_TROODONTE -> "Encontrou troodonte!";
            case ENCONTROU_VELOCIRAPTOR -> "Encontrou velociraptor!";
            case ENCONTROU_TREX -> "Encontrou Tiranossauro Rex!";
            default -> null;
        };
        if (msg != null) notificar(msg);
    }

    private void notificar(String mensagem) {
        if (listener != null) listener.onMensagem(mensagem);
    }

    private void notificarAlteracao() {
        if (listener != null) listener.onEstadoAlterado();
    }
}
