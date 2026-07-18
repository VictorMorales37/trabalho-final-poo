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

        int dim = tabuleiro.getDimensao();
        int tam = Math.min(getWidth(), getHeight()) / dim;
        int side = tam * dim;
        int offX = (getWidth() - side) / 2;
        int offY = (getHeight() - side) / 2;

        Image grama = CarregadorImagens.fundo(side);
        if (grama != null) {
            g.drawImage(grama, offX, offY, this);
        } else {
            g.setColor(CarregadorImagens.corFundo('.'));
            g.fillRect(offX, offY, side, side);
        }

        boolean[][] visivel = debug ? null : tabuleiro.calcularVisibilidade(jogador);

        for (int x = 0; x < dim; x++) {
            for (int y = 0; y < dim; y++) {
                int px = offX + y * tam;
                int py = offY + x * tam;

                char simbolo;
                if (!debug && visivel != null && !visivel[x][y]) {
                    simbolo = '+';
                } else {
                    Entidade e = tabuleiro.getEntidade(x, y);
                    if (e == null) continue;
                    if (e.getSimbolo() == Macros.SIMB_PAREDE) simbolo = '#';
                    else simbolo = e.getSimbolo();
                }

                Image img = CarregadorImagens.carregar(simbolo, tam);
                if (img != null) {
                    g.drawImage(img, px, py, this);
                } else {
                    g.setColor(CarregadorImagens.corFundo(simbolo));
                    g.fillRect(px, py, tam, tam);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Arial", Font.BOLD, Math.max(8, tam / 2)));
                    g.drawString(String.valueOf(simbolo), px + tam / 3, py + tam * 2 / 3);
                }
            }
        }
    }
}
