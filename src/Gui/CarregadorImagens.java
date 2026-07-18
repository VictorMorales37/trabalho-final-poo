package Gui;

import Util.Macros;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CarregadorImagens {

    private static final Map<String, Image> ORIGINAIS = new HashMap<>();
    private static final Map<String, Image> ESCALADAS = new HashMap<>();
    private static int ultimoTamCelula = -1;
    private static int ultimoLadoFundo = -1;
    private static Image fundoEscalado;

    public static Image fundo(int lado) {
        if (fundoEscalado != null && lado == ultimoLadoFundo) return fundoEscalado;

        Image original = porArquivo("grama.png");
        if (original == null) {
            fundoEscalado = null;
            return null;
        }

        fundoEscalado = escalar(original, lado, lado);
        ultimoLadoFundo = lado;
        return fundoEscalado;
    }

    public static Image carregar(char simbolo, int tam) {
        String nome = nomeArquivo(simbolo);
        if (nome == null) return null;

        if (tam != ultimoTamCelula) {
            ESCALADAS.clear();
            ultimoTamCelula = tam;
        }

        String chave = nome + "@" + tam;
        if (ESCALADAS.containsKey(chave)) return ESCALADAS.get(chave);

        Image original = porArquivo(nome);
        Image escalada = original == null ? null : escalar(original, tam, tam);
        ESCALADAS.put(chave, escalada);
        return escalada;
    }

    private static String nomeArquivo(char simbolo) {
        return switch (simbolo) {
            case 'P' -> "jogador.png";
            case 'C' -> "compsognato.png";
            case 'T' -> "troodonte.png";
            case 'V' -> "velociraptor.png";
            case 'R' -> "trex.png";
            case 'X' -> "caixa.png";
            case '#', '█' -> "parede.png";
            case '+' -> "neblina.png";
            default -> null;
        };
    }

    private static Image porArquivo(String nome) {
        if (ORIGINAIS.containsKey(nome)) return ORIGINAIS.get(nome);

        File arquivo = new File(Macros.PASTA_IMAGENS + nome);
        Image img = null;
        if (arquivo.exists()) {
            try {
                img = ImageIO.read(arquivo);
            } catch (Exception ignored) {
            }
        }
        ORIGINAIS.put(nome, img);
        return img;
    }

    private static Image escalar(Image original, int w, int h) {
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(original, 0, 0, w, h, null);
        g2.dispose();
        return dest;
    }

    public static Color corFundo(char simbolo) {
        if (simbolo == '#' || simbolo == '█') return Color.GRAY;
        if (simbolo == '+') return Color.DARK_GRAY;
        if (simbolo == '.') return new Color(76, 175, 80);
        return new Color(129, 199, 132);
    }
}
