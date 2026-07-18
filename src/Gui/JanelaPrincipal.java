package Gui;

import Entidades.Personagens.Dinossauros.Dinossauro;
import Util.Direcao;
import Util.ResultadoCombate;

import javax.swing.*;
import java.awt.*;

public class JanelaPrincipal extends JFrame implements ControladorGUI.Listener {

    private final ControladorGUI controlador;
    private final PainelTabuleiro painelTabuleiro;
    private final PainelStatus painelStatus;
    private final PainelControles painelControles;
    private final JTextArea areaMensagens;
    private final JLabel lblDebug;

    public JanelaPrincipal() {
        super("Jurassic - Aventura");
        controlador = new ControladorGUI();
        controlador.setListener(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(4, 4));

        criarMenuBar();

        painelTabuleiro = new PainelTabuleiro();
        painelStatus = new PainelStatus();
        painelControles = new PainelControles();

        painelControles.setListener(new PainelControles.AcaoListener() {
            @Override
            public void onMover(Direcao direcao) {
                controlador.mover(direcao);
            }

            @Override
            public void onCurar() {
                controlador.curar();
            }
        });

        JPanel painelLateral = new JPanel(new BorderLayout(0, 8));
        painelLateral.setBackground(new Color(210, 180, 140));
        painelLateral.add(painelStatus, BorderLayout.CENTER);
        painelLateral.add(painelControles, BorderLayout.SOUTH);

        add(painelTabuleiro, BorderLayout.CENTER);
        add(painelLateral, BorderLayout.EAST);

        areaMensagens = new JTextArea(3, 40);
        areaMensagens.setEditable(false);
        areaMensagens.setFont(new Font("SansSerif", Font.PLAIN, 12));
        areaMensagens.setLineWrap(true);
        areaMensagens.setWrapStyleWord(true);
        areaMensagens.setBackground(new Color(245, 235, 220));
        JScrollPane scrollMensagens = new JScrollPane(areaMensagens);
        scrollMensagens.setBorder(BorderFactory.createTitledBorder("Mensagens"));

        lblDebug = new JLabel(" ");
        lblDebug.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblDebug.setForeground(new Color(180, 0, 0));
        lblDebug.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        JPanel painelInferior = new JPanel(new BorderLayout());
        painelInferior.add(lblDebug, BorderLayout.NORTH);
        painelInferior.add(scrollMensagens, BorderLayout.CENTER);
        add(painelInferior, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(780, 620));
        setLocationRelativeTo(null);

        SwingUtilities.invokeLater(this::mostrarDialogoInicio);
    }

    private void criarMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArquivo = new JMenu("Arquivo");
        JMenuItem itemSalvar = new JMenuItem("Salvar");
        itemSalvar.addActionListener(e -> controlador.salvarJogo());
        JMenuItem itemCarregar = new JMenuItem("Carregar Jogo");
        itemCarregar.addActionListener(e -> controlador.carregarJogo());
        JMenuItem itemSair = new JMenuItem("Saída");
        itemSair.addActionListener(e -> sair());
        menuArquivo.add(itemSalvar);
        menuArquivo.add(itemCarregar);
        menuArquivo.addSeparator();
        menuArquivo.add(itemSair);

        JMenu menuJogo = new JMenu("Jogo");
        JMenuItem itemNovo = new JMenuItem("Novo Jogo");
        itemNovo.addActionListener(e -> mostrarDialogoInicio());
        JMenuItem itemReiniciar = new JMenuItem("Reiniciar");
        itemReiniciar.addActionListener(e -> controlador.reiniciarPartida());
        JMenuItem itemDebug = new JMenuItem("Modo Debug");
        itemDebug.addActionListener(e -> controlador.alternarDebugMode());
        menuJogo.add(itemNovo);
        menuJogo.add(itemReiniciar);
        menuJogo.add(itemDebug);

        menuBar.add(menuArquivo);
        menuBar.add(menuJogo);
        setJMenuBar(menuBar);
    }

    private void mostrarDialogoInicio() {
        DialogoInicio dialogo = new DialogoInicio(this);
        dialogo.setVisible(true);
        if (dialogo.isConfirmado()) {
            controlador.iniciarNovoJogo(dialogo.getDificuldade(), dialogo.getMapa());
        }
    }

    private void sair() {
        int opcao = JOptionPane.showConfirmDialog(this,
                "Deseja realmente sair?",
                "Saída",
                JOptionPane.YES_NO_OPTION);
        if (opcao == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void atualizarInterface() {
        painelTabuleiro.atualizar(
                controlador.getTabuleiro(),
                controlador.getJogador(),
                controlador.isDebugMode()
        );
        painelStatus.atualizar(controlador.getJogador());
        lblDebug.setText(controlador.isDebugMode() ? " [MODO DEBUG ATIVO] " : " ");
    }

    @Override
    public void onEstadoAlterado() {
        SwingUtilities.invokeLater(this::atualizarInterface);
    }

    @Override
    public void onMensagem(String mensagem) {
        SwingUtilities.invokeLater(() -> {
            areaMensagens.append(mensagem + "\n");
            areaMensagens.setCaretPosition(areaMensagens.getDocument().getLength());
        });
    }

    @Override
    public void onCombateIniciado(Dinossauro dinossauro, boolean dinoAtacouPrimeiro, Direcao direcaoMovimento) {
        DialogoCombate dialogo = new DialogoCombate(
                this,
                controlador.getJogador(),
                dinossauro,
                controlador.getSistemaCombate(),
                controlador.getTabuleiro()
        );
        ResultadoCombate resultado = dialogo.aguardarResultado();
        controlador.processarResultadoCombate(dinossauro, resultado, direcaoMovimento);
    }

    @Override
    public void onVitoria() {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
                "Você EXTINGUIU os dinossauros!",
                "Vitória!",
                JOptionPane.INFORMATION_MESSAGE));
    }

    @Override
    public void onDerrota() {
        SwingUtilities.invokeLater(() -> {
            int opcao = JOptionPane.showConfirmDialog(this,
                    "Você MORREU! Deseja reiniciar a partida?",
                    "Derrota",
                    JOptionPane.YES_NO_OPTION);
            if (opcao == JOptionPane.YES_OPTION) {
                controlador.reiniciarPartida();
            }
        });
    }

    @Override
    public void onPartidaCarregada() {
        // estado já atualizado pelo controlador
    }
}
