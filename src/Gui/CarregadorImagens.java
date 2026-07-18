package Gui;

import Util.Macros;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;

public class CarregadorImagens {

    // tenta carregar png da pasta Imagens/
    public static Image carregar(char simbolo) {
        String nome = switch (simbolo) {
            case 'P' -> "jogador.png";
            case 'C' -> "compsognato.png";
            case 'T' -> "troodonte.png";
            case 'V' -> "velociraptor.png";
            case 'R' -> "trex.png";
            case 'X' -> "caixa.png";
            case '#', '█' -> "parede.png";
            case '+' -> "neblina.png";
            default -> "grama.png";
        };

        File arquivo = new File(Macros.PASTA_IMAGENS + nome);
        if (!arquivo.exists()) return null;

        try {
            return ImageIO.read(arquivo);
        } catch (Exception e) {
            return null;
        }
    }

    public static Color corFundo(char simbolo) {
        if (simbolo == '#' || simbolo == '█') return Color.GRAY;
        if (simbolo == '+') return Color.DARK_GRAY;
        if (simbolo == '.') return new Color(76, 175, 80);
        return new Color(129, 199, 132);
    }
}
