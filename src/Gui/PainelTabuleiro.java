package Gui;

import Entidades.Entidade;
import Entidades.Personagens.Jogador;
import Sistema.Tabuleiro;
import Util.Macros;

import javax.swing.*;
import java.awt.*;

public class PainelTabuleiro extends JPanel {

    private Tabuleiro tabuleiro;
    private Jogador jogador;
    private boolean debug;

    public PainelTabuleiro() {
        setPreferredSize(new Dimension(500, 500));
        setBackground(Color.BLACK);
    }

    public void setDados(Tabuleiro tabuleiro, Jogador jogador, boolean debug) {
        this.tabuleiro = tabuleiro;
        this.jogador = jogador;
        this.debug = debug;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (tabuleiro == null || jogador == null) return;

        int tam = Math.min(getWidth(), getHeight()) / tabuleiro.getDimensao();
        int offX = (getWidth() - tam * tabuleiro.getDimensao()) / 2;
        int offY = (getHeight() - tam * tabuleiro.getDimensao()) / 2;

        boolean[][] visivel = debug ? null : tabuleiro.calcularVisibilidade(jogador);

        for (int x = 0; x < tabuleiro.getDimensao(); x++) {
            for (int y = 0; y < tabuleiro.getDimensao(); y++) {
                int px = offX + y * tam;
                int py = offY + x * tam;

                char simbolo;
                if (!debug && visivel != null && !visivel[x][y]) {
                    simbolo = '+';
                } else {
                    Entidade e = tabuleiro.getEntidade(x, y);
                    if (e == null) simbolo = '.';
                    else if (e.getSimbolo() == Macros.SIMB_PAREDE) simbolo = '#';
                    else simbolo = e.getSimbolo();
                }

                g.setColor(CarregadorImagens.corFundo(simbolo));
                g.fillRect(px, py, tam, tam);
                g.setColor(Color.BLACK);
                g.drawRect(px, py, tam, tam);

                Image img = CarregadorImagens.carregar(simbolo);
                if (img != null) {
                    g.drawImage(img, px, py, tam, tam, this);
                } else {
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, tam / 2));
                    g.drawString(String.valueOf(simbolo), px + tam / 3, py + tam * 2 / 3);
                }
            }
        }
    }
}
