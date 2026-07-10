package Entidades.Dinossauros;
import Util.Macros;

public class Compsognato extends Dinossauro {
    public Compsognato() {
        super(Macros.SAUDE_COMPSOGNATO, Macros.VEL_COMPSOGNATO);
    }

    @Override
    public Dinossauro copia() {
        Compsognato d = new Compsognato();

        d.setPosicaoX(getPosicaoX());
        d.setPosicaoY(getPosicaoY());
        d.setSaude(getSaude());

        return d;
    }

    @Override
    public char getSimbolo() { return Macros.SIMB_COMPSOGNATO; }
}