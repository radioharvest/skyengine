package aq.oceanbase.skyscroll.logic.tree;

import aq.oceanbase.skyscroll.logic.tree.nodes.Node;

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

    public void setState(Node.NODESTATE originState, Node.NODESTATE endState) {
        switch (originState) {
            case IDLE:
                this.state = CONNECTIONSTATE.IDLE;
                break;
            case WRONG:
                this.state = CONNECTIONSTATE.INACTIVE;
                break;
            case RIGHT:
                switch (endState) {
                     case OPEN:
                        this.state = CONNECTIONSTATE.OPEN;
                        break;
                    case RIGHT:
                        this.state = CONNECTIONSTATE.ACTIVE;
                        break;
                    case WRONG:
                        this.state = CONNECTIONSTATE.INACTIVE;
                        break;
                    default:                // if IDLE
                        this.state = CONNECTIONSTATE.IDLE;
                        break;
                }
                break;
            case OPEN:
                switch (endState) {
                    case RIGHT:
                        this.state = CONNECTIONSTATE.OPEN;
                        break;
                    case WRONG:
                        this.state = CONNECTIONSTATE.INACTIVE;
                        break;
                    default:                // if IDLE or OPEN
                        this.state = CONNECTIONSTATE.IDLE;
                        break;
                }
        }
    }

    public CONNECTIONSTATE getState() {
        return this.state;
    }
}
