package graphLib.graph2D;

import graphLib.base.Graph;
import graphLib.base.Node;
import org.jetbrains.annotations.NotNull;

public class GraphGrid2D extends Graph<GraphGrid2D.Point> {

    protected GraphGrid2D() {
        super(point2DNode -> point2DNode.getData().hashCode());
    }

    public GraphGrid2D(int xStart, int yStart, int xEnd, int yEnd) {
        this();
        for (int x = xStart; x <= xEnd; x++) {
            for (int y = yStart; y <= yEnd; y++) {
                var newNode = getOrCreateNode(x, y);

                var topNode = getOrNullNode(x, y-1);
                if (topNode != null) newEdge(newNode, topNode);

                var leftNode = getOrNullNode(x-1, y);
                if (leftNode != null) newEdge(newNode, leftNode);
            }
        }
    }

    public Node<Point> getOrNullNode(int x, int y) {
        return getOrNullNode(new Point(x, y));
    }

    public Node<Point> getOrNullNode(Point point) {
        return nodes.get(point.hashCode());
    }

    public Node<Point> getOrCreateNode(int x, int y) {
        return getOrCreateNode(new Point(x, y));
    }

    public Node<Point> getOrCreateNode(Point point) {
        if (nodes.containsKey(point.hashCode())) return nodes.get(point.hashCode());
        return newNode(point);
    }

    public record Point(int x, int y) implements Comparable<Point> {
        @Override
        public int compareTo(@NotNull Point o) {
            return Math.abs(x - o.x) + Math.abs(y - o.y);
        }
    }
}
