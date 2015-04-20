package aq.oceanbase.skyscroll.game.tree.nodes;

import aq.oceanbase.skyscroll.engine.graphics.primitives.Sprite;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;

public class NodeConnectionSocket {

    public float posX;
    public float posY;
    public float posZ;

    public int connectionId;

    public int endNode;

    private float mRadius;

    private Sprite mSprite;

    public NodeConnectionSocket(float x, float y, float z, int connId, int endNode) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;

        this.connectionId = connId;
        this.endNode = endNode;

        this.mRadius = 0.1f;

        this.mSprite = new Sprite(this.getPosV(), mRadius*2, mRadius*2);
    }

    public NodeConnectionSocket(Vector3f pos, int connId, int endNode) {
        this(pos.x, pos.y, pos.z, connId, endNode);
    }

    public void setPos(Vector3f newPos) {
        this.posX = newPos.x;
        this.posY = newPos.y;
        this.posZ = newPos.z;
    }

    public void setRadius(float rad) {
        this.mRadius = rad;
    }

    public Vector3f getPosV() {
        return new Vector3f(posX, posY, posZ);
    }

    public Sprite getSprite() {
        return this.mSprite;
    }

    public float getRadius() {
        return this.mRadius;
    }
}
