package graphLib.base;

import java.util.*;
import java.util.function.Function;

public class Graph<T extends Comparable<T>> {
    private final Function<Node<T>, Integer> nodeHash;
    public Map<Integer, Node<T>> nodes = new HashMap<>();
    public Map<Integer, List<Node<T>>> outgoing = new HashMap<>();

    public Graph(){
        nodeHash = Node::getId;
    }

    public Graph(Function<Node<T>, Integer> nodeHash){
        this.nodeHash = nodeHash;
    }

    public void addNode(Node<T> node) {
        nodes.put(nodeHash.apply(node), node);
    }

    public Node<T> newNode() {
        var node = new Node<>(this);
        addNode(node);
        return node;
    }

    public Node<T> newNode(T data) {
        var node = new Node<>(this);
        node.setData(data);
        addNode(node);
        return node;
    }

    public Node<T> newNode(String label) {
        var node = new Node<>(this);
        node.setLabel(label);
        addNode(node);
        return node;
    }

    public void newBiEdge(Node<T> node1, Node<T> node2) {
        this.outgoing.computeIfAbsent(node1.getId(), k -> new ArrayList<>()).add(node2);
        this.outgoing.computeIfAbsent(node2.getId(), k -> new ArrayList<>()).add(node1);
    }

    public void show(){
        throw new UnsupportedOperationException("The base graph does not have a visualizer.");
    }

    public List<Node<T>> getNeighbors(Node<T> node) {
        return outgoing.getOrDefault(node.getId(), List.of());
    }

    public List<Node<T>> getAllNode() {
        return new ArrayList<>(nodes.values());
    }
}
