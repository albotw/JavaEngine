package fr.albotw.Engine.events;

import fr.albotw.Engine.events.Event;
import fr.albotw.Engine.events.EventQueue;
import fr.albotw.Engine.events.ThreadID;

import java.util.HashMap;

public class EventDispatcher {
    private HashMap<ThreadID, EventQueue> channel;
    public static EventDispatcher instance = null;

    public static void createEventDispatcher() {
        if (instance == null) {
            instance = new EventDispatcher();
        }
    }

    private EventDispatcher() {
        this.channel = new HashMap<ThreadID, EventQueue>();
    }

    public void subscribe(ThreadID id, EventQueue handle) {
        this.channel.put(id, handle);
    }

    public void unsubscribe(ThreadID id) {
        this.channel.remove(id);
    }

    public void publish(Event e, ThreadID target) {
        try {
            this.channel.get(target).grab(e);
        } catch (Exception ex) {
            System.err.println("Erreur lors de l'envoi du message " + e.toString());
            ex.printStackTrace();
        }
    }

    public void publishToAll(Event e) {
        for (EventQueue handle : this.channel.values()) {
            handle.grab(e);
        }
    }
}
