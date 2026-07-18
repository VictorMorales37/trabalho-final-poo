package Gui;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Itens.Item;
import Util.Direcao;
import Util.Macros;
import Util.ResultadoCombate;

import javax.swing.*;
import java.awt.*;

public class JanelaPrincipal extends JFrame {

    private ControladorGUI jogo;
    private PainelTabuleiro tabuleiro;
    private JLabel lblSaude;
    private JProgressBar barraSaude;
    private DefaultListModel<String> listaItens;
    private JTextArea mensagens;

    public JanelaPrincipal() {
        super("Jurassic - Aventura");
        jogo = new ControladorGUI();
        jogo.setJanela(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        criarMenu();
        criarTela();
        pack();
        setMinimumSize(new Dimension(750, 600));
        setLocationRelativeTo(null);

        perguntarNovoJogo();
    }

    private void criarMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu arquivo = new JMenu("Arquivo");
        JMenuItem salvar = new JMenuItem("Salvar");
        salvar.addActionListener(e -> jogo.salvar());
        JMenuItem carregar = new JMenuItem("Carregar Jogo");
        carregar.addActionListener(e -> jogo.carregar());
        JMenuItem sair = new JMenuItem("Saída");
        sair.addActionListener(e -> System.exit(0));
        arquivo.add(salvar);
        arquivo.add(carregar);
        arquivo.add(sair);

        JMenu menuJogo = new JMenu("Jogo");
        JMenuItem novo = new JMenuItem("Novo Jogo");
        novo.addActionListener(e -> perguntarNovoJogo());
        JMenuItem reiniciar = new JMenuItem("Reiniciar");
        reiniciar.addActionListener(e -> jogo.reiniciar());
        JMenuItem debug = new JMenuItem("Modo Debug");
        debug.addActionListener(e -> jogo.toggleDebug());
        menuJogo.add(novo);
        menuJogo.add(reiniciar);
        menuJogo.add(debug);

        barra.add(arquivo);
        barra.add(menuJogo);
        setJMenuBar(barra);
    }

    private void criarTela() {
        // tabuleiro no centro
        tabuleiro = new PainelTabuleiro();
        add(tabuleiro, BorderLayout.CENTER);

        // painel lateral direito
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(new Color(210, 180, 140));
        lateral.setPreferredSize(new Dimension(200, 500));
        lateral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("Status do personagem");
        titulo.setFont(new Font("Arial", Font.BOLD, 14));
        lateral.add(titulo);
        lateral.add(Box.createVerticalStrut(10));

        lblSaude = new JLabel("saúde: 0/5");
        lblSaude.setForeground(Color.RED);
        lateral.add(lblSaude);

        barraSaude = new JProgressBar(0, Macros.SAUDE_JOGADOR);
        barraSaude.setForeground(Color.RED);
        barraSaude.setMaximumSize(new Dimension(200, 25));
        lateral.add(barraSaude);
        lateral.add(Box.createVerticalStrut(15));

        lateral.add(new JLabel("inventario"));
        listaItens = new DefaultListModel<>();
        JList<String> lista = new JList<>(listaItens);
        JScrollPane scrollInv = new JScrollPane(lista);
        scrollInv.setPreferredSize(new Dimension(180, 120));
        lateral.add(scrollInv);
        lateral.add(Box.createVerticalStrut(15));

        // botoes de movimento
        lateral.add(new JLabel("Movimento"));
        lateral.add(criarBotao("↑", Direcao.CIMA));
        JPanel meio = new JPanel(new FlowLayout());
        meio.setBackground(new Color(210, 180, 140));
        meio.add(criarBotao("←", Direcao.ESQUERDA));
        meio.add(criarBotao("→", Direcao.DIREITA));
        lateral.add(meio);
        lateral.add(criarBotao("↓", Direcao.BAIXO));

        JButton btnCurar = new JButton("Kit médico");
        btnCurar.addActionListener(e -> jogo.curar());
        lateral.add(Box.createVerticalStrut(10));
        lateral.add(btnCurar);

        add(lateral, BorderLayout.EAST);

        // mensagens embaixo
        mensagens = new JTextArea(3, 40);
        mensagens.setEditable(false);
        add(new JScrollPane(mensagens), BorderLayout.SOUTH);
    }

    private JButton criarBotao(String texto, Direcao dir) {
        JButton btn = new JButton(texto);
        btn.addActionListener(e -> jogo.mover(dir));
        return btn;
    }

    private void perguntarNovoJogo() {
        String[] difs = {"Fácil", "Médio", "Difícil"};
        String[] mapas = {"Mapa 1", "Mapa 2", "Mapa 3", "Mapa 4", "Mapa 5"};

        int dif = JOptionPane.showOptionDialog(this, "Dificuldade:", "Novo Jogo",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, difs, difs[0]) + 1;

        if (dif == 0) return;

        int mapa = JOptionPane.showOptionDialog(this, "Mapa:", "Novo Jogo",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, mapas, mapas[0]) + 1;

        if (mapa == 0) return;

        jogo.novoJogo(dif, mapa);
    }

    public void atualizarTela() {
        tabuleiro.setDados(jogo.getTabuleiro(), jogo.getJogador(), jogo.isDebug());

        if (jogo.getJogador() != null) {
            int hp = Math.max(0, jogo.getJogador().getSaude());
            lblSaude.setText("saúde: " + hp + "/" + Macros.SAUDE_JOGADOR);
            barraSaude.setValue(hp);

            listaItens.clear();
            for (Item item : jogo.getJogador().getInventario().getItens()) {
                listaItens.addElement(item.getNome());
            }
        }
    }

    public void mostrarMensagem(String msg) {
        mensagens.append(msg + "\n");
    }

    public void abrirCombate(Dinossauro dino, Direcao dir) {
        DialogoCombate dialogo = new DialogoCombate(this, jogo.getJogador(), dino,
                jogo.getCombate(), jogo.getTabuleiro());
        ResultadoCombate res = dialogo.getResultado();
        jogo.fimCombate(dino, res, dir);
    }

    public void mostrarVitoria() {
        JOptionPane.showMessageDialog(this, "Você venceu!", "Vitória", JOptionPane.INFORMATION_MESSAGE);
    }

    public void mostrarDerrota() {
        int r = JOptionPane.showConfirmDialog(this, "Você morreu! Reiniciar?",
                "Derrota", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) jogo.reiniciar();
    }
}
