package Itens;

public interface Consumivel {
    /**
     * Retorna true se o item deve ser removido do inventário após o uso.
     */
    boolean consumidoAposUso();
}
