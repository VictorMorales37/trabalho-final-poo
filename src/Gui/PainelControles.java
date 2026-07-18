package Gui;

import Util.Direcao;

import javax.swing.*;
import java.awt.*;

public class PainelControles extends JPanel {

    public interface AcaoListener {
        void onMover(Direcao direcao);
        void onCurar();
    }

    private AcaoListener listener;

    public PainelControles() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(210, 180, 140));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel titulo = new JLabel("Movimento");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titulo);
        add(Box.createVerticalStrut(8));

        JPanel dPad = new JPanel(new GridBagLayout());
        dPad.setBackground(new Color(210, 180, 140));
        dPad.setAlignmentX(Component.CENTER_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 1; gbc.gridy = 0;
        dPad.add(criarBotaoMovimento("↑", Direcao.CIMA), gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        dPad.add(criarBotaoMovimento("←", Direcao.ESQUERDA), gbc);
        gbc.gridx = 2; gbc.gridy = 1;
        dPad.add(criarBotaoMovimento("→", Direcao.DIREITA), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        dPad.add(criarBotaoMovimento("↓", Direcao.BAIXO), gbc);

        add(dPad);
        add(Box.createVerticalStrut(12));

        JButton btnCurar = new JButton("Usar kit médico");
        btnCurar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCurar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnCurar.addActionListener(e -> {
            if (listener != null) listener.onCurar();
        });
        add(btnCurar);
    }

    public void setListener(AcaoListener listener) {
        this.listener = listener;
    }

    private JButton criarBotaoMovimento(String texto, Direcao direcao) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setPreferredSize(new Dimension(52, 52));
        btn.addActionListener(e -> {
            if (listener != null) listener.onMover(direcao);
        });
        return btn;
    }
}
