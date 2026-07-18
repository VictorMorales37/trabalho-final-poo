package Gui;

import Sistema.LeitorDeInput;
import Util.Direcao;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LeitorGui extends LeitorDeInput {

    private final BlockingQueue<Integer> fila = new LinkedBlockingQueue<>();

    public LeitorGui() {
        super(null);
    }

    public void escolher(int valor) {
        fila.offer(valor);
    }

    @Override
    public int lerInput(int min, int max) {
        fila.clear(); // ignora cliques antigos
        try {
            int valor = fila.take();
            if (valor < min || valor > max) return min;
            return valor;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return min;
        }
    }

    @Override
    public Direcao lerDirecao(int input) {
        return super.lerDirecao(input);
    }
}
