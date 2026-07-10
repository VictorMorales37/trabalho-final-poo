package Entidades.Dinossauros;
import Util.Macros;

public class TiranossauroRex extends Dinossauro {
    public TiranossauroRex() {
        super(Macros.SAUDE_TREX, 0);
    }

    @Override
    public Dinossauro copia() {
        TiranossauroRex d = new TiranossauroRex();

        d.setPosicaoX(getPosicaoX());
        d.setPosicaoY(getPosicaoY());
        d.setSaude(getSaude());

        return d;
    }

    @Override
    public char getSimbolo() { return Macros.SIMB_TREX; }

    @Override
    public boolean podeSerAtacadoSemArma() { return false; }

    @Override
    public int getDanoAtaque() { return 4; }
}