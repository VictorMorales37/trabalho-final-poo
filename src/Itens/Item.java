package Itens;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;

public abstract class Item {
    public abstract String getNome();
    public abstract int usar(Jogador jogador, Dinossauro dino);
}
