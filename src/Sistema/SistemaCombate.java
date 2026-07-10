package Sistema;
import Entidades.Dinossauros.Dinossauro;
import Entidades.Jogador;
import Util.Macros;
import java.util.Random;

public class SistemaCombate {
    private final Random random;

    public SistemaCombate(Random random) {
        this.random = random;
    }
    // ----------------------------- ataque -----------------------------
    public int atacarMao(){
        int acerto = random.nextInt(6) + 1;

        if (acerto == 6){ // crítico
            return 2;
        }
        else if (acerto == 1 || acerto == 2){ // falha
            return 0;
        }
        else return 1;
    }
    public int atacarBastao(Jogador j) {
        if (j.getTemBastao()) {
            int acerto = random.nextInt(6) + 1;
            if (acerto == 6){ // crítico
                return 2;
            }
            else if (acerto == 1){ // falha
                return 0;
            }
            else{
                return 1;
            }
        } else {
            System.out.println("Você ainda não tem bastão.");
            return 0;
        }
    }
    public int atacarArmaDardos(Jogador j){
        if (j.getArmaDardos() > 0){
            j.setArmaDardos(j.getArmaDardos() - 1);
            return Macros.DANO_DARDOS;
        }
        else {
            System.out.println("Você não possui dardos para usar.");
            return 0;
        }
    }

    public ResultadoCombate combate(Jogador jogador, Dinossauro dino, Menu menu,
                                             LeitorDeInput leitorDeInput, Tabuleiro tabuleiro) {

        while (jogador.getSaude() > 0 && dino.getSaude() > 0) {
            menu.opcoesCombate(jogador); // 1 - mão | 2 - bastão | 3 - dardos | 4 - curar | 5 - fugir
            int input = leitorDeInput.lerInput(1, 5);

            int dano = switch (input) {
                case 1:
                    if (!dino.podeSerAtacadoSemArma()) {
                        System.out.println("Não é possível atacar o T-Rex sem armas!");
                        yield 0;
                    }
                    yield atacarMao();

                case 2:
                    yield atacarBastao(jogador);

                case 3:
                    if (!dino.podeSerAtacadoComDardos()) {
                        System.out.println("O Velociraptor é ágil demais para os dardos!");
                        yield 0;
                    }
                    yield atacarArmaDardos(jogador);

                case 4:
                    curar(jogador); yield 0;

                case 5:
                    fugir(jogador, tabuleiro); yield -1;

                default:
                    yield 0;
            };

            // ------------------------------ dano do ataque do jogador ------------------------------

            if (dano == 0) System.out.println("Ataque falhou");

            else if (dano == -1) {
                System.out.println("Voce fugiu");
                return ResultadoCombate.FUGIU;
            }
            else {
                System.out.println("Voce atacou o dinossauro");
                System.out.println("Ele recebeu " + dano + " de dano\n");
            }

            dino.setSaude(dino.getSaude() - dano);
            if (dino.getSaude() <= 0) {
                System.out.println("Voce derrotou o dinossauro!");
                return ResultadoCombate.VENCEU;
            }

            // ------------------------------ ataque do dinossauro ------------------------------
            System.out.println("Cuidado!");
            System.out.println("O dinossauro está tentando te atacar...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            boolean passouTestePercepcao = passouTestePercepcao(jogador);
            if ( passouTestePercepcao ){
                System.out.println("Voce conseguiu desviar.");
            }
            if (!passouTestePercepcao && dino.getSimbolo() == Macros.SIMB_TREX) {
                System.out.println("T-REX TE ATACOU!!!");
                jogador.setSaude(jogador.getSaude() - 4);
            }
            else if (!passouTestePercepcao) {
                System.out.println("O dinossauro te atacou!");
                jogador.setSaude(jogador.getSaude() - dino.getDanoAtaque());
            }

            if ( jogador.getSaude() <= 0){
                return ResultadoCombate.PERDEU;
            }
        }
        return null;
    }

    // ----------------------------- cura -----------------------------
    public void curar(Jogador j) {
        if (j.getKitsMedicos() > 0) {
            System.out.println("Você usou um Kit Médico.");
            j.setKitsMedicos(j.getKitsMedicos() - 1);
            j.setSaude(Macros.SAUDE_JOGADOR);
        }
        else System.out.println("Você não possui Kits Médicos para usar.");
    }

    // ----------------------------- teste percepção -----------------------------
    public boolean passouTestePercepcao(Jogador j) {
        int dado = random.nextInt(3) + 1;
        return dado <= j.getPercepcao();
    }

    // ----------------------------- fugir -----------------------------
    public void fugir(Jogador jogador, Tabuleiro tabuleiro) {
        for (int tentativas = 0; tentativas < 4; tentativas++) {
            int val = random.nextInt(4);
            Sistema.Movimentacao.Direcao dir = switch (val) {
                case 0 -> Sistema.Movimentacao.Direcao.CIMA;
                case 1 -> Sistema.Movimentacao.Direcao.BAIXO;
                case 2 -> Sistema.Movimentacao.Direcao.DIREITA;
                case 3 -> Sistema.Movimentacao.Direcao.ESQUERDA;
                default -> Sistema.Movimentacao.Direcao.INVALIDA;
            };
            jogador.mover(dir, tabuleiro);
        }
    }
}