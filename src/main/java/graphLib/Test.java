package graphLib;

import graphLib.alg.AStar;
import graphLib.graph2D.GraphGrid2D;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Test {
    public static void main(String[] args) {
        var graph = new GraphGrid2D(0,0,100,100);

        var start = graph.getOrNullNode(10,10);
        var end = graph.getOrNullNode(50,50);

        var algo = new AStar<>(graph, start, end);

        var result = algo.getResult();
        algo.start();

        try {
            System.out.println(result.get(10, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }


    }
}
