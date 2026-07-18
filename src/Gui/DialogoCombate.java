package Gui;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Sistema.SistemaCombate;
import Util.Macros;

import javax.swing.*;
import java.awt.*;

public class DialogoCombate extends JDialog {

    private final Jogador jogador;
    private final Dinossauro dino;
    private final LeitorGui leitor;
    private final SistemaCombate combate;

    private final JLabel lblJogador;
    private final JLabel lblDino;
    private final JTextArea log;

    public DialogoCombate(JFrame pai, LeitorGui leitor, Jogador jogador,
                          Dinossauro dino, SistemaCombate combate) {
        super(pai, "Combate", false);
        this.leitor = leitor;
        this.jogador = jogador;
        this.dino = dino;
        this.combate = combate;

        setSize(450, 400);
        setLocationRelativeTo(pai);
        setLayout(new BorderLayout(5, 5));

        JPanel topo = new JPanel(new GridLayout(1, 2, 10, 0));
        lblJogador = new JLabel();
        lblDino = new JLabel();
        topo.add(lblJogador);
        topo.add(lblDino);
        add(topo, BorderLayout.NORTH);

        log = new JTextArea(10, 30);
        log.setEditable(false);
        add(new JScrollPane(log), BorderLayout.CENTER);

        JPanel botoes = new JPanel(new GridLayout(2, 3, 5, 5));
        JButton btnMaos = new JButton("Mãos");
        btnMaos.setEnabled(combate.podeUsarMaos(jogador));
        btnMaos.addActionListener(e -> leitor.escolher(1));
        botoes.add(btnMaos);
        botoes.add(criarBotao("Bastão", 2));
        botoes.add(criarBotao("Dardos", 3));
        botoes.add(criarBotao("Curar", 4));
        botoes.add(criarBotao("Fugir", 5));
        add(botoes, BorderLayout.SOUTH);

        atualizarHp(jogador);
        adicionarLog("Combate!");
    }

    private JButton criarBotao(String texto, int acao) {
        JButton btn = new JButton(texto);
        btn.addActionListener(e -> leitor.escolher(acao));
        return btn;
    }

    public void adicionarLog(String msg) {
        log.append(msg + "\n");
    }

    public void atualizarHp(Jogador j) {
        lblJogador.setText("Jogador: " + j.getSaude() + "/" + Macros.SAUDE_JOGADOR);
        lblDino.setText("Dino: " + dino.getSaude());
    }
}
