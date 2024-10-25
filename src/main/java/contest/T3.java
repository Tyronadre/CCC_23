package contest;

import contest.dataClasses.ShipPath;
import framework.Framework;

import java.util.ArrayList;
import java.util.List;

public class T3 {
    public static void main(String[] args) {
        final int level = 3;
        for (int i = 0; i <= 5; i++) {
            var lines = Framework.readInput(level, i).split("\r\n");

            int mapSize = Integer.parseInt(lines[0]);

            int numberOfShipPaths = Integer.parseInt(lines[mapSize + 1]);

            List<ShipPath> shipPaths = new ArrayList<>();
            for (int j = 0; j < numberOfShipPaths; j++) {
                shipPaths.add(new ShipPath(lines[mapSize + 2 + j]));
            }

            var sb = new StringBuilder();
            for (ShipPath shipPath : shipPaths) {
                System.out.println(shipPath.getPath());
                System.out.println(shipPath.selfIntersects());
                if (!shipPath.selfIntersects()) {
                    sb.append("VALID\n");
                } else {
                    sb.append("INVALID\n");
                }
            }


            Framework.writeOutput(level, i, sb.toString());
        }
    }
}
