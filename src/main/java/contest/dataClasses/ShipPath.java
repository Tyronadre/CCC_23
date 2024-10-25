package contest.dataClasses;

import java.util.ArrayList;
import java.util.List;

public class ShipPath {
    private List<Coordinate> path;

    public ShipPath(List<Coordinate> path) {
        this.path = path;
    }

    public ShipPath(String pathToParse) {
        path = new ArrayList<>();
        String[] coordinates = pathToParse.split(" ");
        for (String coordinate : coordinates) {
            path.add(new Coordinate(coordinate));
        }
    }


    public List<Coordinate> getPath() {
        return path;
    }

    public boolean selfIntersects() {
        //Two times the same coordinate
        for (int i = 0; i < path.size(); i++) {
            for (int j = i + 1; j < path.size(); j++) {
                if (path.get(i).equals(path.get(j))) {
                    return true;
                }
            }
        }
        //X Pattern
        for (int i = 0; i < path.size() - 1; i++) {
            var c1 = path.get(i);
            var c2 = path.get(i + 1);
            //not a diagonal
            if (!(Math.abs(c1.x() - c2.x()) == 1 && Math.abs(c1.y() - c2.y()) == 1)) {
                continue;
            }
            //calculate points for other diagonal
            var c3 = new Coordinate(c1.x(), c2.y());
            var c4 = new Coordinate(c2.x(), c1.y());
            //check if they are in the path
            if (path.contains(c3) && path.contains(c4) && Math.abs(path.indexOf(c3) - path.indexOf(c4)) == 1) {
                return true;
            }
        }
        return false;
    }

    public ShipPath append(ShipPath path) {
        if (!this.path.get(this.path.size() - 1).equals(path.getPath().get(0))) {
            throw new RuntimeException("Paths are not connected");
        }
        this.path.remove(this.path.size() - 1);
        this.path.addAll(path.getPath());
        return this;
    }

    public int length() {
        return path.size();
    }

    @Override
    public String toString() {
        return "ShipPath{" + "path=" + path + '}';
    }
}
