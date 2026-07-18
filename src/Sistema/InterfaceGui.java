package Sistema;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;

public interface InterfaceGui {
    void atualizar();
    void onVitoria();
    void onDerrota();
    void prepararCombate(Jogador jogador, Dinossauro dinossauro);
    void finalizarCombate();
}
