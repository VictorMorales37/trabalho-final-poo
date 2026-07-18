package Sistema;
import Entidades.Entidade;
import Sistema.Movimentacao.ResultadoMovimento;
import Entidades.Jogador;
import Util.Macros;
import Entidades.Dinossauros.Dinossauro;

public class Menu {
    public void menuInicial() {
        System.out.println("▄█████  ▄▄▄  ▄▄▄▄  ▄▄▄▄  ▄▄▄▄▄ ▄▄ ▄▄ ▄▄ ▄▄ ▄▄ ▄▄▄▄▄ ▄▄  ▄▄  ▄▄▄▄ ▄▄  ▄▄▄       ██ ▄▄ ▄▄ ▄▄▄▄   ▄▄▄   ▄▄▄▄  ▄▄▄▄ ▄▄  ▄▄▄▄  ▄▄▄");
        System.out.println("▀▀▀▄▄▄ ██▀██ ██▄██ ██▄█▄ ██▄▄  ██▄██ ██ ██▄██ ██▄▄  ███▄██ ██▀▀▀ ██ ██▀██      ██ ██ ██ ██▄█▄ ██▀██ ███▄▄ ███▄▄ ██ ██▀▀▀ ██▀██");
        System.out.println("█████▀ ▀███▀ ██▄█▀ ██ ██ ██▄▄▄  ▀█▀  ██  ▀█▀  ██▄▄▄ ██ ▀██ ▀████ ██ ██▀██   ████▀ ▀███▀ ██ ██ ██▀██ ▄▄██▀ ▄▄██▀ ██ ▀████ ██▀██");

        System.out.println("Boas vindas, jogador!");
        System.out.println("1- Novo jogo");
        System.out.println("2- Sair");
    }
    public void menuPrincipal() {
        System.out.println("1- Movimentar");
        System.out.println("2- Cura");
        System.out.println("3- Modo DEBUG");
        System.out.println("4- Sair");
    }
    public void menuSaida() {
        System.out.println("1- Reiniciar Jogo");
        System.out.println("2- Novo jogo");
        System.out.println("3- Sair");
    }
    public void escolherDificuldade() {
        System.out.println("Selecione Dificuldade:");
        System.out.println("1- Fácil");
        System.out.println("2- Médio");
        System.out.println("3- Difícil");
    }
    public void statusJogador(Jogador j) {
        System.out.println("Saude: " + j.getSaude() + "/" + Macros.SAUDE_JOGADOR);
        System.out.println("Percepção: " + j.getPercepcao());
    }
    public void mostrarTabuleiroDEBUG(Tabuleiro t) {
        for (int x = 0; x < t.getDimensao(); x++) {
            for (int y = 0; y < t.getDimensao(); y++) {
                char simb = t.getEntidade(x, y) != null ? t.getEntidade(x, y).getSimbolo() : '.';
                System.out.print(simb + " ");
            }
            System.out.println();
        }
    }
    public void mostrarTabuleiroJogador(Tabuleiro t, Jogador j) {
        boolean[][] visivel = t.calcularVisibilidade(j);

        for (int x = 0; x < t.getDimensao(); x++) {
            for (int y = 0; y < t.getDimensao(); y++) {
                if (visivel[x][y]) {
                    Entidade entidade = t.getEntidade(x, y);
                    char simb;
                    if (entidade != null) {
                        simb = entidade.getSimbolo();
                    } else {
                        simb = '.';
                    }
                    System.out.print(simb + " ");
                } else {
                    System.out.print(Macros.SIMB_MISTERIO);
                }
            }
            System.out.println();
        }
    }
    public void opcoesMovimento() {
        System.out.println("1 - ^");
        System.out.println("2 - <");
        System.out.println("3 - >");
        System.out.println("4 - v");
        System.out.println("5 - Voltar");
    }
    public void opcoesCombate(Jogador j) {
        System.out.println("Saude atual: " + j.getSaude() + " / " + Macros.SAUDE_JOGADOR + "\n");
        System.out.println("Escolha opção de ação:");
        System.out.println("1- Atacar com as Mãos");
        System.out.println("2- Atacar com Bastão");
        System.out.println("3- Atacar com Dardos");
        System.out.println("4- Curar");
        System.out.println("5- Fugir do combate");
    }
    public void avisoMovimento(ResultadoMovimento resultado) {
        if ( resultado == ResultadoMovimento.BLOQUEADO){
            System.out.println("Movimento não permitido");
        }
        else if (resultado == ResultadoMovimento.ENCONTROU_COMPSOGNATO){
            System.out.println("Encontrou compsognato!");
        }
        else if (resultado == ResultadoMovimento.ENCONTROU_TROODONTE){
            System.out.println("Encontrou troodonte!");
        }
        else if (resultado == ResultadoMovimento.ENCONTROU_VELOCIRAPTOR){
            System.out.println("Encontrou velociraptor!");
        }
        else if (resultado == ResultadoMovimento.ENCONTROU_TREX){
            System.out.println("Encontrou Tiranossauro Rex!");
        }
    }

    public void avisoDinossauroEncontrou(Dinossauro d) {
        System.out.println("Um " + d.getClass().getSimpleName() + " te encontrou!");
    }

    public void mensagemVitoria() {
        System.out.println("Você EXTINGUIU os dinossauros!");
    }
    public void mensagemDerrota() {
        System.out.println("Você MORREU!");
    }
    public void mensagemSaida() {
        System.out.println("Saindo do jogo...");
    }
}
