package Itens.Consumiveis;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.Item;
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
