package Sistema;

import Entidades.Personagens.Dinossauros.Dinossauro;

// Quem recebe o pedido de movimento automático do dinossauro
public interface MovimentadorDino {
    void moverUmDinossauro(Dinossauro d);
}
