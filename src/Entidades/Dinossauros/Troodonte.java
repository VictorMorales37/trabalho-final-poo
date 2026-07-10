package Entidades.Dinossauros;
import Util.Macros;

public class Troodonte extends Dinossauro {
    public Troodonte() {
        super(Macros.SAUDE_TROODONTE, Macros.VEL_TROODONTE);
    }

    @Override
    public Dinossauro copia() {
        Troodonte d = new Troodonte();

        d.setPosicaoX(getPosicaoX());
        d.setPosicaoY(getPosicaoY());
        d.setSaude(getSaude());

        return d;
    }

    @Override
    public char getSimbolo() { return Macros.SIMB_TROODONTE; }
}