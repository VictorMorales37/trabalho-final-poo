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
import Util.ResultadoMovimento;

import java.util.Random;


public class SistemaCombate {
    private final Random random;

    public SistemaCombate(Random random) {
        this.random = random;
    }

    public boolean podeUsarMaos(Jogador jogador) {
        return jogador.pegarItem(Bastao.class) == null;
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
            menu.opcoesCombate(jogador);
            int input = leitorDeInput.lerInput(1, 5);

            if (input == 5) {
                boolean fugiu = fugir(jogador, tabuleiro);
                if (fugiu) {
                    menu.mensagem("Voce fugiu");
                    return ResultadoCombate.FUGIU;
                } else {
                    menu.mensagem("Não foi possível fugir!");
                    continue;
                }
            }

            if (input == 4) {
                Item kit = jogador.pegarItem(KitMedico.class);
                if (kit == null) continue; // botão desabilitado na GUI
                kit.usar(jogador, dino, menu);
                if (kit instanceof Consumivel && ((Consumivel) kit).consumidoAposUso()) {
                    jogador.removerItem(kit);
                }
            } else {
                int dano = 0;
                if (input == 1) {
                    if (!podeUsarMaos(jogador) || !dino.podeSerAtacadoSemArma()) continue;
                    dano = atacarMao();
                } else if (input == 2) {
                    Item bastao = jogador.pegarItem(Bastao.class);
                    if (bastao == null) continue;
                    dano = bastao.usar(jogador, dino, menu);
                } else if (input == 3) {
                    Item arma = jogador.pegarItem(ArmaDardos.class);
                    if (arma == null) continue;
                    dano = arma.usar(jogador, dino, menu);
                } else {
                    continue;
                }

                if (dano == 0) menu.mensagem("Ataque falhou");
                else {
                    menu.mensagem("Voce atacou o dinossauro");
                    menu.mensagem("Ele recebeu " + dano + " de dano");
                }
                dino.receberDano(dano);
                if (!dino.estaVivo()) {
                    menu.mensagem("Voce derrotou o dinossauro!");
                    return ResultadoCombate.VENCEU;
                }
            }

            menu.mensagem("Cuidado!");
            menu.mensagem("O dinossauro está tentando te atacar...");
            boolean passouTestePercepcao = passouTestePercepcao(jogador);
            if (passouTestePercepcao) {
                menu.mensagem("Voce conseguiu desviar.");
            }
            if (!passouTestePercepcao && dino.getSimbolo() == Macros.SIMB_TREX) {
                menu.mensagem("T-REX TE ATACOU!!!");
                jogador.setSaude(jogador.getSaude() - 4);
            }
            else if (!passouTestePercepcao) {
                menu.mensagem("O dinossauro te atacou!");
                jogador.setSaude(jogador.getSaude() - dino.getDanoAtaque());
            }

            if (!jogador.estaVivo()){
                return ResultadoCombate.PERDEU;
            }
        }
        return ResultadoCombate.VENCEU;
    }

    public boolean passouTestePercepcao(Jogador j) {
        int dado = random.nextInt(3) + 1;
        return dado <= j.getPercepcao();
    }

    // tenta andar para uma célula livre (não fica na mesma do dino)
    public boolean fugir(Jogador jogador, Tabuleiro tabuleiro) {
        Direcao[] dirs = {Direcao.CIMA, Direcao.BAIXO, Direcao.ESQUERDA, Direcao.DIREITA};
        for (int i = 0; i < dirs.length; i++) {
            int j = random.nextInt(dirs.length);
            Direcao tmp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = tmp;
        }

        for (Direcao dir : dirs) {
            int novoX = jogador.getPosicaoX() + dir.dx;
            int novoY = jogador.getPosicaoY() + dir.dy;
            if (jogador.verificaMovimento(novoX, novoY, tabuleiro) == ResultadoMovimento.LIVRE) {
                jogador.setPosicaoX(novoX);
                jogador.setPosicaoY(novoY);
                return true;
            }
        }
        return false;
    }
}
