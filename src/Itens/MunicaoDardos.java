package Itens;

import Entidades.Dinossauros.Dinossauro;
import Entidades.Jogador;

public class MunicaoDardos extends Item {
    @Override
    public String getNome() {
        return "Munição de Dardos";
    }

    @Override
    public int usar(Jogador jogador, Dinossauro dino) {
        return 0;
    }
}