package Entidades;

import Itens.ArmaDardos;
import Itens.Inventario;
import Itens.Item;
import Sistema.Movimentacao.Direcao;
import Sistema.Movimentacao.ResultadoMovimento;
import Sistema.Tabuleiro;
import Util.Macros;

import java.util.ArrayList;

public class Jogador extends Personagem {
    private final char simbolo;
    private int percepcao;

    private Inventario inventario;

    public Jogador(char simbolo, int saude, int percepcao) {
        this.simbolo = simbolo;
        this.saude = saude;
        this.percepcao = percepcao;
        posicaoX = -1;
        posicaoY = -1;
        inventario = new Inventario();
        this.receberItem(new ArmaDardos());
    }

    public Jogador copia() {
        Jogador j = new Jogador(simbolo, saude, percepcao);
        j.setPosicaoX(posicaoX);
        j.setPosicaoY(posicaoY);
        j.inventario = this.inventario;
        return j;
    }

    @Override
    public char getSimbolo() {
        return simbolo;
    }

    public ResultadoMovimento mover(Direcao direcao, Tabuleiro tabuleiro) {
        int novoX = getPosicaoX() + direcao.dx;
        int novoY = getPosicaoY() + direcao.dy;

        ResultadoMovimento resultado = verificaMovimento(novoX, novoY, tabuleiro);

        if (resultado == ResultadoMovimento.LIVRE || resultado == ResultadoMovimento.ENCONTROU_CAIXA) {
            setPosicaoX(novoX);
            setPosicaoY(novoY);
        }
        return resultado;
    }

    public void receberItem(Item item) {
        inventario.adicionar(item);
    }

    public void limparInventario() {
        inventario.limpar();
    }

    public Inventario getInventario() {
        return inventario;
    }

    public Item pegarItem(Class<?> tipo) {
        return inventario.pegar(tipo);
    }

    public void removerItem(Item item) {
        inventario.remover(item);
    }

    public void receberCura(int cura){
        if (this.saude + cura > Macros.SAUDE_JOGADOR) {
            this.saude = Macros.SAUDE_JOGADOR;
        }
        else {
            this.saude += cura ;
        }
    }

    public void setPercepcao(int p) {
        percepcao = p;
    }

    public int getPercepcao() {
        return percepcao;
    }
}

