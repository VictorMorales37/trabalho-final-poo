package Itens;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Sistema.Menu;

import java.util.Random;

public class Bastao extends Item {
    private final Random random;

    public Bastao(Random random) {
        this.random = random;
    }

    @Override
    public String getNome() {
        return "Bastão";
    }

    @Override
    public int usar(Jogador j, Dinossauro dino, Menu menu) {
        int acerto = random.nextInt(6) + 1;
        if (acerto == 6){ // crítico
            return 2;
        }
        else if (acerto == 1){ // falha
            return 0;
        }
        else{
            return 1;
        }
    }
}
