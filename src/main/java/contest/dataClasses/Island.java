package contest.dataClasses;

import java.util.ArrayList;
import java.util.List;

public class Island {
    List<Coordinate> coordinatesList;

    public Island(Coordinate coordinate) {
        coordinatesList = new ArrayList<>();
        coordinatesList.add(coordinate);
    }

    public boolean isAdjacent(Coordinate otherCoordinate) {
        for (var coordinate : coordinatesList) {
            if (coordinate.isAdjacent(otherCoordinate)) {
                return true;
            }
        }
        return false;
    }

    public void UnionIslands(Island island) {
        this.coordinatesList.addAll(island.getCoodinatesList());
    }

    public List<Coordinate> getCoodinatesList() {
        return coordinatesList;
    }

    public void addCoordinate(Coordinate newC) {
        coordinatesList.add(newC);
    }


    @Override
    public String toString() {
        return "Island{" +
            "coordinatesList=" + coordinatesList +
            '}';
    }
}
