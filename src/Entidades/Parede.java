package Entidades;

import Util.Macros;

public class Parede extends Entidade {

    public Parede() {
        super(Macros.SIMB_PAREDE);
    }

    @Override
    public char getSimbolo() {
        return Macros.SIMB_PAREDE;
    }
}
