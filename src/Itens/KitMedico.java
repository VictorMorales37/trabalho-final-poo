package Itens;

import Entidades.Dinossauros.Dinossauro;
import Entidades.Jogador;
import Util.Macros;

public class KitMedico extends Item implements Consumivel {
    @Override
    public String getNome() {
        return "Kit Médico";
    }

    @Override
    public int usar(Jogador jogador, Dinossauro dino) {
        jogador.receberCura(Macros.CURA);
        System.out.println("Você usou o kit médico e recuperou vida.");
        return 0;
    }

    @Override
    public boolean consumidoAposUso() {
        return true;
    }
}
