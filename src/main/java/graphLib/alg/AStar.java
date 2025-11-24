package graphLib.alg;

import graphLib.base.Graph;
import graphLib.base.Node;
import graphLib.graph2D.GraphGrid2D;
import graphLib.gui.AStar2DGUI;

import javax.swing.*;
import java.util.*;

public class AStar<T extends Comparable<T>> extends Algorithm<T, List<Node<T>>> {
    private final Node<T> originNode;
    private final Node<T> destinationNode;
    private final PriorityQueue<Node<T>> openNodes;
    private final Map<Integer, Boolean> closedNodes;

    private final Map<Node<T>, Integer> gScore;
    private final Map<Node<T>, Integer> hScore;
    private final Map<Node<T>, Node<T>> parent;

    private GScoreFunction<T> gScoreFunction;
    private HScoreFunction<T> hScoreFunction;


    /**
     * GScore -> cost from origin
     * HScore -> cost to destination
     */
    public AStar(Graph<T> graph, Node<T> origin, Node<T> destination) {
        super(graph);
        this.originNode = origin;
        this.destinationNode = destination;

        openNodes = new PriorityQueue<>((o1, o2) -> getFScore(o1) - getFScore(o2));
        closedNodes = new HashMap<>();
        gScore = new HashMap<>();
        hScore = new HashMap<>();
        parent = new HashMap<>();

        this.gScoreFunction = (currentNode, nextNode, gScoreCurrentNode) -> gScoreCurrentNode + 1;
        this.hScoreFunction = Node::compareTo;
    }

    /**
     * Sets the function for calculating the g score for a neighboring node.
     * The first node is the current node, the second the next Node.
     *
     * @param gScoreFunction the function
     */
    public void setGScoreFunction(GScoreFunction<T> gScoreFunction) {
        this.gScoreFunction = gScoreFunction;
    }

    /**
     * Sets the function for calculating the h score for a node to the destination node.
     * The first node is the current node, the second is the destination node.
     *
     * @param hScoreFunction the function
     */
    public void setHScoreFunction(HScoreFunction<T> hScoreFunction) {
        this.hScoreFunction = hScoreFunction;
    }

    private List<Node<T>> reconstructPath(Node<T> current) {
        List<Node<T>> path = new ArrayList<>();
        path.add(current);
        while (current != originNode) {
            current = parent.get(current);
            path.add(current);
        }
        return path;
    }

    private int getFScore(Node<T> node) {
        return getGScore(node) + getHScore(node);
    }

    private int getGScore(Node<T> node) {
        return gScore.getOrDefault(node, Integer.MAX_VALUE);
    }

    private int getHScore(Node<T> node) {
        return destinationNode.compareTo(node);
    }

    @Override
    protected void initAlgorithm() {
        parent.put(originNode, null);
        openNodes.add(originNode);
        gScore.put(originNode, 0);
        hScore.put(originNode, hScoreFunction.apply(originNode, destinationNode));
    }

    @Override
    protected boolean stepAlgorithm() {
        if (openNodes.isEmpty()) {
            result.complete(null);
            return true;
        }

        var current = openNodes.remove();
        System.out.println("Current node: " + current);
        closedNodes.put(current.getId(), true);

        if (current.equals(destinationNode)) {
            result.complete(reconstructPath(current));
            return true;
        }

        for (var neighbor : current.getNeighbors()) {
            if (closedNodes.getOrDefault(neighbor.getId(), false)) continue;

            //get the g score from the neighbor with a path over this node
            var neighborGScore = gScoreFunction.apply(current, neighbor, gScore.get(current));

            if (!openNodes.contains(neighbor)) openNodes.add(neighbor);
            else if (neighborGScore > gScore.get(neighbor)) continue;

            //this path is better
            parent.put(neighbor, current);
            gScore.put(neighbor, neighborGScore);
            hScore.put(neighbor, hScoreFunction.apply(neighbor, destinationNode));
        }
        return false;
    }

    @Override
    public JPanel getGUI() {
        if (this.graph instanceof GraphGrid2D) return new AStar2DGUI((AStar<GraphGrid2D.Point>) this);
        else return null;
    }

    public interface GScoreFunction<T extends Comparable<T>> {
        /**
         * Calculates the GScore for the next node
         *
         * @param currentNode       the current node
         * @param nextNode          the next node
         * @param gScoreCurrentNode the g score that the current node has
         * @return the g score for the next node
         */
        int apply(Node<T> currentNode, Node<T> nextNode, int gScoreCurrentNode);
    }

    public interface HScoreFunction<T extends Comparable<T>> {
        /**
         * Calculates the HScore for the current node
         *
         * @param currentNode the current node
         * @param finalNode   the final node
         * @return the h score for the current node
         */
        int apply(Node<T> currentNode, Node<T> finalNode);
    }
}
