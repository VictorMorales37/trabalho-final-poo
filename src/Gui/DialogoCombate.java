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
import javax.swing.border.LineBorder;
import java.awt.*;

public class DialogoCombate extends JDialog {

    private final Jogador jogador;
    private final Dinossauro dinossauro;
    private final SistemaCombate sistemaCombate;
    private final Tabuleiro tabuleiro;

    private final JLabel lblJogador;
    private final JLabel lblInimigo;
    private final JProgressBar barraJogador;
    private final JProgressBar barraInimigo;
    private final int saudeMaxInimigo;
    private final JTextArea logCombate;
    private final JPanel painelAcoes;

    private ResultadoCombate resultado = null;

    public DialogoCombate(Frame parent, Jogador jogador, Dinossauro dinossauro,
                          SistemaCombate sistemaCombate, Tabuleiro tabuleiro) {
        super(parent, "Combate", true);
        this.jogador = jogador;
        this.dinossauro = dinossauro;
        this.sistemaCombate = sistemaCombate;
        this.tabuleiro = tabuleiro;
        this.saudeMaxInimigo = dinossauro.getSaude();

        setLayout(new BorderLayout(8, 8));
        setSize(480, 420);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(60, 40, 30));

        JPanel painelTopo = new JPanel(new GridLayout(1, 2, 12, 0));
        painelTopo.setBackground(new Color(60, 40, 30));
        painelTopo.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        JPanel painelJogador = criarPainelCombatente("Jogador", Color.BLUE);
        lblJogador = (JLabel) painelJogador.getClientProperty("label");
        barraJogador = (JProgressBar) painelJogador.getClientProperty("barra");
        barraJogador.setMaximum(Macros.SAUDE_JOGADOR);

        JPanel painelInimigo = criarPainelCombatente(nomeInimigo(dinossauro), Color.RED);
        lblInimigo = (JLabel) painelInimigo.getClientProperty("label");
        barraInimigo = (JProgressBar) painelInimigo.getClientProperty("barra");
        barraInimigo.setMaximum(saudeMaxInimigo);

        painelTopo.add(painelJogador);
        painelTopo.add(painelInimigo);
        add(painelTopo, BorderLayout.NORTH);

        logCombate = new JTextArea(8, 30);
        logCombate.setEditable(false);
        logCombate.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logCombate.setBackground(new Color(245, 235, 220));
        logCombate.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        JScrollPane scrollLog = new JScrollPane(logCombate);
        scrollLog.setBorder(new LineBorder(Color.BLACK, 2));
        add(scrollLog, BorderLayout.CENTER);

