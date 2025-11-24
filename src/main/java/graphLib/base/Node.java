package graphLib.base;

import java.util.List;

public class Node<T extends Comparable<T>> implements Comparable<Node<T>> {
    private final Graph<T> graph;
    private final int id;
    private static int idCounter = 0;
    private T data;
    private String label;

    protected Node(Graph<T> graph) {
        this.graph = graph;
        this.id = idCounter++;
    }

    public int getId() {
        return id;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int compareTo(Node<T> otherNode) {
        return this.data.compareTo(otherNode.data);
    }

    public List<Node<T>> getNeighbors() {
        return graph.getNeighbors(this);
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

    @Override
    public int hashCode() {
        return id;
    }
}
