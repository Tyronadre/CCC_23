package contest_24.dataClasses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Lawn {
    int width;
    int height;
    List<Position> trees;

    public Lawn(int width, int height) {
        this.width = width;
        this.height = height;
        this.trees = new ArrayList<>();
    }

    public void parse(String[] lines) {
        for (int x = 0; x < lines.length; x++) {
            for (int y = 0; y < lines[x].length(); y++) {
                if (lines[x].charAt(y) == 'X') {
                    this.addTree(y, x);
                }
            }
        }
    }

    void addTree(int x, int y) {
        this.trees.add(new Position(x, y));
    }

    public boolean validatePath(Path path) {
        //check dimension
        int width = path.getWidth();
        int height = path.getHeight();
        if (width != this.width || height != this.height) {
            if (width > this.width || height > this.height) {
                return false;
            } else {
                System.out.println("Path is smaller than lawn!");
                System.out.println("Path width: " + width + " Path height: " + height + " Lawn width: " + this.width + " Lawn height: " + this.height);
            }
        }

        //shift path
        path.shift(-path.minX(), -path.minY());

        //check if path hits a tree
        PathNode current = path.start;
        Set<Position> pathPositions = new HashSet<>();
        while (current != null) {
            pathPositions.add(current.position);
            for (Position tree : this.trees) {
                if (tree.x == current.position.x && tree.y == current.position.y) {
                    return false;
                }
            }
            current = current.next;
        }

        //check if all fields are visited
        if (pathPositions.size() != this.getNumberOfUnoccupiedFields()) {
            return false;
        }


        return true;
    }

    private int getNumberOfUnoccupiedFields() {
        return this.width * this.height - this.trees.size();
    }

    @Override
    public String toString() {
        return "Lawn{" +
                "width=" + width +
                ", height=" + height +
                ", trees=" + trees +
                '}';
    }

    public boolean isTree(int xLawn, int yLawn) {
        for (Position tree : this.trees) {
            if (tree.x == xLawn && tree.y == yLawn) {
                return true;
            }
        }
        return false;
    }

    public boolean treeInRow(int y) {
        for (Position tree : this.trees) {
            if (tree.y == y) {
                return true;
            }
        }
        return false;
    }
}
