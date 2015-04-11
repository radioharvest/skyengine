package aq.oceanbase.skyscroll.logic.tree.connections;

import aq.oceanbase.skyscroll.graphics.elements.Line3D;
import aq.oceanbase.skyscroll.logic.tree.nodes.Node;
import aq.oceanbase.skyscroll.logic.tree.nodes.NodeConnectionSocket;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

public class NodeConnection {
    public static float K_LINE_WITDH = 0.05f;

    public static enum CONNECTIONSTATE {
        IDLE, OPEN, ACTIVE, INACTIVE
    }

    public int id;

    private Line3D mLine;

    public int originNode;
    public int endNode;

    private CONNECTIONSTATE state = CONNECTIONSTATE.IDLE;


    public NodeConnection(int origin, int end, int id) {
        this.id = id;
        this.originNode = origin;
        this.endNode = end;
        this.mLine = new Line3D(Vector3f.getZero(), Vector3f.getZero());
    }

    public NodeConnection(int origin, int end, int id, CONNECTIONSTATE state) {
        this(origin, end, id);
        this.state = state;
    }

    public void setLine(Vector3f startPos, Vector3f endPos) {
        this.mLine = new Line3D(startPos, endPos);
        this.mLine.getRay().getStartPos().print("Debug", "New Line Pos");
        updateLineState();
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
            case CORRECT:
                switch (endState) {
                     case OPEN:
                        this.state = CONNECTIONSTATE.OPEN;
                        break;
                    case CORRECT:
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
                    case CORRECT:
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

        updateLineState();
    }

    private void updateLineState() {
        switch (state) {
            case IDLE:
                this.mLine.setWidthAndColor(K_LINE_WITDH,  new float[] {0.7f, 0.7f, 0.7f, 1.0f}).setDotted(false);
                break;
            case OPEN:
                this.mLine.setWidthAndColor(K_LINE_WITDH * 1.5f, new float[] {1.0f, 1.0f, 1.0f, 1.0f}).setDotted(true);
                break;
            case ACTIVE:
                this.mLine.setWidthAndColor(K_LINE_WITDH * 2, new float[] {1.0f, 1.0f, 1.0f, 1.0f}).setDotted(false);
                break;
            case INACTIVE:
                this.mLine.setWidthAndColor(K_LINE_WITDH * 1.5f, new float[] {0.8f, 0.0f, 0.0f, 1.0f}).setDotted(false);
                break;
            default:
                this.mLine.setWidthAndColor(K_LINE_WITDH,  new float[] {0.7f, 0.7f, 0.7f, 1.0f}).setDotted(false);
                break;
        }
    }

    public void occludeLine(Vector3f socketPos, Vector3f camPos) {

    }

    public CONNECTIONSTATE getState() {
        return this.state;
    }

    public Line3D getLine() {
        return this.mLine;
    }

    public int getId() {
        return this.id;
    }
}
