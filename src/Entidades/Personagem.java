package Entidades;

public abstract class Personagem extends Entidade {
    protected int saude;

    public int getSaude() { return saude; }
    public void setSaude(int saude) { this.saude = saude; }

    public boolean estaVivo() { return saude > 0; }
    public void receberDano(int dano) { saude -= dano; }
}
