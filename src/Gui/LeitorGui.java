package Gui;

import Sistema.LeitorDeInput;
import Util.Direcao;

import java.util.concurrent.CountDownLatch;

public class LeitorGui extends LeitorDeInput {

    private CountDownLatch latch = new CountDownLatch(1);
    private int escolha = 1;

    public LeitorGui() {
        super(null);
    }

    public void escolher(int valor) {
        escolha = valor;
        latch.countDown();
    }

    @Override
    public int lerInput(int min, int max) {
        latch = new CountDownLatch(1);
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return escolha;
    }

    @Override
    public Direcao lerDirecao(int input) {
        return super.lerDirecao(input);
    }
}
