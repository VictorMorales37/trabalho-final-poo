package Sistema;

import Util.Direcao;

import java.util.InputMismatchException;
import java.util.Scanner;

public class LeitorDeInput {
    private final Scanner scanner;

    public LeitorDeInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public int lerInput(int min, int max) {
        while (true) {
            try {
                int valor = scanner.nextInt();
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("Digite um valor entre " + min + " e " + max + ".");
            } catch (InputMismatchException e) {
                System.out.println("Digite valor valido: (valores entre " + min + " e " + max + ")");
                scanner.nextLine();
            }
        }
    }

    public Direcao lerDirecao(int input) {
        return switch (input) {
            case 1 -> Direcao.CIMA;
            case 2 -> Direcao.ESQUERDA;
            case 3 -> Direcao.DIREITA;
            case 4 -> Direcao.BAIXO;
            default -> Direcao.INVALIDA;
        };
    }
}
