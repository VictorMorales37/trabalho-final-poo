package Sistema;

import Entidades.*;
import Entidades.Personagens.Dinossauros.*;
import Entidades.Personagens.Jogador;
import Itens.*;
import Itens.Consumiveis.KitMedico;
import Itens.Consumiveis.MunicaoDardos;
import Util.Macros;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class SistemaSalvamento {

    private final Random randomGenerator;

    public SistemaSalvamento(Random randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public void salvar(String caminho, Jogador jogador, ArrayList<Dinossauro> dinossauros,
                       ArrayList<Caixa> caixas, Tabuleiro tabuleiro) {
        File file = new File(caminho);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("saude=" + jogador.getSaude());
            writer.newLine();
            writer.write("percepcao=" + jogador.getPercepcao());
            writer.newLine();
            writer.write("itens=" + serializeItems(jogador));
            writer.newLine();

            char[][] mapChars = buildMap(jogador, dinossauros, caixas, tabuleiro);
            for (int x = 0; x < Macros.TAMANHO_TABULEIRO; x++) {
                writer.write(mapChars[x]);
                writer.newLine();
            }

            for (Dinossauro d : dinossauros) {
                writer.write(d.getSimbolo() + " " + d.getPosicaoX() + " " + d.getPosicaoY() + " " + d.getSaude());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar jogo: " + caminho, e);
        }
    }

    public void carregar(String caminho, Tabuleiro tabuleiro, Jogador jogador,
                         ArrayList<Dinossauro> dinossauros, ArrayList<Caixa> caixas) {
        ArrayList<String> lines = readLines(caminho);
        int index = 0;

        int health = Macros.SAUDE_JOGADOR;
        int perception = Macros.PERCEPCAO_INICIAL;
        String serializedItems = "ArmaDardos";

        // cabeçalho (opcional) até a primeira linha do mapa
        while (index < lines.size() && !isMapLine(lines.get(index))) {
            String line = lines.get(index);
            if (line.startsWith("saude=")) {
                health = Integer.parseInt(line.substring("saude=".length()).trim());
            } else if (line.startsWith("percepcao=")) {
                perception = Integer.parseInt(line.substring("percepcao=".length()).trim());
            } else if (line.startsWith("itens=")) {
                serializedItems = line.substring("itens=".length()).trim();
            }
            index++;
        }

        if (index + Macros.TAMANHO_TABULEIRO > lines.size()) {
            throw new IllegalArgumentException("Save incompleto (mapa ausente): " + caminho);
        }

        ArrayList<String> mapLines = new ArrayList<>();
        for (int i = 0; i < Macros.TAMANHO_TABULEIRO; i++) mapLines.add(lines.get(index + i));
        index += Macros.TAMANHO_TABULEIRO;

        tabuleiro.limpar();
        dinossauros.clear();
        caixas.clear();

        boolean playerFound = false;
        for (int x = 0; x < Macros.TAMANHO_TABULEIRO; x++) {
            String mapLine = mapLines.get(x);
            if (mapLine.length() != Macros.TAMANHO_TABULEIRO) {
                throw new IllegalArgumentException("Linha de mapa inválida no save: " + caminho);
            }
            for (int y = 0; y < Macros.TAMANHO_TABULEIRO; y++) {
                char c = mapLine.charAt(y);
                processCell(c, x, y, tabuleiro, jogador, dinossauros, caixas);
                if (c == Macros.SIMB_JOGADOR) {
                    if (playerFound) throw new IllegalArgumentException("Save com mais de um jogador: " + caminho);
                    playerFound = true;
                }
            }
        }

        if (!playerFound) throw new IllegalArgumentException("Save sem jogador (P): " + caminho);

        jogador.setSaude(health);
        jogador.setPercepcao(perception);
        jogador.limparInventario();
        for (Item item : deserializeItems(serializedItems)) jogador.receberItem(item);

        // linhas adicionais: dinossauros com hp customizado
        while (index < lines.size()) {
            String[] parts = lines.get(index).trim().split("\\s+");
            if (parts.length == 4) {
                char symbol = parts[0].charAt(0);
                int dx = Integer.parseInt(parts[1]);
                int dy = Integer.parseInt(parts[2]);
                int hp = Integer.parseInt(parts[3]);
                for (Dinossauro d : dinossauros) {
                    if (d.getSimbolo() == symbol && d.getPosicaoX() == dx && d.getPosicaoY() == dy) {
                        d.setSaude(hp);
                        break;
                    }
                }
            }
            index++;
        }
    }

    public boolean existeSave(String caminho) {
        return new File(caminho).isFile();
    }

    // --- auxiliares ---
    private char[][] buildMap(Jogador jogador, ArrayList<Dinossauro> dinossauros,
                              ArrayList<Caixa> caixas, Tabuleiro tabuleiro) {
        char[][] map = new char[Macros.TAMANHO_TABULEIRO][Macros.TAMANHO_TABULEIRO];

        for (int x = 0; x < Macros.TAMANHO_TABULEIRO; x++) {
            for (int y = 0; y < Macros.TAMANHO_TABULEIRO; y++) {
                Entidade e = tabuleiro.getEntidade(x, y);
                map[x][y] = (e instanceof Parede) ? '#' : '.';
            }
        }

        for (Caixa caixa : caixas) map[caixa.getPosicaoX()][caixa.getPosicaoY()] = boxSymbol(caixa);
        for (Dinossauro dino : dinossauros) map[dino.getPosicaoX()][dino.getPosicaoY()] = dino.getSimbolo();
        map[jogador.getPosicaoX()][jogador.getPosicaoY()] = Macros.SIMB_JOGADOR;
        return map;
    }

    private char boxSymbol(Caixa caixa) {
        if (caixa.getCompsognato() != null) return 'S';
        Item item = caixa.getItem();
        if (item instanceof KitMedico) return 'K';
        if (item instanceof Bastao) return 'B';
        if (item instanceof MunicaoDardos) return 'M';
        return 'K';
    }

    private String serializeItems(Jogador jogador) {
        StringBuilder sb = new StringBuilder();
        for (Item item : jogador.getInventario().getItens()) {
            if (sb.length() > 0) sb.append(',');
            sb.append(item.getClass().getSimpleName());
        }
        return sb.toString();
    }

    private ArrayList<Item> deserializeItems(String serialized) {
        ArrayList<Item> items = new ArrayList<>();
        if (serialized == null || serialized.isBlank()) return items;

        for (String nome : serialized.split(",")) {
            String tipo = nome.trim();
            if (tipo.isEmpty()) continue;
            Item item = switch (tipo) {
                case "ArmaDardos" -> new ArmaDardos();
                case "Bastao" -> new Bastao(randomGenerator);
                case "KitMedico" -> new KitMedico();
                case "MunicaoDardos" -> new MunicaoDardos();
                default -> null;
            };
            if (item != null) items.add(item);
        }
        return items;
    }

    private void processCell(char c, int x, int y, Tabuleiro tabuleiro, Jogador jogador,
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
            case Macros.SIMB_COMPSOGNATO -> addDino(new Compsognato(), x, y, dinossauros);
            case Macros.SIMB_TROODONTE -> addDino(new Troodonte(), x, y, dinossauros);
            case Macros.SIMB_VELOCIRAPTOR -> addDino(new Velociraptor(), x, y, dinossauros);
            case Macros.SIMB_TREX -> addDino(new TiranossauroRex(), x, y, dinossauros);
            case 'K' -> addBox(new Caixa(new KitMedico()), x, y, caixas);
            case 'B' -> addBox(new Caixa(new Bastao(randomGenerator)), x, y, caixas);
            case 'M' -> addBox(new Caixa(new MunicaoDardos()), x, y, caixas);
            case 'S' -> addBox(new Caixa(new Compsognato()), x, y, caixas);
            default -> throw new IllegalArgumentException("Símbolo inválido '" + c + "' em (" + x + "," + y + ")");
        }
    }

    private void addDino(Dinossauro d, int x, int y, ArrayList<Dinossauro> dinossauros) {
        d.setPosicaoX(x);
        d.setPosicaoY(y);
        dinossauros.add(d);
    }

    private void addBox(Caixa c, int x, int y, ArrayList<Caixa> caixas) {
        c.setPosicaoX(x);
        c.setPosicaoY(y);
        caixas.add(c);
    }

    private boolean isMapLine(String line) {
        if (line.length() != Macros.TAMANHO_TABULEIRO) return false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if ("#.PCTVRKBMS".indexOf(c) < 0) return false;
        }
        return true;
    }

    private ArrayList<String> readLines(String caminho) {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(caminho))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank() && !line.startsWith("//")) {
                    lines.add(line.trim());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar save: " + caminho, e);
        }
        return lines;
    }
}
