package graphLib.gui;

import javax.swing.*;
import java.awt.event.KeyListener;

public abstract class GraphGUI extends JPanel {
    public abstract JPanel getCustomControls();

    public abstract KeyListener getKeyAdapter();


    @Override
    public void reshape(int x, int y, int w, int h) {
        System.out.println("Reshape: " + x + ", " + y + ", " + w + ", " + h);
        super.reshape(x, y, w, h);
    }
}
