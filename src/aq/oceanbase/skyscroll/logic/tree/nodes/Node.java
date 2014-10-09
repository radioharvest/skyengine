package aq.oceanbase.skyscroll.logic.tree.nodes;

import android.util.Log;
import aq.oceanbase.skyscroll.logic.tree.NodeConnection;
import aq.oceanbase.skyscroll.utils.math.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Node {

    public static enum NODESTATE {
        IDLE, OPEN, WRONG, CORRECT
    }

    public int id;          // node id

    public float posX;
    public float posY;
    public float posZ;

    private int type;           // type is int for 3 subjects which are set up in Game

    private NODESTATE state;    // state of the Node
    private boolean selected;   // selected flag

    private int difficulty;     // difficulty level is int, amount is set in Game

    private long questionId;     // id of the question in database

    private int inboundConnections[] = new int[] {};     // array of inbound connections
    private int outboundConnections[] = new int[] {};    //array of outbound connections

    private NodeConnectionSocket sockets[] = new NodeConnectionSocket[] {};


    public Node(int id, float x, float y, float z) {
        this.id = id;

        this.posX = x;
        this.posY = y;
        this.posZ = z;

        this.state = NODESTATE.IDLE;

        Log.e("Debug", new StringBuilder().append(sockets.length).toString());
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


    public void setQuestionId(long id) {
        this.questionId = id;
    }

    public long getQuestionId() {
        return this.questionId;
    }


    public void setIdle() {
        this.state = NODESTATE.IDLE;
    }

    public boolean isIdle() {
        if (state == NODESTATE.IDLE) return true;
        else return false;
    }

    public Node setState(NODESTATE state) {
        this.state = state;
        return this;
    }

    public NODESTATE getState() {
        return this.state;
    }


    public Node setOutboundConnections(int[] out) {
        this.outboundConnections = out;
        return this;
    }

    public int[] getOutboundConnections() {
        return this.outboundConnections;
    }

    public Node setInboundConnections(int[] in) {
        this.inboundConnections = in;
        return this;
    }

    public Node setInboundConnections(List<Integer> list) {
        this.inboundConnections = new int[list.size()];
        for (int i = 0; i < inboundConnections.length; i++)
            inboundConnections[i] = list.get(i);

        return this;
    }

    public int[] getInboundConnections() {
        return this.inboundConnections;
    }

    public Node setSockets(NodeConnectionSocket sockets[]) {
        this.sockets = sockets;
        return this;
    }

    public NodeConnectionSocket[] getSockets() {
        return this.sockets;
    }

    public NodeConnectionSocket getSocket(int id) {
        for (int i = 0; i <= sockets.length; i++)
        {
            if (sockets[i].connectionId == id) return sockets[i];
        }
        //if no such socket return empty
        return new NodeConnectionSocket(0, 0, 0, -1, 0);
    }

    public int getConnectionId(int endNodeId) {
        for (int i = 0; i < this.sockets.length; i++) {
            if (sockets[i].endNode == endNodeId) return sockets[i].connectionId;
        }
        return -1;
    }


    public boolean isSelected() {
        return this.selected;
    }

    public void select() {
        this.selected = true;
    }

    public void deselect() {
        this.selected = false;
    }
}
