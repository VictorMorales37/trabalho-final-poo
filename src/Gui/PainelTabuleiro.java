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
    private boolean debugMode;

    public PainelTabuleiro() {
        setBackground(new Color(50, 50, 50));
        setPreferredSize(new Dimension(500, 500));
    }

    public void atualizar(Tabuleiro tabuleiro, Jogador jogador, boolean debugMode) {
        this.tabuleiro = tabuleiro;
        this.jogador = jogador;
        this.debugMode = debugMode;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (tabuleiro == null || jogador == null) {
            desenharPlaceholder((Graphics2D) g);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int dim = tabuleiro.getDimensao();
        int tamanho = Math.min(getWidth(), getHeight()) / dim;
        int offsetX = (getWidth() - tamanho * dim) / 2;
        int offsetY = (getHeight() - tamanho * dim) / 2;

        boolean[][] visivel = debugMode ? null : tabuleiro.calcularVisibilidade(jogador);

        for (int x = 0; x < dim; x++) {
            for (int y = 0; y < dim; y++) {
                int px = offsetX + y * tamanho;
                int py = offsetY + x * tamanho;

                if (!debugMode && visivel != null && !visivel[x][y]) {
                    CarregadorImagens.desenharCelula(g2, px, py, tamanho,
                            CarregadorImagens.corFundo('+'), '+', Color.GRAY);
                    continue;
                }

                Entidade entidade = tabuleiro.getEntidade(x, y);
                char simbolo;
                if (entidade != null) {
                    simbolo = entidade.getSimbolo();
                    if (simbolo == Macros.SIMB_PAREDE) simbolo = '#';
                } else {
                    simbolo = '.';
                }

                CarregadorImagens.desenharCelula(g2, px, py, tamanho,
                        CarregadorImagens.corFundo(simbolo), simbolo,
                        CarregadorImagens.corLetra(simbolo));
            }
        }
    }

    private void desenharPlaceholder(Graphics2D g2) {
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 16));
        String msg = "Inicie ou carregue uma partida";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2);
    }
}
