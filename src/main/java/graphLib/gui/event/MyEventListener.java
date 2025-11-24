package graphLib.gui.event;

import java.util.EventListener;

public interface MyEventListener<T extends Event> extends EventListener {

    void handleEvent(T e);

}
