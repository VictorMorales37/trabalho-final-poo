package Entidades;

import Sistema.Tabuleiro;
import Sistema.Movimentacao.ResultadoMovimento;
import Util.Macros;

public abstract class Entidade {
    protected int posicaoX;
    protected int posicaoY;
     // Mantendo para manter a assinatura antiga, subclasses usarão se precisarem

    public abstract char getSimbolo();

    public void setPosicaoX(int x) {
        this.posicaoX = x;
    }

    public void setPosicaoY(int y) {
        this.posicaoY = y;
    }

    public int getPosicaoX() {
        return posicaoX;
    }

    public int getPosicaoY() {
        return posicaoY;
    }

    public ResultadoMovimento verificaMovimento(int x, int y, Tabuleiro tabuleiro) {
        if (x < 0 || x >= Macros.TAMANHO_TABULEIRO || y < 0 || y >= Macros.TAMANHO_TABULEIRO) {
            return ResultadoMovimento.BLOQUEADO;
        }

        Entidade ent = tabuleiro.getGrid()[x][y];
        if (ent != null) {
            if (ent.getSimbolo() == Macros.SIMB_PAREDE) return ResultadoMovimento.BLOQUEADO;
            else if (ent.getSimbolo() == Macros.SIMB_COMPSOGNATO) return ResultadoMovimento.ENCONTROU_COMPSOGNATO;
            else if (ent.getSimbolo() == Macros.SIMB_TROODONTE) return ResultadoMovimento.ENCONTROU_TROODONTE;
            else if (ent.getSimbolo() == Macros.SIMB_VELOCIRAPTOR) return ResultadoMovimento.ENCONTROU_VELOCIRAPTOR;
            else if (ent.getSimbolo() == Macros.SIMB_TREX) return ResultadoMovimento.ENCONTROU_TREX;
            else if (ent.getSimbolo() == Macros.SIMB_CAIXA) return ResultadoMovimento.ENCONTROU_CAIXA;
        }
        return ResultadoMovimento.LIVRE;
    }
}
