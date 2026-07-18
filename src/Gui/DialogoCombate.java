package Gui;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.ArmaDardos;
import Itens.Bastao;
import Itens.Consumiveis.KitMedico;
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

    private final JButton btnMaos;
    private final JButton btnBastao;
    private final JButton btnDardos;
    private final JButton btnCurar;
    private final JButton btnFugir;

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
        btnMaos = criarBotao("Mãos", 1);
        btnBastao = criarBotao("Bastão", 2);
        btnDardos = criarBotao("Dardos", 3);
        btnCurar = criarBotao("Curar", 4);
        btnFugir = criarBotao("Fugir", 5);
        botoes.add(btnMaos);
        botoes.add(btnBastao);
        botoes.add(btnDardos);
        botoes.add(btnCurar);
        botoes.add(btnFugir);
        add(botoes, BorderLayout.SOUTH);

        atualizarHp(jogador);
        atualizarBotoes();
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
        atualizarBotoes();
    }

    // desabilita botões de itens que o jogador não tem (ficam cinza / não clicáveis)
    public void atualizarBotoes() {
        boolean temBastao = jogador.pegarItem(Bastao.class) != null;
        boolean temDardos = jogador.pegarItem(ArmaDardos.class) != null;
        boolean temKit = jogador.pegarItem(KitMedico.class) != null;

        btnMaos.setEnabled(combate.podeUsarMaos(jogador) && dino.podeSerAtacadoSemArma());
        btnBastao.setEnabled(temBastao);
        btnDardos.setEnabled(temDardos);
        btnCurar.setEnabled(temKit);
        btnFugir.setEnabled(true);
    }
}
