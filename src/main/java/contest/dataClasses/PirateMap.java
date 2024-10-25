package contest.dataClasses;

import java.util.ArrayList;
import java.util.List;

enum MapData {
    WATER,
    LAND
}

public class PirateMap {
    int size;
    List<Island> islands = new ArrayList<>();
    MapData[][] map;

    public PirateMap(int size) {
        this.size = size;
        map = new MapData[size][size];
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                map[x][y] = MapData.LAND;
            }
        }
    }

    public PirateMap(int size, String[] mapToPars) {
        this.size = size;
        this.map = new MapData[size][size];
        for (int x = 0; x < size; x++) {
            var line = mapToPars[x];
            for (int y = 0; y < size; y++) {
                map[x][y] = line.charAt(y) == 'W' ? MapData.WATER : MapData.LAND;
            }
        }
    }

    public MapData get(int x, int y) {
        return map[y][x];
    }

    public void parseIslands() {
        islands = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                var newC = new Coordinate(x, y);
                if (get(newC) == MapData.WATER) {
                    continue;
                }
                List<Island> adjacentIslands = new ArrayList<>();

                for (var island : islands)
                    if (island.isAdjacent(newC))
                        adjacentIslands.add(island);


                if (adjacentIslands.size() == 0)
                    islands.add(new Island(newC));
                else if (adjacentIslands.size() == 1)
                    adjacentIslands.get(0).addCoordinate(newC);
                else
                    for (int i = 1; i < adjacentIslands.size(); i++) {
                        Island adjacentIsland = adjacentIslands.get(i);
                        adjacentIslands.get(0).UnionIslands(adjacentIsland);
                        islands.remove(adjacentIsland);
                    }
                adjacentIslands.get(0).addCoordinate(newC);

            }
        }
    }

    MapData get(Coordinate newC) {
        return get(newC.x(), newC.y());
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Map: ");

        for (int x = 0; x < size; x++) {
            sb.append("\n");
            for (int y = 0; y < size; y++) {
                sb.append(map[x][y] == MapData.WATER ? "W" : "L");
            }
        }

        sb.append("Number Of Islands: ");
        sb.append(islands.size());
        return sb.toString();
    }

    public Island getIsland(Coordinate coordinate) {
        for (var island : islands) {
            if (island.getCoodinatesList().contains(coordinate)) {
                return island;
            }
        }
        return null;
    }

    public void setTileWater(Coordinate adjacentIslandCoords) {
        map[adjacentIslandCoords.y()][adjacentIslandCoords.x()] = MapData.WATER;
    }
}
