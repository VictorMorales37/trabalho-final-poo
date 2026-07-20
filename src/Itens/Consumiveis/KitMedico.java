package Itens.Consumiveis;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.Item;
import Sistema.Menu;
import Util.Macros;

public class KitMedico extends Item implements Consumivel {
    @Override
    public String getNome() {
        return "Kit Médico";
    }

    @Override
    public int usar(Jogador jogador, Dinossauro dino, Menu menu) {
        jogador.receberCura(Macros.CURA);
        menu.mensagem("Você usou o kit médico e recuperou vida.");
        return 0;
    }

    @Override
    public boolean consumidoAposUso() {
        return true;
    }
}
