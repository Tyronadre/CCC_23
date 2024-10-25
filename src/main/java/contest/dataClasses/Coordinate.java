package contest.dataClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Coordinate {
    private final int x;
    private final int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(String coordinate) {
        var cords = coordinate.split(",");
        x = Integer.parseInt(cords[0]);
        y = Integer.parseInt(cords[1]);
    }

    public boolean isAdjacent(Coordinate otherCoordinate) {
        if (otherCoordinate == null) {
            throw new RuntimeException("otherCoordinate is null");
        }
        if (this.equals(otherCoordinate)) {
            return true;
        }
        if (x == otherCoordinate.x && y == otherCoordinate.y) {
            return true;
        }
        if (Math.abs(x - otherCoordinate.x) <= 1 && y - otherCoordinate.y == 0) {
            return true;
        }
        if (Math.abs(y - otherCoordinate.y) <= 1 && x - otherCoordinate.x == 0) {
            return true;
        }
        return false;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Coordinate) obj;
        return this.x == that.x && this.y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + '|' + y + ')';
    }

    public List<Coordinate> getAdjacentCoordinates(int mapSize) {
        var coords = new ArrayList<Coordinate>();

        coords.add(new Coordinate(x - 1, y - 1));
        coords.add(new Coordinate(x + 1, y + 1));
        coords.add(new Coordinate(x - 1, y + 1));
        coords.add(new Coordinate(x + 1, y - 1));
        coords.add(new Coordinate(x - 1, y));
        coords.add(new Coordinate(x + 1, y));
        coords.add(new Coordinate(x, y - 1));
        coords.add(new Coordinate(x, y + 1));

        for (int i = 0; i < coords.size(); i++) {
            var coord = coords.get(i);
            if (coord.x < 0  || coord.x > mapSize|| coord.y < 0 || coord.y > mapSize) {
                coords.remove(i);
                i--;
            }
        }
        return coords;
    }
}
