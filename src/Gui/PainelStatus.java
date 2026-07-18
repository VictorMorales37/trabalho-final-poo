package Gui;

import Entidades.Personagens.Jogador;
import Itens.Item;
import Util.Macros;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class PainelStatus extends JPanel {

    private final JLabel lblSaude;
    private final JProgressBar barraSaude;
    private final DefaultListModel<String> modeloInventario;
    private final JList<String> listaInventario;

    public PainelStatus() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(210, 180, 140));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(220, 500));

        JLabel titulo = new JLabel("Status do personagem");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 16));
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(titulo);
        add(Box.createVerticalStrut(12));

        lblSaude = new JLabel("saúde: 0/5");
        lblSaude.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblSaude.setForeground(Color.RED);
        lblSaude.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(lblSaude);
        add(Box.createVerticalStrut(6));

        barraSaude = new JProgressBar(0, Macros.SAUDE_JOGADOR);
        barraSaude.setForeground(Color.RED);
        barraSaude.setBackground(Color.WHITE);
        barraSaude.setBorder(new LineBorder(Color.BLACK, 3));
        barraSaude.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        barraSaude.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(barraSaude);
        add(Box.createVerticalStrut(16));

        JPanel painelInventario = new JPanel(new BorderLayout());
        painelInventario.setBackground(new Color(210, 180, 140));
        painelInventario.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                "inventario",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 13)
        ));
        painelInventario.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelInventario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        modeloInventario = new DefaultListModel<>();
        listaInventario = new JList<>(modeloInventario);
        listaInventario.setBackground(new Color(245, 222, 179));
        listaInventario.setFont(new Font("SansSerif", Font.PLAIN, 13));
        painelInventario.add(new JScrollPane(listaInventario), BorderLayout.CENTER);
        add(painelInventario);

        add(Box.createVerticalGlue());
    }

    public void atualizar(Jogador jogador) {
        if (jogador == null) {
            lblSaude.setText("saúde: -/-");
            barraSaude.setValue(0);
            modeloInventario.clear();
            return;
        }

        int saude = Math.max(0, jogador.getSaude());
        lblSaude.setText("saúde: " + saude + "/" + Macros.SAUDE_JOGADOR);
        barraSaude.setValue(saude);

        modeloInventario.clear();
        for (Item item : jogador.getInventario().getItens()) {
            modeloInventario.addElement(item.getNome());
        }
    }
}
