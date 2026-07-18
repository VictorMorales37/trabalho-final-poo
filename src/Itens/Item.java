package Itens;

import Entidades.Dinossauros.Dinossauro;
import Entidades.Jogador;

public abstract class Item {
    public abstract String getNome();
    public abstract int usar(Jogador jogador, Dinossauro dino);
}
