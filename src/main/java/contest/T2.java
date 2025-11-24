package contest;

import contest.dataClasses.Coordinate;
import contest.dataClasses.PirateMap;
import util.Framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class T2 {

    public static void main(String[] args) {
        final int level = 2;
        for (int i = 1; i <= 5; i++) {
            var lines = Framework.readFileString(level, i).split("\r\n");

            int mapsize = Integer.parseInt(lines[0]);
            PirateMap map = new PirateMap(mapsize, Arrays.copyOfRange(lines, 1, mapsize + 1));

            int numberOfCordsPairs = Integer.parseInt(lines[mapsize + 1]);

            List<Coordinate[]> cordPairs = new ArrayList<>();
            for (int j = 0; j < numberOfCordsPairs; j++) {
                var cord = lines[mapsize + 2 + j].split(" ");
                cordPairs.add(new Coordinate[]{new Coordinate(cord[0]), new Coordinate(cord[1])});
            }

            map.parseIslands();
            System.out.println(map);

            var stringBuilder = new StringBuilder();
            for (Coordinate[] coordinatePair : cordPairs) {
                var island1 = map.getIsland(coordinatePair[0]);
                var island2 = map.getIsland(coordinatePair[1]);
                System.out.print(Arrays.toString(coordinatePair) + " " + island1 + " " + island2);
                if (island1 == null || island2 == null) {
                    System.out.println("DIFFERENT");
                    stringBuilder.append("DIFFERENT\n");
                    System.err.println("THIS SHOULD NOT HAPPEN");
                }
                else if (island1.equals(island2)) {
                    System.out.println("SAME");
                    stringBuilder.append("SAME\n");

                } else {
                    System.out.println("DIFFERENT");
                    stringBuilder.append("DIFFERENT\n");
                }
            }
            Framework.writeOutput(level, i, stringBuilder.toString());
        }
    }
}
