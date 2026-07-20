package Itens.Consumiveis;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.Item;
import Sistema.Menu;

public class MunicaoDardos extends Item implements Consumivel {
    @Override
    public String getNome() {
        return "Munição de Dardos";
    }

    @Override
    public int usar(Jogador jogador, Dinossauro dino, Menu menu) {
        return 0;
    }

    @Override
    public boolean consumidoAposUso() {
        return true;
    }
}