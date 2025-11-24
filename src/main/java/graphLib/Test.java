package graphLib;

import graphLib.graph2D.GraphGrid2D;

public class Test {
    public static void main(String[] args) {

//        new BaseGUI<>(new GraphGUI() {
//            @Override
//            public JPanel getCustomControls() {
//                return new JPanel();
//            }
//
//            @Override
//            public KeyListener getKeyAdapter() {
//                return null;
//            }
//        }, new GraphGrid2D(0, 0, 1000, 1000));

        var graph = new GraphGrid2D(0,0,2000,2000);
        graph.show();
    }
}
