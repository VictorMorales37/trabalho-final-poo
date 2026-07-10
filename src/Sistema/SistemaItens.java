package Sistema;

import Entidades.*;
import Entidades.Itens.*;
import Entidades.Dinossauros.Compsognato;
import Entidades.Dinossauros.Dinossauro;

import java.util.ArrayList;

public class SistemaItens {
    private final SistemaCombate sistemaCombate;

    public SistemaItens(SistemaCombate sistemaCombate) {
        this.sistemaCombate = sistemaCombate;
    }

    public void abrirCaixa(Jogador jogador, Caixa caixa, ArrayList<Dinossauro> dinossauros,
                           Tabuleiro tabuleiro, Menu menu, LeitorDeInput leitorDeInput) {

        if (caixa.getItem() instanceof KitMedico) {
            System.out.println("Você encontrou Kit Médico");
            jogador.setKitsMedicos(jogador.getKitsMedicos() + 1);
        }
        else if (caixa.getItem() instanceof Bastao) {
            System.out.println("Você encontrou Bastão");
            jogador.setTemBastao();
        }
        else if (caixa.getItem() instanceof MunicaoDardos) {
            System.out.println("Você encontrou munição para arma de dardos");
            jogador.setArmaDardos(jogador.getArmaDardos() + 1);
        }
        else if (caixa.getCompsognato() != null) {
            System.out.println("Cuidado! Um compsognato estava atrás da caixa");
        }
    }
}