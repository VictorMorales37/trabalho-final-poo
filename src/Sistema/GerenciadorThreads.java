package Sistema;

import Entidades.Personagens.Dinossauros.Dinossauro;

import java.util.ArrayList;

// Cuida das threads de movimento dos dinossauros
public class GerenciadorThreads {

    private final ArrayList<ThreadDinossauro> threads = new ArrayList<>();
    private final Jogo jogo;

    public GerenciadorThreads(Jogo jogo) {
        this.jogo = jogo;
    }

    public void iniciarTodas(ArrayList<Dinossauro> dinossauros) {
        pararTodas();
        for (Dinossauro d : dinossauros) {
            iniciarUma(d);
        }
    }

    public void iniciarUma(Dinossauro d) {
        if (d.getVelocidade() <= 0) return; // T-Rex não anda
        for (ThreadDinossauro t : threads) {
            if (t.getDinossauro() == d) return;
        }
        ThreadDinossauro t = new ThreadDinossauro(jogo, d);
        threads.add(t);
        t.start();
    }

    public void pararUma(Dinossauro d) {
        for (int i = 0; i < threads.size(); i++) {
            if (threads.get(i).getDinossauro() == d) {
                threads.get(i).parar();
                threads.remove(i);
                return;
            }
        }
    }

    public void pararTodas() {
        for (ThreadDinossauro t : threads) {
            t.parar();
        }
        threads.clear();
    }
}
