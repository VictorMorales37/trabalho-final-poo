package Gui;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Entidades.Personagens.Jogador;
import Itens.Consumiveis.KitMedico;
import Itens.Item;
import Sistema.InterfaceGui;
import Sistema.Jogo;
import Sistema.ThreadJogador;
import Util.Direcao;
import Util.Macros;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class JanelaPrincipal extends JFrame implements InterfaceGui {

    private final Jogo jogo;
    private final MenuGui menuGui;
    private final LeitorGui leitorGui;
    private DialogoCombate dialogoCombate;

    private PainelTabuleiro painelTabuleiro;
    private JLabel lblSaude;
    private JProgressBar barraSaude;
    private DefaultListModel<String> listaItens;
    private JButton btnKitMedico;

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
        configurarTeclado();
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
        sair.addActionListener(e -> {
            jogo.pararThreadsDinossauros();
            System.exit(0);
        });
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

        JLabel titulo = new JLabel("Status do personagem");
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setFont(new Font("Arial", Font.BOLD, 16));

        lateral.add(titulo);
        lateral.add(Box.createVerticalStrut(10));

        lblSaude = new JLabel("saúde: 0/5");
        lblSaude.setForeground(Color.RED);
        lblSaude.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSaude.setFont(new Font("Arial", Font.BOLD, 12));
        lateral.add(lblSaude);

        barraSaude = new JProgressBar(0, Macros.SAUDE_JOGADOR);
        barraSaude.setForeground(Color.RED);
        barraSaude.setMaximumSize(new Dimension(200, 25));
        lateral.add(barraSaude);
        lateral.add(Box.createVerticalStrut(15));

        JLabel inventario = new JLabel("inventario");
        inventario.setAlignmentX(Component.CENTER_ALIGNMENT);
        inventario.setFont(new Font("Arial", Font.BOLD, 14));
        lateral.add(inventario);
        listaItens = new DefaultListModel<>();
        lateral.add(new JScrollPane(new JList<>(listaItens)));
        lateral.add(Box.createVerticalStrut(15));


        JButton btnCurar = new JButton("Kit médico");
        btnCurar.setFocusable(false);
        btnCurar.addActionListener(e -> executar(() -> jogo.usarKitMedico()));
        this.btnKitMedico = btnCurar;
        lateral.add(Box.createVerticalStrut(10));
        lateral.add(btnCurar);

        add(lateral, BorderLayout.EAST);

        JTextArea mensagens = new JTextArea(3, 40);
        mensagens.setEditable(false);
        mensagens.setFocusable(false);
        menuGui.setArea(mensagens);
        add(new JScrollPane(mensagens), BorderLayout.SOUTH);
    }

    // WASD
    private void configurarTeclado() {
        setFocusable(true);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() != KeyEvent.KEY_PRESSED) return false;
            // não move durante combate
            if (dialogoCombate != null && dialogoCombate.isVisible()) return false;

            Direcao dir = null;
            int tecla = e.getKeyCode();
            if (tecla == KeyEvent.VK_W || tecla == KeyEvent.VK_UP) {
                dir = Direcao.CIMA;
            } else if (tecla == KeyEvent.VK_S || tecla == KeyEvent.VK_DOWN) {
                dir = Direcao.BAIXO;
            } else if (tecla == KeyEvent.VK_A || tecla == KeyEvent.VK_LEFT) {
                dir = Direcao.ESQUERDA;
            } else if (tecla == KeyEvent.VK_D || tecla == KeyEvent.VK_RIGHT) {
                dir = Direcao.DIREITA;
            }

            if (dir != null) {
                new ThreadJogador(jogo, dir).start();
                return true;
            }
            return false;
        });
    }


    private void executar(Runnable acao) {
        new Thread(() -> {
            acao.run();
            SwingUtilities.invokeLater(this::atualizar);
        }).start();
    }

    private void perguntarNovoJogo() {
        String[] difs = {"Fácil", "Médio", "Difícil"};

        int dif = JOptionPane.showOptionDialog(this, "Dificuldade:", "Novo Jogo",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, difs, difs[0]) + 1;
        if (dif == 0) return;

        int mapa = jogo.random.nextInt(Macros.NUM_MAPAS) + 1;
        jogo.iniciarNovoJogo(dif, mapa);
        requestFocusInWindow();
    }

    @Override
    public void atualizar() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(this::atualizar);
            return;
        }

        painelTabuleiro.setDados(jogo.getTabuleiro(), jogo.getJogador(), jogo.isDebugMode());

        if (jogo.getJogador() != null) {
            int hp = Math.max(0, jogo.getJogador().getSaude());
            lblSaude.setText("saúde: " + hp + "/" + Macros.SAUDE_JOGADOR);
            barraSaude.setValue(hp);

            listaItens.clear();
            for (Item item : jogo.getJogador().getInventario().getItens()) {
                listaItens.addElement(item.getNome());
            }

            btnKitMedico.setEnabled(jogo.getJogador().pegarItem(KitMedico.class) != null);
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
                requestFocusInWindow();
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
