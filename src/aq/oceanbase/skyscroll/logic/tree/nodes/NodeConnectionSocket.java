package aq.oceanbase.skyscroll.logic.tree.nodes;

import aq.oceanbase.skyscroll.utils.math.Vector3f;

public class NodeConnectionSocket {

    public float posX;
    public float posY;
    public float posZ;

    public int connectionId;

    public int endNode;

    public NodeConnectionSocket(float x, float y, float z, int connId, int endNode) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        this.connectionId = connId;
        this.endNode = endNode;
    }

    public NodeConnectionSocket(Vector3f pos, int connId, int endNode) {
        this(pos.x, pos.y, pos.z, connId, endNode);
    }

    public void setPos(Vector3f newPos) {
        this.posX = newPos.x;
        this.posY = newPos.y;
        this.posZ = newPos.z;
    }

    public Vector3f getPos() {
        return new Vector3f(posX, posY, posZ);
    }
}
