package Sistema;

import Entidades.Caixa;
import Entidades.Personagens.Jogador;
import Entidades.Personagens.Dinossauros.Compsognato;
import Itens.Item;

import java.util.ArrayList;

public class SistemaItens {

    public Compsognato processarCaixaNaPosicao(Jogador jogador, ArrayList<Caixa> caixas) {
        Caixa caixaEncontrada = encontrarCaixa(jogador, caixas);
        if (caixaEncontrada == null) return null;

        Compsognato surpresa = abrirCaixa(jogador, caixaEncontrada);
        caixas.remove(caixaEncontrada);

        if (surpresa != null) {
            surpresa.setPosicaoX(caixaEncontrada.getPosicaoX());
            surpresa.setPosicaoY(caixaEncontrada.getPosicaoY());
        }
        return surpresa;
    }

    private Caixa encontrarCaixa(Jogador jogador, ArrayList<Caixa> caixas) {
        for (Caixa c : caixas) {
            if (c.getPosicaoX() == jogador.getPosicaoX() && c.getPosicaoY() == jogador.getPosicaoY()) {
                return c;
            }
        }
        return null;
    }

    private Compsognato abrirCaixa(Jogador jogador, Caixa caixa) {
        if (caixa.getItem() != null) {
            Item item = caixa.getItem();
            jogador.receberItem(item);
            return null;
        } else {
            return caixa.getCompsognato();
        }
    }
}