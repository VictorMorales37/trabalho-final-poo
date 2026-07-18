package Sistema;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Dinossauros.Velociraptor;

// Thread que move um dinossauro sozinho de tempos em tempos
public class ThreadDinossauro extends Thread {

    private final MovimentadorDino movimentador;
    private final Dinossauro dinossauro;
    private final int intervalo;
    private boolean rodando = true;

    public ThreadDinossauro(MovimentadorDino movimentador, Dinossauro dinossauro) {
        this.movimentador = movimentador;
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
                movimentador.moverUmDinossauro(dinossauro);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
