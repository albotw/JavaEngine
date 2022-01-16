package fr.albotw.Engine.events;

public abstract class Event {
    protected ThreadID sender;

    public Event() {
        sender = null;
    }

    public Event(ThreadID sender) {
        this.sender = sender;
    }

    public ThreadID getSender() {
        return this.sender;
    }

    public void setSender(ThreadID sender) {
        this.sender = sender;
    }
}
