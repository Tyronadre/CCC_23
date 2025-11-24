package graphLib.gui.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventBus {
    public static final EventBus instance = new EventBus();

    private EventBus() {}

    private final Map<Class<?>, List<Handler>> handlers = new ConcurrentHashMap<>();

    public void register(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {

            // Check annotation
            if (!method.isAnnotationPresent(Subscribe.class)) continue;

            // Check method parameter count
            if (method.getParameterCount() != 1) {
                throw new IllegalArgumentException("Event handler must have exactly one parameter: " + method);
            }

            Class<?> eventType = method.getParameterTypes()[0];

            // Make accessible
            method.setAccessible(true);

            // Store handler
            handlers.computeIfAbsent(eventType, c -> new ArrayList<>()).add(new Handler(listener, method));
        }
    }

    public void unregister(Object listener) {
        for (List<Handler> list : handlers.values()) {
            list.removeIf(h -> h.listener == listener);
        }
    }

    public void post(Object event) {
        Class<?> eventType = event.getClass();

        // Dispatch handlers for exact class
        List<Handler> list = handlers.get(eventType);
        if (list != null) {
            for (Handler h : list) {
                h.invoke(event);
            }
        }
    }

    private record Handler(Object listener, Method method) {

        void invoke(Object event) {
            try {
                method.invoke(listener, event);
            } catch (Exception e) {
                throw new RuntimeException("Error while invoking event handler", e);
            }
        }
    }
}

