package Entidades.Dinossauros;

import Entidades.Entidade;
import Entidades.Jogador;
import Entidades.Personagem;
import Sistema.Movimentacao.Direcao;
import Sistema.Movimentacao.ResultadoMovimento;
import Sistema.Tabuleiro;

import java.util.Random;

public abstract class Dinossauro extends Personagem {

    protected int velocidade;

    public Dinossauro(int saude, int velocidade) {
        this.saude = saude;
        this.velocidade = velocidade;
    }

    public abstract Dinossauro copia();

    @Override
    public abstract char getSimbolo();

    public int getVelocidade() { return velocidade; }
    
    public boolean podeSerAtacadoSemArma() {
        return true;
    }

    public boolean podeSerAtacadoComDardos() {
        return true;
    }

    public int getDanoAtaque() {
        return 1;
    }

    private Direcao direcaoAleatoria(Random random) {
        int val = random.nextInt(4);
        return switch (val) {
            case 0 -> Direcao.CIMA;
            case 1 -> Direcao.BAIXO;
            case 2 -> Direcao.DIREITA;
            case 3 -> Direcao.ESQUERDA;
            default -> Direcao.INVALIDA;
        };
    }

    public boolean mover(Jogador jogador, Tabuleiro tabuleiro, Random random) {
        for (int i = 0; i < getVelocidade(); i++) {
            int novoX;
            int novoY;
            int tentativas = 0;
            do {
                Direcao direcaoAleatoria = direcaoAleatoria(random);
                novoX = getPosicaoX() + direcaoAleatoria.dx;
                novoY = getPosicaoY() + direcaoAleatoria.dy;
                tentativas++;
            } while ((verificaMovimento(novoX, novoY, tabuleiro) != ResultadoMovimento.LIVRE &&
                    !(novoX == jogador.getPosicaoX() && novoY == jogador.getPosicaoY()))
                    && tentativas < 4);

            if (tentativas >= 4) return false;

            setPosicaoX(novoX);
            setPosicaoY(novoY);

            if (getPosicaoX() == jogador.getPosicaoX() &&
                    getPosicaoY() == jogador.getPosicaoY()) {
                return true;
            }
        }
        return false;
    }
}