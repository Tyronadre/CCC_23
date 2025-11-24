package graphLib.gui;

import graphLib.alg.Algorithm;

import javax.swing.*;
import java.awt.*;

public class BaseGUI<T extends Comparable<T>,V> extends JFrame {
    private final Algorithm<T,V> algorithm;

    public BaseGUI(Algorithm<T,V> algorithm) {
        super("GraphLib");

        this.algorithm = algorithm;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);

        createContentPanel();

        this.setVisible(true);
    }

    private void createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.PAGE_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(controlPanel, BorderLayout.PAGE_START);

        var stepButton = new JButton("Step");
        stepButton.setEnabled(false);
        stepButton.addActionListener(e -> {
            algorithm.step();
        });

        var pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> {
            if (pauseButton.getText().equals("Pause")) {
                pauseButton.setText("Resume");
                algorithm.pause();
                stepButton.setEnabled(true);
            } else if (pauseButton.getText().equals("Resume")) {
                pauseButton.setText("Pause");
                algorithm.resume();
                stepButton.setEnabled(false);
            }
        });

        controlPanel.add(pauseButton);
        controlPanel.add(stepButton);

        var algoGUI = algorithm.getGUI();
        if (algoGUI != null) {
            panel.add(algoGUI, BorderLayout.CENTER);
        } else {
            panel.add(new JLabel("No GUI available"), BorderLayout.PAGE_START);
        }
    }
}
