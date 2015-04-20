package aq.oceanbase.skyscroll.engine.graphics;

import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;

public class OrderUnit implements Comparable {
    public static enum ORDERUNITTYPE {
        Node, Connection
    }

    private int mId;
    private float mCamDistSqr;
    private ORDERUNITTYPE mType;

    public OrderUnit(int id, ORDERUNITTYPE type, Vector3f pos, Vector3f camPos) {
        this.mId = id;
        this.mType = type;
        this.mCamDistSqr = camPos.subtractV(pos).lengthSqr();
    }

    public int getId() {
        return this.mId;
    }

    public ORDERUNITTYPE getType() {
        return this.mType;
    }

    @Override
    public int compareTo(Object obj) {
        float opCamDist = ((OrderUnit) obj).mCamDistSqr;
        if (mCamDistSqr >= opCamDist ) {
            return -1;
        } else {
            return 1;
        }
    }
}
