package Util;

/**
 * Contrato para entidades que sabem produzir uma cópia profunda de si mesmas.
 * Usado para restaurar o estado inicial da partida (reinício) sem
 * compartilhar referências com os objetos originais.
 */
public interface Copiavel<T> {
    T copia();
}
