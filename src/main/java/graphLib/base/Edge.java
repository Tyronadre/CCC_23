package graphLib.base;

public class Edge<T extends Comparable<T>> {
    private final Node<T> source;
    private final Node<T> target;
    private double weight;

    protected Edge(Node<T> source, Node<T> target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    protected Edge(Node<T> source, Node<T> target) {
        this(source, target, 1);
    }

    public Node<T> getSource() {
        return source;
    }

    public Node<T> getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
