package Itens;

import Entidades.Dinossauros.Dinossauro;
import Entidades.Jogador;
import Util.Macros;

public class ArmaDardos extends Item {

    @Override
    public String getNome() {
        return "Arma de dardos";
    }

    @Override
    public int usar(Jogador jogador, Dinossauro dino) {
        if (!dino.podeSerAtacadoComDardos()) {
            System.out.println("O Velociraptor é ágil demais para os dardos!");
            return 0;
        }

        Item municao = jogador.pegarItem(MunicaoDardos.class);
        if (municao == null) {
            System.out.println("Você não tem munição de dardos!");
            return 0;
        }

        jogador.removerItem(municao);
        return Macros.DANO_DARDOS;
    }
}