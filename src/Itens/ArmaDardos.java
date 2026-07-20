package Itens;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.Consumiveis.MunicaoDardos;
import Sistema.Menu;
import Util.Macros;

public class ArmaDardos extends Item {

    @Override
    public String getNome() {
        return "Arma de dardos";
    }

    @Override
    public int usar(Jogador jogador, Dinossauro dino, Menu menu) {
        if (!dino.podeSerAtacadoComDardos()) {
            menu.mensagem("O Velociraptor é ágil demais para os dardos!");
            return 0;
        }

        Item municao = jogador.pegarItem(MunicaoDardos.class);
        if (municao == null) {
            menu.mensagem("Você não tem munição de dardos!");
            return 0;
        }

        jogador.removerItem(municao);
        return Macros.DANO_DARDOS;
    }
}