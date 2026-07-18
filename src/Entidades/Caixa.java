package Entidades;

import Entidades.Personagens.Dinossauros.Compsognato;
import Itens.Item;
import Util.Macros;

public class Caixa extends Entidade {
    private Item item;
    private Compsognato compsognato;

    public Caixa(Item item) {
        super(Macros.SIMB_CAIXA);
        this.item = item;
        this.compsognato = null;
    }

    public Caixa(Compsognato compsognato) {
        super(Macros.SIMB_CAIXA);
        this.compsognato = compsognato;
        this.item = null;
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
    public char getSimbolo() { 
        return Macros.SIMB_CAIXA; 
    }
}