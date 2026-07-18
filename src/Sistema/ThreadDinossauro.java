package Sistema;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Dinossauros.Velociraptor;

// Thread que move um dinossauro periodicamente
public class ThreadDinossauro extends Thread {

    private final Jogo jogo;
    private final Dinossauro dinossauro;
    private final int intervalo;
    private boolean rodando = true;

    public ThreadDinossauro(Jogo jogo, Dinossauro dinossauro) {
        this.jogo = jogo;
        this.dinossauro = dinossauro;
        if (dinossauro instanceof Velociraptor) {
            this.intervalo = 1000;
        } else {
            this.intervalo = 2000;
        }
    }

    public Dinossauro getDinossauro() {
        return dinossauro;
    }

    public void parar() {
        rodando = false;
        interrupt();
    }

    @Override
    public void run() {
        while (rodando) {
            try {
                Thread.sleep(intervalo);
                if (!rodando) break;
                jogo.moverUmDinossauro(dinossauro);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
