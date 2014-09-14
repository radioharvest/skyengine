package aq.oceanbase.skyscroll.logic.tree;

public class NodeConnection {
    public static enum CONNECTIONSTATE {
        IDLE, OPEN, ACTIVE, INACTIVE
    }

    public int id;

    public int originNode;
    public int endNode;

    private CONNECTIONSTATE state = CONNECTIONSTATE.IDLE;


    public NodeConnection(int origin, int end, int id) {
        this.id = id;
        this.originNode = origin;
        this.endNode = end;
    }

    public NodeConnection(int origin, int end, int id, CONNECTIONSTATE state) {
        this(origin, end, id);
        this.state = state;
    }


    public void setState(CONNECTIONSTATE state) {
        this.state = state;
    }

    public CONNECTIONSTATE getState() {
        return this.state;
    }
}
