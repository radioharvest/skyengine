package aq.oceanbase.skyscroll.game.tree.connections;

import aq.oceanbase.skyscroll.engine.graphics.primitives.Line3D;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;

public class NodeConnectionOrderUnit implements Comparable {
    private int mId;
    private float mCamDistSqr;

    public NodeConnectionOrderUnit(int origid, Line3D line, Vector3f camPos) {
        this.mId = origid;
        this.mCamDistSqr = camPos.subtractV( line.getRay().getCenterPos() ).lengthSqr();
    }

    public int getId() {
        return mId;
    }

    @Override
    public int compareTo(Object obj) {
        NodeConnectionOrderUnit compObj = ((NodeConnectionOrderUnit) obj);
        /*if ( this.mStartNodeId == compObj.mStartNodeId || this.mStartNodeId == compObj.mEndNodeId ||
             this.mEndNodeId == compObj.mEndNodeId || this.mEndNodeId == compObj.mStartNodeId )
        {
            if (mCamDistSqr >= compObj.mCamDistSqr) {
                return 1;
            } else {
                return -1;
            }
        }

        Vector3f inter = this.mLine.intersectsLine(compObj.mLine);
        if (inter != null) {
            if ()
        } else {*/
            if (mCamDistSqr >= compObj.mCamDistSqr) {
                return -1;
            } else {
                return 1;
            }
        //}

        //return 1;
    }
}
