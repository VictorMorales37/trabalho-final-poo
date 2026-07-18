package Sistema;
import Entidades.*;
import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Util.Macros;

import java.util.ArrayList;

public class Tabuleiro {
    private final int dimensao;
    private final Entidade[][] grid;
    private Entidade[][] posicoesIniciais;

    public Tabuleiro(int dimensao) {
        this.dimensao = dimensao;
        grid = new Entidade[dimensao][dimensao];
        posicoesIniciais = new Entidade[dimensao][dimensao];
    }

    public void limpar() {
        for (int i = 0; i < dimensao; i++) {
            for (int j = 0; j < dimensao; j++) {
                grid[i][j] = null;
                posicoesIniciais[i][j] = null;
            }
        }
    }

    public void colocarEntidade(Entidade entidade) {
        grid[entidade.getPosicaoX()][entidade.getPosicaoY()] = entidade;
    }

    public Entidade[][] getGrid() {
        return grid;
    }

    public Entidade getEntidade(int x, int y) {
        return grid[x][y];
    }

    public void setGrid(Entidade[][] novoGrid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = novoGrid[i][j];
            }
        }
    }

    public Entidade[][] getPosicoesIniciais() {
        return posicoesIniciais;
    }

    public int getDimensao() {
        return dimensao;
    }

    public void salvarPosicoes() {
        posicoesIniciais = new Entidade[dimensao][dimensao];

        for (int i = 0; i < dimensao; i++) {
            System.arraycopy(grid[i], 0, posicoesIniciais[i], 0, dimensao);
        }
    }

    public void atualizar(Jogador j, ArrayList<Dinossauro> dinos, ArrayList<Caixa> caixas) {
        for (int x = 0; x < dimensao; x++) {
            for (int y = 0; y < dimensao; y++) {
                if (!(grid[x][y] instanceof Parede)) {
                    grid[x][y] = null;
                }
            }
        }

        for (Caixa caixa : caixas) {
            grid[caixa.getPosicaoX()][caixa.getPosicaoY()] = caixa;
        }

        for (Dinossauro dinossauro : dinos) {
            grid[dinossauro.getPosicaoX()][dinossauro.getPosicaoY()] = dinossauro;
        }

        if (j.getPosicaoX() != -1 && j.getPosicaoY() != -1) {
            grid[j.getPosicaoX()][j.getPosicaoY()] = j;
        }
    }

    public boolean[][] calcularVisibilidade(Jogador j) {
        boolean[][] visivel = new boolean[dimensao][dimensao];
        int x = j.getPosicaoX();
        int y = j.getPosicaoY();

        if (x == -1 || y == -1) return visivel;

        visivel[x][y] = true;

        // cima
        for (int cx = x - 1; cx >= 0; cx--) {
            visivel[cx][y] = true;
            if (grid[cx][y] != null && grid[cx][y].getSimbolo() != Macros.SIMB_JOGADOR) break;
        }

        // baixo
        for (int cx = x + 1; cx < dimensao; cx++) {
            visivel[cx][y] = true;
            if (grid[cx][y] != null && grid[cx][y].getSimbolo() != Macros.SIMB_JOGADOR) break;
        }

        // esquerda
        for (int cy = y - 1; cy >= 0; cy--) {
            visivel[x][cy] = true;
            if (grid[x][cy] != null && grid[x][cy].getSimbolo() != Macros.SIMB_JOGADOR) break;
        }

        // direita
        for (int cy = y + 1; cy < dimensao; cy++) {
            visivel[x][cy] = true;
            if (grid[x][cy] != null && grid[x][cy].getSimbolo() != Macros.SIMB_JOGADOR) break;
        }

        return visivel;
    }
}
