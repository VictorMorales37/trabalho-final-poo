package Entidades;

import Entidades.Dinossauros.Compsognato;
import Itens.Item;
import Util.Macros;

public class Caixa extends Entidade {
    private Item item;
    private Compsognato compsognato;

    public Caixa(Item item) {
        this.item = item;
    }

    public Caixa(Compsognato compsognato) {
        this.compsognato = compsognato;
    }

    public Item getItem() {
        return item;
    }

    public Compsognato getCompsognato() {
        return compsognato;
    }

    public Caixa copia() {
        Caixa c;
        if (item != null) {
            c = new Caixa(item); 
        } else {
            Compsognato copia;
            if (compsognato != null) {
                copia = (Compsognato) compsognato.copia();
            } else {
                copia = null;
            }
            c = new Caixa(copia);
        }

        c.setPosicaoX(getPosicaoX());
        c.setPosicaoY(getPosicaoY());

        return c;
    }

    @Override
    public char getSimbolo() { return Macros.SIMB_CAIXA; }
}