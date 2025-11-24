package contest;

import contest.dataClasses.Coordinate;
import contest.dataClasses.PirateMap;
import contest.dataClasses.ShipPath;
import contest.dataClasses.ShipPathFinder;
import util.Framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class T4 {
    public static void main(String[] args) {
        final int level = 4;
        for (int i = 0; i <= 5; i++) {
            var lines = Framework.readFileString(level, i).split("\r\n");

            int mapSize = Integer.parseInt(lines[0]);
            PirateMap map = new PirateMap(mapSize, Arrays.copyOfRange(lines, 1, mapSize + 1));
            int numberOfRoutesToPlan = Integer.parseInt(lines[mapSize + 1]);

            List<ShipPath> shipPaths = new ArrayList<>();
            for (int j = 0; j < numberOfRoutesToPlan; j++) {
                var cords = lines[mapSize + 2 + j].split(" ");

                shipPaths.add(new ShipPathFinder(new Coordinate(cords[0]), new Coordinate(cords[1]), map).findPath());
            }

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
