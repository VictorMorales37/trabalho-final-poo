package Gui;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.Item;
import Sistema.InterfaceGui;
import Sistema.Jogo;
import Util.Direcao;
import Util.Macros;

import javax.swing.*;
import java.awt.*;

public class JanelaPrincipal extends JFrame implements InterfaceGui {

    private final Jogo jogo;
    private final MenuGui menuGui;
    private final LeitorGui leitorGui;
    private DialogoCombate dialogoCombate;

    private PainelTabuleiro painelTabuleiro;
    private JLabel lblSaude;
    private JProgressBar barraSaude;
    private DefaultListModel<String> listaItens;

    public JanelaPrincipal() {
        super("Jurassic - Aventura");

        menuGui = new MenuGui();
        leitorGui = new LeitorGui();
        jogo = new Jogo(menuGui, leitorGui, true);
        jogo.setInterfaceGui(this);

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
        salvar.addActionListener(e -> executar(() -> jogo.salvarPartida()));
        JMenuItem carregar = new JMenuItem("Carregar Jogo");
        carregar.addActionListener(e -> executar(() -> jogo.carregarPartida()));
        JMenuItem sair = new JMenuItem("Saída");
        sair.addActionListener(e -> System.exit(0));
        arquivo.add(salvar);
        arquivo.add(carregar);
        arquivo.add(sair);

        JMenu menuJogo = new JMenu("Jogo");
        JMenuItem novo = new JMenuItem("Novo Jogo");
        novo.addActionListener(e -> perguntarNovoJogo());
        JMenuItem reiniciar = new JMenuItem("Reiniciar");
        reiniciar.addActionListener(e -> executar(() -> jogo.reiniciarPartida()));
        JMenuItem debug = new JMenuItem("Modo Debug");
        debug.addActionListener(e -> jogo.alternarDebug());
        menuJogo.add(novo);
        menuJogo.add(reiniciar);
        menuJogo.add(debug);

        barra.add(arquivo);
        barra.add(menuJogo);
        setJMenuBar(barra);
    }

    private void criarTela() {
        painelTabuleiro = new PainelTabuleiro();
        add(painelTabuleiro, BorderLayout.CENTER);

        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(new Color(210, 180, 140));
        lateral.setPreferredSize(new Dimension(200, 500));
        lateral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lateral.add(new JLabel("Status do personagem"));
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
        lateral.add(new JScrollPane(new JList<>(listaItens)));
        lateral.add(Box.createVerticalStrut(15));

        lateral.add(new JLabel("Movimento"));
        lateral.add(criarBotaoMovimento("↑", Direcao.CIMA));
        JPanel meio = new JPanel(new FlowLayout());
        meio.setBackground(new Color(210, 180, 140));
        meio.add(criarBotaoMovimento("←", Direcao.ESQUERDA));
        meio.add(criarBotaoMovimento("→", Direcao.DIREITA));
        lateral.add(meio);
        lateral.add(criarBotaoMovimento("↓", Direcao.BAIXO));

        JButton btnCurar = new JButton("Kit médico");
        btnCurar.addActionListener(e -> executar(() -> jogo.usarKitMedico()));
        lateral.add(Box.createVerticalStrut(10));
        lateral.add(btnCurar);

        add(lateral, BorderLayout.EAST);

        JTextArea mensagens = new JTextArea(3, 40);
        mensagens.setEditable(false);
        menuGui.setArea(mensagens);
        add(new JScrollPane(mensagens), BorderLayout.SOUTH);
    }

    private JButton criarBotaoMovimento(String texto, Direcao dir) {
        JButton btn = new JButton(texto);
        btn.addActionListener(e -> executar(() -> jogo.executarMovimento(dir)));
        return btn;
    }

    private void executar(Runnable acao) {
        new Thread(() -> {
            acao.run();
            SwingUtilities.invokeLater(this::atualizar);
        }).start();
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

        jogo.iniciarNovoJogo(dif, mapa);
    }

    @Override
    public void atualizar() {
        painelTabuleiro.setDados(jogo.getTabuleiro(), jogo.getJogador(), jogo.isDebugMode());

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

    @Override
    public void onVitoria() {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, "Você venceu!", "Vitória", JOptionPane.INFORMATION_MESSAGE));
    }

    @Override
    public void onDerrota() {
        SwingUtilities.invokeLater(() -> {
            int r = JOptionPane.showConfirmDialog(this, "Você morreu! Reiniciar?",
                    "Derrota", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                executar(() -> jogo.reiniciarPartida());
            }
        });
    }

    @Override
    public void prepararCombate(Jogador jogador, Dinossauro dinossauro) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                dialogoCombate = new DialogoCombate(this, leitorGui, jogador, dinossauro, jogo.getSistemaCombate());
                menuGui.setDialogo(dialogoCombate);
                dialogoCombate.setVisible(true);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void finalizarCombate() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                if (dialogoCombate != null) {
                    dialogoCombate.dispose();
                    dialogoCombate = null;
                }
                menuGui.setDialogo(null);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
