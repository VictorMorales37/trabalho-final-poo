package Gui;

import Util.Macros;

import javax.swing.*;
import java.awt.*;

public class DialogoInicio extends JDialog {

    private int dificuldade = 1;
    private int mapa = 1;
    private boolean confirmado = false;

    public DialogoInicio(Frame parent) {
        super(parent, "Novo Jogo", true);
        setLayout(new BorderLayout(10, 10));
        setSize(360, 280);
        setLocationRelativeTo(parent);

        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));

        conteudo.add(new JLabel("Selecione a dificuldade:"));
        String[] dificuldades = {"Fácil (percepção 3)", "Médio (percepção 2)", "Difícil (percepção 1)"};
        JComboBox<String> comboDificuldade = new JComboBox<>(dificuldades);
        comboDificuldade.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboDificuldade.getPreferredSize().height));
        comboDificuldade.addActionListener(e -> dificuldade = comboDificuldade.getSelectedIndex() + 1);
        conteudo.add(comboDificuldade);
        conteudo.add(Box.createVerticalStrut(12));

        conteudo.add(new JLabel("Selecione o mapa:"));
        String[] mapas = new String[Macros.NUM_MAPAS];
        String[] nomes = {"Arena", "Labirinto", "Salas", "Corredores", "Grade"};
        for (int i = 0; i < Macros.NUM_MAPAS; i++) {
            mapas[i] = (i + 1) + " - " + nomes[i];
        }
        JComboBox<String> comboMapa = new JComboBox<>(mapas);
        comboMapa.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboMapa.getPreferredSize().height));
        comboMapa.addActionListener(e -> mapa = comboMapa.getSelectedIndex() + 1);
        conteudo.add(comboMapa);

        add(conteudo, BorderLayout.CENTER);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> {
            confirmado = false;
            dispose();
        });
        JButton btnIniciar = new JButton("Iniciar");
        btnIniciar.addActionListener(e -> {
            confirmado = true;
            dispose();
        });
        botoes.add(btnCancelar);
        botoes.add(btnIniciar);
        add(botoes, BorderLayout.SOUTH);
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public int getDificuldade() {
        return dificuldade;
    }

    public int getMapa() {
        return mapa;
    }
}
