package graphLib.alg;

import graphLib.base.Graph;
import graphLib.base.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class AStar<T extends Comparable<T>> extends Algorithm<T, List<Node<T>>> {
    private final Node<T> originNode;
    private final Node<T> destinationNode;
    private final PriorityQueue<Node<T>> openNodes;
    private final Map<Integer, Boolean> closedNodes;
    // concurrent set of nodes that are in a non-default state (open/closed/path/discovered)
    private final java.util.Set<Node<T>> interestingNodes = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    private final Map<Node<T>, Integer> gScore;
    private final Map<Node<T>, Integer> hScore;
    private final Map<Node<T>, Node<T>> parent;

    private boolean finished = false;
    private final List<Node<T>> path;


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
        path = new ArrayList<>();

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

        this.path.add(current);
        path.add(current);

        while (current != originNode) {
            current = parent.get(current);
            this.path.add(current);
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
        // mark origin as interesting
        interestingNodes.add(originNode);
        interestingNodes.add(destinationNode);
    }

    @Override
    protected boolean stepAlgorithm() {
        if (openNodes.isEmpty()) {
            result.complete(null);
            finished = true;
            return true;
        }

        var current = openNodes.remove();
        closedNodes.put(current.getId(), true);
        interestingNodes.add(current);

        if (current.equals(destinationNode)) {
            result.complete(reconstructPath(current));
            finished = true;
            return true;
        }

        for (var neighbor : current.getNeighbors()) {
            if (closedNodes.getOrDefault(neighbor.getId(), false)) continue;

            //get the g score from the neighbor with a path over this node
            var neighborGScore = gScoreFunction.apply(current, neighbor, gScore.get(current));

            //this path is better
            parent.put(neighbor, current);
            gScore.put(neighbor, neighborGScore);
            hScore.put(neighbor, hScoreFunction.apply(neighbor, destinationNode));
            interestingNodes.add(neighbor);

            if (!openNodes.contains(neighbor)) openNodes.add(neighbor);
        }
        return false;
    }

    @Override
    public void draw(Graphics2D g2d, Node<T> node) {
        if (finished) {
            if (path.contains(node)) g2d.setColor(Color.DARK_GRAY);
            else g2d.setColor(Color.LIGHT_GRAY);
        } else {
            if (!closedNodes.containsKey(node.getId())) {
                if (openNodes.contains(node)) g2d.setColor(Color.GREEN);
                else g2d.setColor(Color.lightGray);
            } else if (closedNodes.get(node.getId())) g2d.setColor(Color.RED);
        }
        g2d.fill(g2d.getClip());

        g2d.setColor(Color.BLACK);
        g2d.drawString("id: " + node.getId(), 0, 10);
        g2d.drawString("h: " + hScore.get(node), 0, 20);
        g2d.drawString("g: " + gScore.get(node), 0, 30);
        g2d.drawString("p: " + parent.get(node), 0, 40);
    }

    @Override
    protected NodeRenderState nodeRenderState(Node<T> node) {
        if (finished) {
            if (path.contains(node)) return NodeRenderState.PATH;
            return NodeRenderState.FINISHED;
        }
        if (closedNodes.getOrDefault(node.getId(), false)) return NodeRenderState.CLOSED;
        if (openNodes.contains(node)) return NodeRenderState.OPEN;
        return NodeRenderState.DEFAULT;
    }

    @Override
    public Iterable<Node<T>> getInterestingNodes() {
        return interestingNodes;
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