        painelAcoes = new JPanel(new GridLayout(2, 3, 6, 6));
        painelAcoes.setBackground(new Color(60, 40, 30));
        painelAcoes.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));

        painelAcoes.add(criarBotaoAcao("Atacar (mãos)", () -> executarAtaque(1)));
        painelAcoes.add(criarBotaoAcao("Bastão elétrico", () -> executarAtaque(2)));
        painelAcoes.add(criarBotaoAcao("Arma de dardos", () -> executarAtaque(3)));
        painelAcoes.add(criarBotaoAcao("Kit médico", this::executarCura));
        painelAcoes.add(criarBotaoAcao("Fugir", this::executarFuga));

        add(painelAcoes, BorderLayout.SOUTH);

        atualizarBarras();
        adicionarLog("Combate iniciado contra " + nomeInimigo(dinossauro) + "!");
        adicionarLog("Escolha sua ação.");
    }

    public ResultadoCombate aguardarResultado() {
        setVisible(true);
        return resultado;
    }

    private JPanel criarPainelCombatente(String nome, Color corBarra) {
        JPanel painel = new JPanel(new BorderLayout(4, 4));
        painel.setBackground(new Color(210, 180, 140));
        painel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.BLACK, 2),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel lbl = new JLabel(nome, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        painel.add(lbl, BorderLayout.NORTH);

        JProgressBar barra = new JProgressBar();
        barra.setForeground(corBarra);
        barra.setBackground(Color.WHITE);
        barra.setBorder(new LineBorder(Color.BLACK, 2));
        painel.add(barra, BorderLayout.CENTER);

        painel.putClientProperty("label", lbl);
        painel.putClientProperty("barra", barra);
        return painel;
    }

    private JButton criarBotaoAcao(String texto, Runnable acao) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.addActionListener(e -> acao.run());
        return btn;
    }

    private void executarAtaque(int tipo) {
        int dano = switch (tipo) {
            case 1 -> {
                if (!dinossauro.podeSerAtacadoSemArma()) {
                    adicionarLog("Não é possível atacar o T-Rex sem armas!");
                    yield 0;
                }
                yield sistemaCombate.atacarMao();
            }
            case 2 -> {
                Item bastao = jogador.pegarItem(Bastao.class);
                if (bastao == null) {
                    adicionarLog("Você ainda não tem bastão.");
                    yield 0;
                }
                yield bastao.usar(jogador, dinossauro);
            }
            case 3 -> {
                Item arma = jogador.pegarItem(ArmaDardos.class);
                if (arma == null) {
                    adicionarLog("Você ainda não tem arma de dardos.");
                    yield 0;
                }
                yield arma.usar(jogador, dinossauro);
            }
            default -> 0;
        };

        if (dano == 0) {
            adicionarLog("Ataque falhou ou foi bloqueado.");
        } else {
            adicionarLog("Você causou " + dano + " de dano!");
        }

        dinossauro.receberDano(dano);
        atualizarBarras();

        if (!dinossauro.estaVivo()) {
            adicionarLog("Você derrotou o dinossauro!");
            resultado = ResultadoCombate.VENCEU;
            encerrarCombate();
            return;
        }

        executarTurnoInimigo();
    }

    private void executarCura() {
        Item kit = jogador.pegarItem(KitMedico.class);
        if (kit == null) {
            adicionarLog("Você não tem kits médicos.");
            return;
        }
        kit.usar(jogador, dinossauro);
        if (kit instanceof Consumivel && ((Consumivel) kit).consumidoAposUso()) {
            jogador.removerItem(kit);
        }
        adicionarLog("Você usou um kit médico.");
        atualizarBarras();
        executarTurnoInimigo();
    }

    private void executarFuga() {
        sistemaCombate.fugir(jogador, tabuleiro);
        adicionarLog("Você fugiu do combate!");
        resultado = ResultadoCombate.FUGIU;
        encerrarCombate();
    }

    private void executarTurnoInimigo() {
        adicionarLog("O dinossauro tenta te atacar...");

        boolean passou = sistemaCombate.passouTestePercepcao(jogador);
        if (passou) {
            adicionarLog("Você conseguiu desviar!");
        } else if (dinossauro.getSimbolo() == Macros.SIMB_TREX) {
            adicionarLog("T-REX TE ATACOU!!! -4 de saúde!");
            jogador.setSaude(jogador.getSaude() - 4);
        } else {
            adicionarLog("O dinossauro te atacou! -" + dinossauro.getDanoAtaque() + " de saúde.");
            jogador.setSaude(jogador.getSaude() - dinossauro.getDanoAtaque());
        }

        atualizarBarras();

        if (!jogador.estaVivo()) {
            adicionarLog("Você foi derrotado...");
            resultado = ResultadoCombate.PERDEU;
            encerrarCombate();
        }
    }

    private void encerrarCombate() {
        for (Component c : painelAcoes.getComponents()) {
            c.setEnabled(false);
        }
        Timer timer = new Timer(800, e -> dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private void atualizarBarras() {
        int saudeJogador = Math.max(0, jogador.getSaude());
        lblJogador.setText("Jogador  HP: " + saudeJogador + "/" + Macros.SAUDE_JOGADOR);
        barraJogador.setValue(saudeJogador);

        int saudeInimigo = Math.max(0, dinossauro.getSaude());
        lblInimigo.setText(nomeInimigo(dinossauro) + "  HP: " + saudeInimigo + "/" + saudeMaxInimigo);
        barraInimigo.setValue(saudeInimigo);
    }

    private void adicionarLog(String msg) {
        logCombate.append("> " + msg + "\n");
        logCombate.setCaretPosition(logCombate.getDocument().getLength());
    }

    private static String nomeInimigo(Dinossauro d) {
        return switch (d.getSimbolo()) {
            case 'C' -> "Compsognato";
            case 'T' -> "Troodonte";
            case 'V' -> "Velociraptor";
            case 'R' -> "T-Rex";
            default -> "Dinossauro";
        };
    }
}
