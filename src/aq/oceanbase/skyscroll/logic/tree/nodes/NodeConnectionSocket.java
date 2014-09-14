package aq.oceanbase.skyscroll.logic.tree.nodes;

import aq.oceanbase.skyscroll.utils.math.Vector3f;

public class NodeConnectionSocket {

    public float posX;
    public float posY;
    public float posZ;

    public int connectionId;

    public NodeConnectionSocket(float x, float y, float z, int connId) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        this.connectionId = connId;
    }

    public NodeConnectionSocket(Vector3f pos, int connId) {
        this(pos.x, pos.y, pos.z, connId);
    }
}
