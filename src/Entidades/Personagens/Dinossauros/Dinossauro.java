package Entidades.Personagens.Dinossauros;

import Entidades.Personagens.Jogador;
import Entidades.Personagens.Personagem;
import Util.Macros;
import Util.Direcao;
import Util.ResultadoMovimento;
import Util.Copiavel;
import Sistema.Tabuleiro;

import java.util.Random;

public abstract class Dinossauro extends Personagem implements Copiavel<Dinossauro> {

    protected int velocidade;

    public Dinossauro(int saude, int velocidade) {
        super(Macros.SIMB_COMPSOGNATO, saude);
        this.velocidade = velocidade;
    }

    @Override
    public abstract Dinossauro copia();

    @Override
    public abstract char getSimbolo();

    public int getVelocidade() {
        return velocidade;
    }

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
        // já está na mesma célula = encontrou
        if (getPosicaoX() == jogador.getPosicaoX() && getPosicaoY() == jogador.getPosicaoY()) {
            return true;
        }

        for (int i = 0; i < getVelocidade(); i++) {
            int novoX = getPosicaoX();
            int novoY = getPosicaoY();
            Direcao escolhida = Direcao.INVALIDA;
            int tentativas = 0;
            boolean valido = false;

            do {
                escolhida = direcaoAleatoria(random);
                novoX = getPosicaoX() + escolhida.dx;
                novoY = getPosicaoY() + escolhida.dy;
                tentativas++;

                boolean noJogador = novoX == jogador.getPosicaoX() && novoY == jogador.getPosicaoY();
                boolean livre = verificaMovimento(novoX, novoY, tabuleiro) == ResultadoMovimento.LIVRE;
                valido = livre || noJogador;
            } while (!valido && tentativas < 4);

            if (!valido) return false;

            setPosicaoX(novoX);
            setPosicaoY(novoY);

            if (getPosicaoX() == jogador.getPosicaoX() && getPosicaoY() == jogador.getPosicaoY()) {
                return true;
            }
        }
        return false;
    }
}
