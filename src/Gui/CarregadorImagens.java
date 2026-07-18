package Gui;

import Util.Macros;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Carrega PNGs da pasta Imagens/. Se a imagem não existir, retorna null
 * e o painel desenha um placeholder com letra.
 */
public class CarregadorImagens {

    private static final Map<String, Image> cache = new HashMap<>();

    private static final Map<Character, String> ARQUIVOS = Map.of(
            Macros.SIMB_JOGADOR, "jogador.png",
            Macros.SIMB_COMPSOGNATO, "compsognato.png",
            Macros.SIMB_TROODONTE, "troodonte.png",
            Macros.SIMB_VELOCIRAPTOR, "velociraptor.png",
            Macros.SIMB_TREX, "trex.png",
            Macros.SIMB_PAREDE, "parede.png",
            Macros.SIMB_CAIXA, "caixa.png",
            '.', "grama.png",
            '+', "neblina.png"
    );

    public static Image obterImagem(char simbolo) {
        String arquivo = ARQUIVOS.getOrDefault(simbolo, simbolo + ".png");
        return obterImagemPorArquivo(arquivo);
    }

    public static Image obterImagemPorArquivo(String arquivo) {
        if (cache.containsKey(arquivo)) {
            return cache.get(arquivo);
        }

        File file = new File(Macros.PASTA_IMAGENS + arquivo);
        if (!file.exists()) {
            cache.put(arquivo, null);
            return null;
        }

        try {
            Image img = ImageIO.read(file);
            cache.put(arquivo, img);
            return img;
        } catch (IOException e) {
            cache.put(arquivo, null);
            return null;
        }
    }

    public static void desenharCelula(Graphics2D g, int x, int y, int tamanho,
                                       Color fundo, char simbolo, Color corLetra) {
        g.setColor(fundo);
        g.fillRect(x, y, tamanho, tamanho);

        Image img = obterImagem(simbolo);
        if (img != null) {
            g.drawImage(img, x, y, tamanho, tamanho, null);
            return;
        }

        g.setColor(Color.BLACK);
        g.drawRect(x, y, tamanho - 1, tamanho - 1);

        g.setColor(corLetra);
        g.setFont(new Font("Monospaced", Font.BOLD, Math.max(12, tamanho / 2)));
        FontMetrics fm = g.getFontMetrics();
        String texto = String.valueOf(simbolo);
        int tx = x + (tamanho - fm.stringWidth(texto)) / 2;
        int ty = y + (tamanho + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(texto, tx, ty);
    }

    public static Color corFundo(char simbolo) {
        return switch (simbolo) {
            case '#', '█' -> new Color(120, 120, 120);
            case '+' -> new Color(40, 40, 40);
            case '.' -> new Color(76, 175, 80);
            default -> new Color(129, 199, 132);
        };
    }

    public static Color corLetra(char simbolo) {
        return switch (simbolo) {
            case 'P' -> Color.WHITE;
            case 'C' -> new Color(33, 150, 243);
            case 'T' -> new Color(121, 85, 72);
            case 'V' -> new Color(56, 142, 60);
            case 'R' -> new Color(183, 28, 28);
            case 'X' -> new Color(141, 110, 99);
            default -> Color.BLACK;
        };
    }
}
