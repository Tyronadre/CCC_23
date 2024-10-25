package contest.dataClasses;

import java.util.ArrayList;

public class ShipPathFinder2 {

    Island island;
    PirateMap map;

    public ShipPathFinder2(Island island, PirateMap map) {
        this.island = island;
        this.map = map;
    }

    public ShipPath findPath() {
        PirateMap waterMap = new PirateMap(map.size);
        Coordinate startCoord = null;

        int numberOfWaterTiles = 0;
        for (var islandCoord : island.getCoodinatesList()) {
            for (var adjacentIslandCoords : islandCoord.getAdjacentCoordinates(map.size)) {
                if (map.get(adjacentIslandCoords) == MapData.WATER) {
                    waterMap.setTileWater(adjacentIslandCoords);
                    if (startCoord == null) {
                        startCoord = adjacentIslandCoords;
                    }
                    numberOfWaterTiles++;
                }
            }
        }
        //Find a path through the waterMap
        var path = new ArrayList<Coordinate>();
        path.add(startCoord);
        var visited = new ArrayList<Coordinate>();
        visited.add(startCoord);
        var currentCoord = startCoord;
        while (true) {
            var adjacentCoords = currentCoord.getAdjacentCoordinates(map.size);
            boolean stepDone = false;
            for (var adjacentCoord : adjacentCoords) {
                if (adjacentCoord.equals(startCoord) && path.size() > 2) return new ShipPath(path);
                else if (path.size() > 3 && euclidianDistance(adjacentCoord, startCoord) < 2) {
                    path.add(adjacentCoord);
                    currentCoord = adjacentCoord;
                }
                else if (waterMap.get(adjacentCoord) == MapData.WATER && !visited.contains(adjacentCoord)) {
                    if (!stepDone) {
                        currentCoord = adjacentCoord;
                        path.add(adjacentCoord);
                        stepDone = true;
                    }
                    visited.add(adjacentCoord);
                }

                System.out.println(adjacentCoord);
            }
        }
    }

    private double euclidianDistance(Coordinate c1, Coordinate c2) {
        return Math.sqrt(Math.pow(c1.x() - c2.x(), 2) + Math.pow(c1.y() - c2.y(), 2));
    }
}
