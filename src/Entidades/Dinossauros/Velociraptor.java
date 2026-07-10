package Entidades.Dinossauros;
import Entidades.Entidade;
import Util.Macros;

public class Velociraptor extends Dinossauro {
    public Velociraptor() {
        super(Macros.SAUDE_VELOCIRAPTOR, Macros.VEL_VELOCIRAPTOR);
    }

    @Override
    public Dinossauro copia() {
        Velociraptor d = new Velociraptor();

        d.setPosicaoX(getPosicaoX());
        d.setPosicaoY(getPosicaoY());
        d.setSaude(getSaude());

        return d;
    }

    @Override
    public char getSimbolo() { return Macros.SIMB_VELOCIRAPTOR; }

    @Override
    public boolean podeSerAtacadoComDardos() { return false; }
}