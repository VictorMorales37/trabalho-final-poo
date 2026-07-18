package Sistema;
import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.ArmaDardos;
import Itens.Bastao;
import Itens.Item;
import Itens.Consumiveis.KitMedico;
import Itens.Consumiveis.Consumivel;
import Util.Direcao;
import Util.Macros;
import Util.ResultadoCombate;

import java.util.Random;


public class SistemaCombate {
    private final Random random;

    public SistemaCombate(Random random) {
        this.random = random;
    }

    public int atacarMao(){
        int acerto = random.nextInt(6) + 1;

        if (acerto == 6) return 2; // critico
        else if (acerto == 1 || acerto == 2) return 0; // falha
        else return 1;
    }

    public ResultadoCombate combate(Jogador jogador, Dinossauro dino, Menu menu,
                                    LeitorDeInput leitorDeInput, Tabuleiro tabuleiro) {

        while (jogador.estaVivo() && dino.estaVivo()) {
            menu.opcoesCombate(jogador); // 1 - mão | 2 - bastão | 3 - dardos | 4 - curar | 5 - fugir
            int input = leitorDeInput.lerInput(1, 5);

            if (input == 5) {
                fugir(jogador, tabuleiro);
                System.out.println("Voce fugiu");
                return ResultadoCombate.FUGIU;
            }

            if (input == 4) {
                Item kit =  jogador.pegarItem(KitMedico.class);
                if (kit == null) {
                    System.out.println("Você não tem kits de medicos.");
                } else {
                    kit.usar(jogador, dino);
                    if (kit instanceof Consumivel && ((Consumivel) kit).consumidoAposUso()) {
                        jogador.removerItem(kit);
                    }
                }
            }
            else {
                int dano = switch (input) {
                    case 1:
                        if (!dino.podeSerAtacadoSemArma()) {
                            System.out.println("Não é possível atacar o T-Rex sem armas!");
                            yield 0;
                        }
                        yield atacarMao();

                    case 2:
                        Item bastao = jogador.pegarItem(Bastao.class);
                        if (bastao == null) {
                            System.out.println("Você ainda não tem bastão.");
                            yield 0;
                        } else {
                            yield bastao.usar(jogador, dino);
                        }

                    case 3:
                        Item arma = jogador.pegarItem(ArmaDardos.class);
                        if (arma == null) {
                            System.out.println("Você ainda não tem arma de dardos.");
                            yield 0;
                        } else {
                            yield arma.usar(jogador, dino);
                        }
                    default:
                        yield 0;
                };

                if (dano == 0) System.out.println("Ataque falhou");
                else {
                    System.out.println("Voce atacou o dinossauro");
                    System.out.println("Ele recebeu " + dano + " de dano\n");
                }
                dino.receberDano(dano);
                if (!dino.estaVivo()) {
                    System.out.println("Voce derrotou o dinossauro!");
                    return ResultadoCombate.VENCEU;
                }

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

            if (!jogador.estaVivo()){
                return ResultadoCombate.PERDEU;
            }
        }
        return null;
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
            Direcao dir = switch (val) {
                case 0 -> Direcao.CIMA;
                case 1 -> Direcao.BAIXO;
                case 2 -> Direcao.DIREITA;
                case 3 -> Direcao.ESQUERDA;
                default -> Direcao.INVALIDA;
            };
            jogador.mover(dir, tabuleiro);
        }
    }
}