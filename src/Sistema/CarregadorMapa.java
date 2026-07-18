package Sistema;

import Entidades.*;
import Entidades.Personagens.Dinossauros.*;
import Entidades.Personagens.Jogador;
import Itens.Bastao;
import Itens.Consumiveis.KitMedico;
import Itens.Consumiveis.MunicaoDardos;
import Util.Macros;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class CarregadorMapa {

    private final Random random;

    public CarregadorMapa(Random random) {
        this.random = random;
    }

    public void carregar(String caminho, Tabuleiro tabuleiro, Jogador jogador,
                         ArrayList<Dinossauro> dinossauros, ArrayList<Caixa> caixas) {
        tabuleiro.limpar();
        dinossauros.clear();
        caixas.clear();

        ArrayList<String> linhas = lerArquivo(caminho);
        if (linhas.size() != Macros.TAMANHO_TABULEIRO) {
            throw new IllegalArgumentException(
                    "Mapa deve ter " + Macros.TAMANHO_TABULEIRO + " linhas: " + caminho);
        }

        boolean jogadorEncontrado = false;

        for (int x = 0; x < Macros.TAMANHO_TABULEIRO; x++) {
            String linha = linhas.get(x);
            if (linha.length() != Macros.TAMANHO_TABULEIRO) {
                throw new IllegalArgumentException(
                        "Linha " + (x + 1) + " deve ter " + Macros.TAMANHO_TABULEIRO + " colunas: " + caminho);
            }

            for (int y = 0; y < Macros.TAMANHO_TABULEIRO; y++) {
                char c = linha.charAt(y);
                processarCelula(c, x, y, tabuleiro, jogador, dinossauros, caixas);

                if (c == Macros.SIMB_JOGADOR) {
                    if (jogadorEncontrado) {
                        throw new IllegalArgumentException("Mapa com mais de um jogador: " + caminho);
                    }
                    jogadorEncontrado = true;
                }
            }
        }

        if (!jogadorEncontrado) {
            throw new IllegalArgumentException("Mapa sem jogador (P): " + caminho);
        }
    }

    private ArrayList<String> lerArquivo(String caminho) {
        ArrayList<String> linhas = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                if (!linha.isBlank() && !linha.startsWith("//")) {
                    linhas.add(linha.trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler mapa: " + caminho, e);
        }
        return linhas;
    }

    private void processarCelula(char c, int x, int y, Tabuleiro tabuleiro, Jogador jogador,
                                 ArrayList<Dinossauro> dinossauros, ArrayList<Caixa> caixas) {
        switch (c) {
            case '.' -> { }
            case '#' -> {
                Parede p = new Parede();
                p.setPosicaoX(x);
                p.setPosicaoY(y);
                tabuleiro.colocarEntidade(p);
            }
            case Macros.SIMB_JOGADOR -> {
                jogador.setPosicaoX(x);
                jogador.setPosicaoY(y);
            }
            case Macros.SIMB_COMPSOGNATO -> adicionarDino(new Compsognato(), x, y, dinossauros);
            case Macros.SIMB_TROODONTE -> adicionarDino(new Troodonte(), x, y, dinossauros);
            case Macros.SIMB_VELOCIRAPTOR -> adicionarDino(new Velociraptor(), x, y, dinossauros);
            case Macros.SIMB_TREX -> adicionarDino(new TiranossauroRex(), x, y, dinossauros);
            case 'K' -> adicionarCaixa(new Caixa(new KitMedico()), x, y, caixas);
            case 'B' -> adicionarCaixa(new Caixa(new Bastao(random)), x, y, caixas);
            case 'M' -> adicionarCaixa(new Caixa(new MunicaoDardos()), x, y, caixas);
            case 'S' -> adicionarCaixa(new Caixa(new Compsognato()), x, y, caixas);
            default -> throw new IllegalArgumentException(
                    "Símbolo inválido '" + c + "' em (" + x + "," + y + ")");
        }
    }

    private void adicionarDino(Dinossauro d, int x, int y, ArrayList<Dinossauro> dinossauros) {
        d.setPosicaoX(x);
        d.setPosicaoY(y);
        dinossauros.add(d);
    }

    private void adicionarCaixa(Caixa c, int x, int y, ArrayList<Caixa> caixas) {
        c.setPosicaoX(x);
        c.setPosicaoY(y);
        caixas.add(c);
    }
}
