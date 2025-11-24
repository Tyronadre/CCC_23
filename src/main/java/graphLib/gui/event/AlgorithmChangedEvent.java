package graphLib.gui.event;

import graphLib.alg.Algorithm;

public class AlgorithmChangedEvent implements Event {
    private final Algorithm<?,?>  algorithm;

    public AlgorithmChangedEvent(Algorithm<?,?> algorithm) {
        this.algorithm = algorithm;
    }

    public Algorithm<?,?> getAlgorithm() {
        return algorithm;
    }
}
