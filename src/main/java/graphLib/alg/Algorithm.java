package graphLib.alg;

import graphLib.base.Graph;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;

public abstract class Algorithm<T extends Comparable<T>, V> {
    Graph<T> graph;
    boolean pause;
    boolean step;
    CompletableFuture<V> result = new CompletableFuture<>();

    public Algorithm(Graph<T> graph) {
        this.graph = graph;
    }

    /**
     * @return a completable future for the result of this algorithm;
     */
    public CompletableFuture<V> getResult() {
        return result;
    }

    /**
     * Pauses the algorithm
     */
    public void pause() {
        this.pause = true;
    }

    /**
     * Steps one time, if the algorithm is paused
     */
    public void step() {
        if (pause)
            this.step = true;
    }

    /**
     * Continues the execution of the algorithm
     */
    public void resume() {
        this.pause = false;
    }


    /**
     * Starts the given algorithm in a new Thread.
     */
    public void start() {
        new Thread(() -> {
            initAlgorithm();
            try {
                boolean stop = false;
                while (!stop)
                    if (pause) {
                        if (step) {
                            stop = Algorithm.this.stepAlgorithm();
                            step = false;
                        } else {
                            Thread.sleep(100);
                        }
                    } else {
                        stop = Algorithm.this.stepAlgorithm();
                    }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Initialization for the algorithm, if this cannot be handled in the constructor.
     * Will be called ONCE when the algorithm is started.
     */
    protected abstract void initAlgorithm();

    /**
     * Main Method for the algorithm. each time this method is called, the algorithm should calculate one step of its
     * execution.
     * Returns true when the algorithm terminated after this step, otherwise false
     */
    protected abstract boolean stepAlgorithm();

    /**
     * Graphical representation of the algorithm
     * @return the jpanel
     */
    public JPanel getGUI() {
        return null;
    }

    /**
     *
     * @return the graph this algorithm runs on
     */
    public Graph<T> getGraph() {
        return graph;
    }
}
