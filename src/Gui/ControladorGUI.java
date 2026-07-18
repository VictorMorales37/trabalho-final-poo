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

    private JanelaPrincipal janela;
    private final Random random = new Random();
    private final Tabuleiro tabuleiro = new Tabuleiro(Macros.TAMANHO_TABULEIRO);
    private Jogador jogador = new Jogador(Macros.SIMB_JOGADOR, Macros.SAUDE_JOGADOR, Macros.PERCEPCAO_INICIAL);
    private final SistemaCombate combate = new SistemaCombate(random);
    private final SistemaSalvamento salvamento = new SistemaSalvamento(random);
    private final CarregadorMapa carregadorMapa = new CarregadorMapa(random);
    private final ArrayList<Dinossauro> dinossauros = new ArrayList<>();
    private final ArrayList<Caixa> caixas = new ArrayList<>();

    private Jogador jogadorInicial;
    private ArrayList<Dinossauro> dinosIniciais;
    private ArrayList<Caixa> caixasIniciais;

    private boolean debug = false;
    private boolean jogando = false;

    public void setJanela(JanelaPrincipal janela) {
        this.janela = janela;
    }

    public Tabuleiro getTabuleiro() { return tabuleiro; }
    public Jogador getJogador() { return jogador; }
    public SistemaCombate getCombate() { return combate; }
    public boolean isDebug() { return debug; }

    public void novoJogo(int dificuldade, int mapa) {
        jogador = new Jogador(Macros.SIMB_JOGADOR, Macros.SAUDE_JOGADOR, Macros.PERCEPCAO_INICIAL);
        jogador.setPercepcao(4 - dificuldade);
        dinossauros.clear();
        caixas.clear();
        tabuleiro.limpar();

        carregadorMapa.carregar(Macros.PASTA_MAPAS + "mapa" + mapa + ".txt",
                tabuleiro, jogador, dinossauros, caixas);
        prepararPartida();
        debug = false;
        jogando = true;
        msg("Nova partida!");
        atualizar();
    }

    public void carregar() {
        if (!salvamento.existeSave(Macros.ARQUIVO_SAVE)) {
            msg("Save não encontrado.");
            return;
        }
        dinossauros.clear();
        caixas.clear();
        salvamento.carregar(Macros.ARQUIVO_SAVE, tabuleiro, jogador, dinossauros, caixas);
        prepararPartida();
        debug = false;
        jogando = true;
        msg("Jogo carregado!");
        atualizar();
    }

    public void salvar() {
        if (!jogando) {
            msg("Nenhuma partida para salvar.");
            return;
        }
        salvamento.salvar(Macros.ARQUIVO_SAVE, jogador, dinossauros, caixas, tabuleiro);
        msg("Jogo salvo!");
    }

    public void toggleDebug() {
        debug = !debug;
        msg(debug ? "Debug ON" : "Debug OFF");
        atualizar();
    }

    public void curar() {
        if (!jogando) return;
        Item kit = jogador.pegarItem(KitMedico.class);
        if (kit == null) {
            msg("Sem kit médico.");
            return;
        }
        kit.usar(jogador, null);
        if (kit instanceof Consumivel c && c.consumidoAposUso()) {
            jogador.removerItem(kit);
        }
        msg("Kit médico usado.");
        atualizar();
    }

    public void mover(Direcao dir) {
        if (!jogando) return;

        if (dinossauros.isEmpty()) {
            msg("Você venceu!");
            janela.mostrarVitoria();
            return;
        }

        ResultadoMovimento res = jogador.mover(dir, tabuleiro);
        if (res == ResultadoMovimento.BLOQUEADO) {
            msg("Movimento bloqueado.");
            atualizar();
            return;
        }

        if (res == ResultadoMovimento.ENCONTROU_CAIXA) abrirCaixa();
        else if (res != ResultadoMovimento.LIVRE) combater(dir);

        if (!jogando) return;

        tabuleiro.atualizar(jogador, dinossauros, caixas);
        moverDinos();
        atualizar();
    }

    public void reiniciar() {
        if (jogadorInicial == null) return;
        jogador = jogadorInicial.copia();
        dinossauros.clear();
        for (Dinossauro d : dinosIniciais) dinossauros.add(d.copia());
        caixas.clear();
        for (Caixa c : caixasIniciais) caixas.add(c.copia());
        tabuleiro.setGrid(tabuleiro.getPosicoesIniciais());
        tabuleiro.atualizar(jogador, dinossauros, caixas);
        debug = false;
        jogando = true;
        msg("Partida reiniciada.");
        atualizar();
    }

    public void fimCombate(Dinossauro d, ResultadoCombate res, Direcao dir) {
        if (res == ResultadoCombate.PERDEU) {
            jogando = false;
            janela.mostrarDerrota();
            atualizar();
            return;
        }
        if (res == ResultadoCombate.VENCEU) {
            dinossauros.remove(d);
            tabuleiro.atualizar(jogador, dinossauros, caixas);
            if (dir != null && dir != Direcao.INVALIDA) {
                jogador.mover(dir, tabuleiro);
            }
            if (dinossauros.isEmpty()) {
                msg("Você venceu!");
                janela.mostrarVitoria();
            }
        }
        atualizar();
    }

    private void prepararPartida() {
        tabuleiro.atualizar(jogador, dinossauros, caixas);
        tabuleiro.salvarPosicoes();
        jogadorInicial = jogador.copia();
        dinosIniciais = new ArrayList<>();
        for (Dinossauro d : dinossauros) dinosIniciais.add(d.copia());
        caixasIniciais = new ArrayList<>();
        for (Caixa c : caixas) caixasIniciais.add(c.copia());
    }

    private void abrirCaixa() {
        for (Caixa c : caixas) {
            if (c.getPosicaoX() == jogador.getPosicaoX() && c.getPosicaoY() == jogador.getPosicaoY()) {
                if (c.getItem() != null) {
                    jogador.receberItem(c.getItem());
                    msg("Achou: " + c.getItem().getNome());
                    caixas.remove(c);
                } else {
                    Compsognato surpresa = c.getCompsognato();
                    caixas.remove(c);
                    surpresa.setPosicaoX(jogador.getPosicaoX());
                    surpresa.setPosicaoY(jogador.getPosicaoY());
                    dinossauros.add(surpresa);
                    msg("Compsognato surpresa!");
                    atacarPrimeiro(surpresa);
                    janela.abrirCombate(surpresa, null);
                }
                return;
            }
        }
    }

    private void combater(Direcao dir) {
        for (Dinossauro d : dinossauros) {
            if (d.getPosicaoX() == jogador.getPosicaoX() + dir.dx &&
                d.getPosicaoY() == jogador.getPosicaoY() + dir.dy) {
                janela.abrirCombate(d, dir);
                return;
            }
        }
    }

    private void moverDinos() {
        for (Dinossauro d : new ArrayList<>(dinossauros)) {
            if (d.mover(jogador, tabuleiro, random)) {
                tabuleiro.atualizar(jogador, dinossauros, caixas);
                msg("Um dinossauro te achou!");
                atacarPrimeiro(d);
                janela.abrirCombate(d, null);
                return;
            }
            tabuleiro.atualizar(jogador, dinossauros, caixas);
        }
    }

    private void atacarPrimeiro(Dinossauro d) {
        if (!combate.passouTestePercepcao(jogador)) {
            jogador.setSaude(jogador.getSaude() - d.getDanoAtaque());
            msg("Ataque surpresa! -" + d.getDanoAtaque() + " HP");
            if (jogador.getSaude() <= 0) {
                jogando = false;
                janela.mostrarDerrota();
            }
        } else {
            msg("Desviou do ataque!");
        }
    }

    private void msg(String texto) {
        if (janela != null) janela.mostrarMensagem(texto);
    }

    private void atualizar() {
        if (janela != null) janela.atualizarTela();
    }
}
