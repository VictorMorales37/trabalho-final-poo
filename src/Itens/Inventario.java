package Itens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inventario {
    private final List<Item> itens = new ArrayList<>();

    public void adicionar(Item item) {
        itens.add(item);
    }

    public Item pegar(Class<?> tipo) {
        for (Item item : itens) {
            if (tipo.isInstance(item)) {
                return item;
            }
        }
        return null;
    }

    public void remover(Item item) {
        itens.remove(item);
    }

    public void limpar() {
        itens.clear();
    }

    public List<Item> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
