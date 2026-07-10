package Sistema;
import Entidades.*;
import Entidades.Dinossauros.Dinossauro;
import Util.Macros;

import java.util.ArrayList;

public class Tabuleiro {
    private final int dimensao;
    private Entidade[][] grid;
    private Entidade[][] posicoesIniciais;
    private final boolean[][] posicoesOcupadas;
    private final ArrayList<int[]> paredes; // mantido para spawn, mas poderíamos instanciar paredes diretas

    public Tabuleiro(int dimensao) {
        this.dimensao = dimensao;
        grid = new Entidade[dimensao][dimensao];
        posicoesIniciais = new Entidade[dimensao][dimensao];
        posicoesOcupadas = new boolean[dimensao][dimensao];
        paredes = new ArrayList<>();
    }

    public Entidade[][] getGrid() {
        return grid;
    }

    public Entidade getEntidade(int x, int y) {
        return grid[x][y];
    }

    public void setGrid(Entidade[][] novoGrid) {
        for (int i = 0; i < grid.length; i++) {
            System.arraycopy(novoGrid[i], 0, grid[i], 0, grid[i].length);
        }
    }

    public Entidade[][] getPosicoesIniciais() {
        return posicoesIniciais;
    }

    public int getDimensao() {
        return dimensao;
    }

    public ArrayList<int[]> getParedes() {
        return paredes;
    }

    public boolean verificarPosicao(int x, int y) {
        if (x < 0 || x > dimensao - 1) return false;
        if (y < 0 || y > dimensao - 1) return false;

        for (int i = 0; i < dimensao; i++) {
            for (int j = 0; j < dimensao; j++) {
                if (i == x && j == y && posicoesOcupadas[i][j]) return false;
            }
        }
        return true;
    }

    public void salvarPosicoes() {
        posicoesIniciais = new Entidade[dimensao][dimensao];

        for (int i = 0; i < dimensao; i++) {
            System.arraycopy(grid[i], 0, posicoesIniciais[i], 0, dimensao);
        }
    }

    public void setPosicoesOcupadas(int x, int y) {
        posicoesOcupadas[x][y] = true;
    }

    public void atualizar(Jogador j, ArrayList<Dinossauro> dinos, ArrayList<Caixa> caixas) {
        for (int x = 0; x < dimensao; x++) {
            for (int y = 0; y < dimensao; y++) {
                grid[x][y] = null;
            }
        }

        // Add Paredes
        for (int[] parede : paredes) {
            Parede p = new Parede();
            p.setPosicaoX(parede[0]);
            p.setPosicaoY(parede[1]);
            grid[parede[0]][parede[1]] = p;
        }
        
        // Add Caixas
        for (Caixa caixa : caixas) {
            grid[caixa.getPosicaoX()][caixa.getPosicaoY()] = caixa;
        }

        // Add Dinos
        for (Dinossauro dinossauro : dinos) {
            grid[dinossauro.getPosicaoX()][dinossauro.getPosicaoY()] = dinossauro;
        }
        
        // Add Jogador
        if(j.getPosicaoX() != -1 && j.getPosicaoY() != -1) {
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
