package Gui;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.ArmaDardos;
import Itens.Bastao;
import Itens.Consumiveis.Consumivel;
import Itens.Consumiveis.KitMedico;
import Itens.Item;
import Sistema.SistemaCombate;
import Sistema.Tabuleiro;
import Util.Macros;
import Util.ResultadoCombate;

import javax.swing.*;
import java.awt.*;

public class DialogoCombate extends JDialog {

    private Jogador jogador;
    private Dinossauro dino;
    private SistemaCombate combate;
    private Tabuleiro tabuleiro;

    private JLabel lblJogador;
    private JLabel lblDino;
    private JTextArea log;
    private ResultadoCombate resultado;

    public DialogoCombate(JFrame pai, Jogador jogador, Dinossauro dino,
                          SistemaCombate combate, Tabuleiro tabuleiro) {
        super(pai, "Combate", true);
        this.jogador = jogador;
        this.dino = dino;
        this.combate = combate;
        this.tabuleiro = tabuleiro;

        setSize(450, 400);
        setLocationRelativeTo(pai);
        setLayout(new BorderLayout(5, 5));

        // HP do jogador e do dinossauro
        JPanel topo = new JPanel(new GridLayout(1, 2, 10, 0));
        lblJogador = new JLabel("Jogador HP: " + jogador.getSaude());
        lblDino = new JLabel("Dino HP: " + dino.getSaude());
        topo.add(lblJogador);
        topo.add(lblDino);
        add(topo, BorderLayout.NORTH);

        // log de mensagens
        log = new JTextArea(10, 30);
        log.setEditable(false);
        add(new JScrollPane(log), BorderLayout.CENTER);

        // botoes de acao
        JPanel botoes = new JPanel(new GridLayout(2, 3, 5, 5));
        botoes.add(criarBotao("Mãos", 1));
        botoes.add(criarBotao("Bastão", 2));
        botoes.add(criarBotao("Dardos", 3));
        botoes.add(criarBotao("Curar", 4));
        botoes.add(criarBotao("Fugir", 5));
        add(botoes, BorderLayout.SOUTH);

        escrever("Combate!");
    }

    public ResultadoCombate getResultado() {
        setVisible(true);
        return resultado;
    }

    private JButton criarBotao(String texto, int acao) {
        JButton btn = new JButton(texto);
        btn.addActionListener(e -> fazerAcao(acao));
        return btn;
    }

    private void fazerAcao(int acao) {
        if (acao == 5) {
            combate.fugir(jogador, tabuleiro);
            escrever("Fugiu!");
            resultado = ResultadoCombate.FUGIU;
            dispose();
            return;
        }

        if (acao == 4) {
            Item kit = jogador.pegarItem(KitMedico.class);
            if (kit == null) {
                escrever("Sem kit.");
                return;
            }
            kit.usar(jogador, dino);
            if (kit instanceof Consumivel c && c.consumidoAposUso()) {
                jogador.removerItem(kit);
            }
            escrever("Curou!");
        } else {
            int dano = 0;
            if (acao == 1) {
                if (!dino.podeSerAtacadoSemArma()) escrever("T-Rex precisa de arma!");
                else dano = combate.atacarMao();
            } else if (acao == 2) {
                Item bastao = jogador.pegarItem(Bastao.class);
                if (bastao == null) escrever("Sem bastão.");
                else dano = bastao.usar(jogador, dino);
            } else if (acao == 3) {
                Item arma = jogador.pegarItem(ArmaDardos.class);
                if (arma == null) escrever("Sem dardos.");
                else dano = arma.usar(jogador, dino);
            }

            if (dano > 0) {
                dino.receberDano(dano);
                escrever("Deu " + dano + " de dano.");
            } else if (acao != 1 || dino.podeSerAtacadoSemArma()) {
                escrever("Ataque falhou.");
            }

            if (!dino.estaVivo()) {
                escrever("Dinossauro derrotado!");
                resultado = ResultadoCombate.VENCEU;
                dispose();
                return;
            }
        }

        atualizarHp();
        turnoDino();
    }

    private void turnoDino() {
        escrever("Dinossauro ataca...");
        if (combate.passouTestePercepcao(jogador)) {
            escrever("Desviou!");
        } else if (dino.getSimbolo() == Macros.SIMB_TREX) {
            jogador.setSaude(jogador.getSaude() - 4);
            escrever("T-Rex! -4 HP");
        } else {
            jogador.setSaude(jogador.getSaude() - dino.getDanoAtaque());
            escrever("-" + dino.getDanoAtaque() + " HP");
        }

        atualizarHp();

        if (!jogador.estaVivo()) {
            escrever("Você morreu...");
            resultado = ResultadoCombate.PERDEU;
            dispose();
        }
    }

    private void atualizarHp() {
        lblJogador.setText("Jogador HP: " + jogador.getSaude() + "/" + Macros.SAUDE_JOGADOR);
        lblDino.setText("Dino HP: " + dino.getSaude());
    }

    private void escrever(String msg) {
        log.append(msg + "\n");
    }
}
