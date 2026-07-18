package Sistema;

import Entidades.Caixa;
import Entidades.Personagens.Dinossauros.Compsognato;
import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Util.Direcao;
import Util.EstadoJogo;
import Util.ResultadoCombate;
import Util.ResultadoMovimento;

import java.util.ArrayList;

// Responsável por combates, caixas, fuga e fim de jogo (vitória/derrota)
public class GerenciadorEncontros {

    private final SistemaCombate sistemaCombate;
    private final SistemaItens sistemaItens;
    private final Menu menu;
    private final LeitorDeInput leitor;
    private final Tabuleiro tabuleiro;
    private final EstadoExecucao execucao;
    private final GerenciadorThreads threads;
    private final boolean modoGui;

    private InterfaceGui interfaceGui;

    public GerenciadorEncontros(SistemaCombate sistemaCombate, SistemaItens sistemaItens,
                                Menu menu, LeitorDeInput leitor, Tabuleiro tabuleiro,
                                EstadoExecucao execucao, GerenciadorThreads threads,
                                boolean modoGui) {
        this.sistemaCombate = sistemaCombate;
        this.sistemaItens = sistemaItens;
        this.menu = menu;
        this.leitor = leitor;
        this.tabuleiro = tabuleiro;
        this.execucao = execucao;
        this.threads = threads;
        this.modoGui = modoGui;
    }

    public void setInterfaceGui(InterfaceGui interfaceGui) {
        this.interfaceGui = interfaceGui;
    }

    // abre caixa na posição do jogador; devolve compsognato se houver surpresa
    public Compsognato abrirCaixa(Jogador jogador, ArrayList<Caixa> caixas,
                                  ArrayList<Dinossauro> dinossauros) {
        Compsognato surpresa = sistemaItens.processarCaixaNaPosicao(jogador, caixas);
        tabuleiro.atualizar(jogador, dinossauros, caixas);
        if (surpresa != null) {
            menu.mensagem("Cuidado! Um compsognato estava atrás da caixa!");
            dinossauros.add(surpresa);
            tabuleiro.atualizar(jogador, dinossauros, caixas);
        } else {
            menu.mensagem("Você encontrou um item na caixa!");
        }
        return surpresa;
    }

    public Dinossauro acharDinoNaDirecao(Jogador jogador, Direcao direcao,
                                         ArrayList<Dinossauro> dinossauros) {
        int x = jogador.getPosicaoX() + direcao.dx;
        int y = jogador.getPosicaoY() + direcao.dy;
        for (Dinossauro d : dinossauros) {
            if (d.getPosicaoX() == x && d.getPosicaoY() == y) {
                return d;
            }
        }
        return null;
    }

    public ResultadoCombate executarCombate(Jogador jogador, Dinossauro dino,
                                            boolean dinoAtacouPrimeiro,
                                            ArrayList<Dinossauro> dinossauros,
                                            ArrayList<Caixa> caixas) {
        if (dinoAtacouPrimeiro) {
            if (!aplicarAtaqueSurpresa(jogador, dino)) {
                tratarDerrota(jogador);
                return ResultadoCombate.PERDEU;
            }
        }

        if (interfaceGui != null) {
            interfaceGui.prepararCombate(jogador, dino);
        }
        ResultadoCombate res = sistemaCombate.combate(jogador, dino, menu, leitor, tabuleiro);
        if (interfaceGui != null) {
            interfaceGui.finalizarCombate();
        }

        if (res == null) {
            res = dino.estaVivo() ? ResultadoCombate.FUGIU : ResultadoCombate.VENCEU;
        }

        if (res == ResultadoCombate.PERDEU) {
            tratarDerrota(jogador);
        } else if (res == ResultadoCombate.VENCEU) {
            dinossauros.remove(dino);
            threads.pararUma(dino);
            tabuleiro.atualizar(jogador, dinossauros, caixas);
        } else if (res == ResultadoCombate.FUGIU) {
            separarAposFuga(jogador, dino, dinossauros, caixas);
        }

        return res;
    }

    private boolean aplicarAtaqueSurpresa(Jogador jogador, Dinossauro dino) {
        boolean desviou = sistemaCombate.passouTestePercepcao(jogador);
        if (!desviou) {
            jogador.setSaude(jogador.getSaude() - dino.getDanoAtaque());
            menu.mensagem("O dinossauro te atacou! -" + dino.getDanoAtaque() + " de saúde.");
            return jogador.getSaude() > 0;
        }
        menu.mensagem("Você desviou do ataque inicial!");
        return true;
    }

    private void separarAposFuga(Jogador jogador, Dinossauro d,
                                 ArrayList<Dinossauro> dinossauros, ArrayList<Caixa> caixas) {
        if (d.getPosicaoX() != jogador.getPosicaoX() || d.getPosicaoY() != jogador.getPosicaoY()) {
            tabuleiro.atualizar(jogador, dinossauros, caixas);
            return;
        }
        Direcao[] dirs = {Direcao.CIMA, Direcao.BAIXO, Direcao.ESQUERDA, Direcao.DIREITA};
        for (Direcao dir : dirs) {
            int nx = d.getPosicaoX() + dir.dx;
            int ny = d.getPosicaoY() + dir.dy;
            if (d.verificaMovimento(nx, ny, tabuleiro) == ResultadoMovimento.LIVRE) {
                d.setPosicaoX(nx);
                d.setPosicaoY(ny);
                break;
            }
        }
        tabuleiro.atualizar(jogador, dinossauros, caixas);
    }

    public void tratarVitoria() {
        menu.mensagemVitoria();
        execucao.setEstado(EstadoJogo.VITORIA);
        execucao.setPausado(true);
        threads.pararTodas();
        if (interfaceGui != null) {
            interfaceGui.onVitoria();
        }
    }

    public void tratarDerrota(Jogador jogador) {
        menu.mensagemDerrota();
        execucao.setEstado(EstadoJogo.DERROTA);
        execucao.setPausado(true);
        threads.pararTodas();
        if (modoGui && interfaceGui != null) {
            interfaceGui.onDerrota();
        }
    }

    // libera pausa se a partida ainda está em andamento
    public void liberarPausaSeJogavel(Jogador jogador, ArrayList<Dinossauro> dinossauros) {
        if (execucao.getEstado() == EstadoJogo.CONTINUAR
                && jogador.estaVivo()
                && !dinossauros.isEmpty()) {
            execucao.setPausado(false);
        }
    }

    public void verificarVitoria(ArrayList<Dinossauro> dinossauros) {
        if (dinossauros.isEmpty() && execucao.getEstado() == EstadoJogo.CONTINUAR) {
            tratarVitoria();
        }
    }
}
