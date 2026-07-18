package Itens;

import Entidades.Dinossauros.Dinossauro;
import Entidades.Jogador;

public class MunicaoDardos extends Item implements Consumivel {
    @Override
    public String getNome() {
        return "Munição de Dardos";
    }

    @Override
    public int usar(Jogador jogador, Dinossauro dino) {
        return 0;
    }

    @Override
    public boolean consumidoAposUso() {
        return true;
    }
}