package Gui;

import Entidades.Personagens.Jogador;
import Sistema.Menu;

import javax.swing.*;

public class MenuGui extends Menu {

    private JTextArea area;
    private DialogoCombate dialogo;

    public void setArea(JTextArea area) {
        this.area = area;
    }

    public void setDialogo(DialogoCombate dialogo) {
        this.dialogo = dialogo;
    }

    @Override
    public void mensagem(String texto) {
        if (dialogo != null) {
            dialogo.adicionarLog(texto);
        } else if (area != null) {
            area.append(texto + "\n");
        } else {
            super.mensagem(texto);
        }
    }

    @Override
    public void opcoesCombate(Jogador j) {
        if (dialogo != null) {
            dialogo.atualizarHp(j);
        } else {
            super.opcoesCombate(j);
        }
    }
}
