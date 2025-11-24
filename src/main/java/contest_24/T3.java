package contest_24;

import contest_24.dataClasses.Lawn;
import contest_24.dataClasses.Path;
import contest_24.dataClasses.PathNode;
import util.Framework;

import java.util.ArrayList;
import java.util.Arrays;

public class T3 {
    public static void main(String[] args) {
        final int level = 3;
        for (int i = 0; i <= 5; i++) {
            var lines = Framework.readFileString(level, i).split("\r\n");
            int numberOfLines = Integer.parseInt(lines[0]);

            var output = new ArrayList<String>();
            lines = Arrays.copyOfRange(lines, 1, lines.length);
            //get an array with only lawn data for this subinput
            for (int subInput = 0; subInput < numberOfLines; subInput++) {
                var lawnSize = lines[0].split(" ");
                var lawn = new Lawn(Integer.parseInt(lawnSize[0]), Integer.parseInt(lawnSize[1]));
                var lawnData = Arrays.copyOfRange(lines, 1, 1 + Integer.parseInt(lawnSize[1]));
                lawn.parse(lawnData);
                lines = Arrays.copyOfRange(lines, 1 + Integer.parseInt(lawnSize[1]), lines.length);
                //createPath
                Path p = Path.createPath(lines[0]);
                lines = Arrays.copyOfRange(lines, 1, lines.length);
                System.out.println("Path: " + p);
                System.out.println("Lawn: " + lawn);
                System.out.println("val path: " + p.validate());
                System.out.println("val lawn: " + lawn.validatePath(p));


                if (p.validate() && lawn.validatePath(p)) {
                    output.add("VALID");
                } else {
                    output.add("INVALID");
                }

                //check if path fits in lawn, if yes shift
            }


            var stringBuilder = new StringBuilder();
            for (var path : output) {
                stringBuilder.append(path).append("\n");
            }
            Framework.writeOutput(level, i, stringBuilder.toString());
        }
    }
}
