package graphLib.gui;

import graphLib.alg.Algorithm;
import graphLib.base.Graph;
import graphLib.gui.event.AlgorithmChangedEvent;
import graphLib.gui.event.AlgorithmDrawEvent;
import graphLib.gui.event.EventBus;
import graphLib.gui.event.Subscribe;

import javax.swing.*;
import java.awt.*;

public class BaseGUI<T extends Comparable<T>> extends JFrame {

    private Algorithm<T, ?> algorithm;
    private Graph<T> graph;

    private JButton startButton;
    private JButton pauseButton;
    private JSlider delaySlider;

    public BaseGUI(GraphGUI contentPanel, Graph<T> graph) {
        super("GraphLib");
        EventBus.instance.register(this);

        this.graph = graph;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1500, 1000);
        this.setLocationRelativeTo(null);

        var controlPanel = createControlPanel();
        controlPanel.add(contentPanel.getCustomControls());
        add(controlPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            this.validate();
            this.repaint();
            this.setVisible(true);
        });
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            algorithm.start();
            startButton.setEnabled(false);
        });

        delaySlider = new JSlider(0, 100, 0);
        delaySlider.createStandardLabels(10);
        delaySlider.addChangeListener(e ->
            algorithm.setDelay(delaySlider.getValue())
        );

        var stepButton = new JButton("Step");
        stepButton.setEnabled(false);
        stepButton.addActionListener(e -> algorithm.step());

        pauseButton = new JButton("Pause");
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

        controlPanel.add(startButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stepButton);
        controlPanel.add(new JLabel("Delay: "));
        controlPanel.add(delaySlider);

        controlPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        return controlPanel;
    }

    @Subscribe
    private void onAlgorithmDrawEvent(AlgorithmDrawEvent event) {
        SwingUtilities.invokeLater(this::repaint);
    }

    protected void setAlgorithm(Algorithm<T, ?> algorithm) {
        this.algorithm = algorithm;
        startButton.setEnabled(true);
        algorithm.setDelay(delaySlider.getValue());
        if (pauseButton.getText().equals("Resume")) algorithm.pause();

        EventBus.instance.post(new AlgorithmChangedEvent(algorithm));
    }
}
