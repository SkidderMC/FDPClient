package net.ccbluex.liquidbounce.event;

public class MultiTypeEvent extends Event {

    public EventType type;

    public MultiTypeEvent() {
        this.type = EventType.PRE;
    }

    public MultiTypeEvent(EventType type) {
        this.type = type;
    }

    public boolean isPre() {
        return this.type == EventType.PRE;
    }

    public boolean isPost() {
        return this.type == EventType.POST;
    }

}