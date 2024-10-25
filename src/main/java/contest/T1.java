package contest;

import framework.Framework;

import java.awt.*;
import java.util.stream.Stream;

public class T1 {
    public static void main(String[] args) {
        final int level = 1;
        for (int i = 0; i <= 5; i++) {
            var lines = Framework.readInput(level, i).split("\r\n");
            int mapsize = Integer.parseInt(lines[0]);
            Character[][] map = new Character[mapsize][mapsize];
            for (int j = 0; j < mapsize; j++) {
                for (int k = 0; k < mapsize; k++) {
                    map[j][k] = lines[j + 1].charAt(k);
                }
            }
            int numberOfCords = Integer.parseInt(lines[mapsize + 1]);
            int[][] cords = new int[numberOfCords][2];
            for (int j = 0; j < numberOfCords; j++) {
                var cord = lines[mapsize + 2 + j].split(",");
                cords[j][0] = Integer.parseInt(cord[0]);
                cords[j][1] = Integer.parseInt(cord[1]);
            }
            Framework.writeOutput(level, i, resolveCoords(map, cords));
        }
    }

    private static String resolveCoords(Character[][] map, int[][] cords) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cords.length; i++) {
            var cord = cords[i];
            var res = resolveCoord(map, cord[1], cord[0]);
            System.out.println(res);
            sb.append(res).append("\n");
        }
        return sb.toString();
    }

    private static Character resolveCoord(Character[][] map, int x, int y) {
        return map[x][y];
    }
}
