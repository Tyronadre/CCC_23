package contest_24;

import kotlin.Pair;

import java.util.ArrayList;

public class T2 {

    public static void main(String[] args) {
        final int level = 2;
        for (int i = 0; i <= 5; i++) {
            var lines = framework.Framework.readInput(level, i).split("\r\n");
            int numberOfLines = Integer.parseInt(lines[0]);
            var output = new ArrayList<Pair<Integer, Integer>>();
            for (int j = 1; j <= numberOfLines; j++) {
                output.add(getMaxRecSize(lines[j]));
                System.out.println("Line: " + lines[j] );
            }
            var stringBuilder = new StringBuilder();
            for (var path : output) {
                stringBuilder.append(path.getFirst()).append(" ").append(path.getSecond()).append("\n");
            }
            framework.Framework.writeOutput(level, i, stringBuilder.toString());
        }
    }

    private static Pair<Integer, Integer> getMaxRecSize(String line) {
        int maxX = 0;
        int maxY = 0;
        int minX = 0;
        int minY = 0;

        int posx = 0;
        int posy = 0;

        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == 'W') {
                posy++;
            } else if (line.charAt(i) == 'A') {
                posx--;
            } else if (line.charAt(i) == 'D') {
                posx++;
            } else if (line.charAt(i) == 'S') {
                posy--;
            }
            if (posx > maxX) {
                maxX = posx;
            }
            if (posx < minX) {
                minX = posx;
            }
            if (posy > maxY) {
                maxY = posy;
            }
            if (posy < minY) {
                minY = posy;
            }
        }

        System.out.println("MaxX: " + maxX + " MinX: " + minX + " MaxY: " + maxY + " MinY: " + minY + "rec: " + (maxX + Math.abs(minX)) + " " + (maxY + Math.abs(minY)));
        return new Pair<>(maxX + Math.abs(minX) + 1, maxY + Math.abs(minY) + 1);
    }
}
