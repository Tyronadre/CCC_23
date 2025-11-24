package contest;

import contest.dataClasses.*;
import util.Framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class T5 {
    public static void main(String[] args) {
        final int level = 5;
        for (int i = 0; i <= 0; i++) {
            var lines = Framework.readFileString(level, i).split("\r\n");

            int mapSize = Integer.parseInt(lines[0]);
            PirateMap map = new PirateMap(mapSize, Arrays.copyOfRange(lines, 1, mapSize + 1));
            map.parseIslands();
            int numberOfRoutesToPlan = Integer.parseInt(lines[mapSize + 1]);

            List<ShipPath> shipPaths = new ArrayList<>();
            for (int j = 0; j < numberOfRoutesToPlan; j++)
                shipPaths.add(new ShipPathFinder3(map.getIsland(new Coordinate(lines[mapSize + 2 + j])), map).findPath());

            var sb = new StringBuilder();
            for (ShipPath shipPath : shipPaths) {
                System.out.println(shipPath.getPath());
                for (var coord : shipPath.getPath()) {
                    sb.append(coord.x()).append(",").append(coord.y()).append(" ");
                }
                sb.replace(sb.length() - 1, sb.length(), "\n");
            }


            Framework.writeOutput(level, i, sb.toString());
        }
    }
}
