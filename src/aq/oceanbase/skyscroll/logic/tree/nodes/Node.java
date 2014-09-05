package aq.oceanbase.skyscroll.logic.tree.nodes;

import aq.oceanbase.skyscroll.utils.math.Vector3f;

public class Node {

    //TODO: change state and type to enum
    public static final int IDLE = 1;
    public static final int OPEN = 2;
    public static final int WRONG = 3;
    public static final int RIGHT = 4;

    public float posX;
    public float posY;
    public float posZ;

    private String type;

    private int state;
    private boolean selected;

    private int difficulty;

    private double questionId;

    private double inConnections[];

    private double outConnections[];


    public Node(float x, float y, float z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        this.state = IDLE;
    }

    public Vector3f getPosV() {
        return new Vector3f(this.posX, this.posY, this.posZ);
    }

    public float[] getPos3f() {
        return new float[] {this.posX, this.posY, this.posZ};
    }

    public float[] getPos4f() {
        return new float[] {this.posX, this.posY, this.posZ, 1.0f};     //w is 1 for point, 0 for direction
    }

    public int getState() {
        return this.state;
    }

    public boolean isIdle() {
        if (state == IDLE) return true;
        else return false;
    }

    public boolean isSelected() {
        if (this.selected) return true;
        else return false;
    }

    public void select() {
        this.selected = true;
    }

    public void deselect() {
        this.selected = false;
    }

    public void setIdle() {
        this.state = IDLE;
    }
}
