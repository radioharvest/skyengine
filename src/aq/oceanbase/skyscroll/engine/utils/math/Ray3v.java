package aq.oceanbase.skyscroll.engine.utils.math;

public class Ray3v {
    private Vector3f mStartPos;
    private Vector3f mEndPos;
    private Vector3f mCenter = null;
    private Vector3f mDirectionNorm = null;

    private float mLength = -1.0f;


    public Ray3v(Vector3f startPos, Vector3f endPos) {
        this.mStartPos = startPos;
        this.mEndPos = endPos;

    }

    public void setStartPos(Vector3f mStartPos) {
        this.mStartPos = mStartPos;

        resetRay();
    }

    public void setEndPos(Vector3f mEndPos) {
        this.mEndPos = mEndPos;

        resetRay();
    }

    public Vector3f getStartPos() {
        return new Vector3f(mStartPos);
    }

    public Vector3f getEndPos() {
        return new Vector3f(mEndPos);
    }

    public Vector3f getCenterPos() {
        if (mCenter == null)
            mCenter = mStartPos.addV(this.getDirectionV().multiplySf(0.5f));
         return new Vector3f(mCenter);
    }

    public Vector3f getDirectionNorm() {
        if (mDirectionNorm == null)
            mDirectionNorm = this.getDirectionV().normalize();

        return new Vector3f(mDirectionNorm);
    }

    public float getLength() {
        if (mLength == -1.0f)
            mLength = this.getDirectionV().length();

        return mLength;
    }

    public Vector3f getDirectionV() {
        return mEndPos.subtractV(mStartPos);
    }


    private void resetRay() {
        mCenter = null;
        mDirectionNorm = null;
        mLength = -1.0f;
    }

    public Vector3f findIntersectionWithPlane(Vector3f planeOrigin, Vector3f planeNorm) {
        float lnDot = this.getDirectionV().dotV(planeNorm);         // formula is: d = (p0 - l0)*n / l*n
        if ( lnDot == 0) {                                           // the ray is parallel
            return Vector3f.getZero();
        }

        float d = ( planeOrigin.subtractV( this.mStartPos ) ).dotV(planeNorm) / lnDot;

        return this.mStartPos.addV( this.getDirectionV().multiplySf(d) );
    }

    /*public Vector3f intersectsLine(Line3D line) {
        //first check in 2D XY
        Vector2f fractions = checkIntersection2D(
                new Vector2f(this.mStartPos.x, this.mStartPos.y),
                new Vector2f(this.mEndPos.x, this.mEndPos.y),
                new Vector2f(line.mStartPos.x, line.mStartPos.y),
                new Vector2f(line.mEndPos.x, line.mEndPos.y)
        );

        if (fractions == null)
            return null;

        Vector3f origDelta = mEndPos.subtractV(mStartPos).multiplySf(fractions.x);
        Vector3f inpDelta = line.mEndPos.subtractV(line.mStartPos).multiplySf(fractions.y);
        float collideDistanceSqr = (float)Math.pow((this.mWidth/2 + line.mWidth/2), 2.0f);

        if (origDelta.subtractV(inpDelta).lengthSqr() <= collideDistanceSqr) {
            return new Vector3f(mStartPos).addV(origDelta);
        }

        return null;
    }

    private Vector2f checkIntersection2D(Vector2f origStart, Vector2f origEnd, Vector2f inpStart, Vector2f inpEnd) {
        Vector2f intersect = Vector2f.getZero();
        Vector2f origDelta = origEnd.subtractV(origStart);
        Vector2f inpDelta = inpEnd.subtractV(inpStart);

        // math logic taken from this stackoverflow topic: http://stackoverflow.com/questions/563198
        // two lines: origStart + t*origDelta, inpStart + u*inpDelta
        // formula 1: t = ( inpStart - origStart ) x inpDelta / ( origDelta x inpDelta )
        // formula 2: u = ( inpStart - origStart ) x origDelta / ( origDelta x inpDelta )
        float numerator = inpStart.subtractV(origStart).crossV(origDelta);
        float denominator = origDelta.crossV(inpDelta);

        if ( (numerator == 0 && denominator == 0) || (numerator != 0 && denominator == 0) )
            return null;

        float u = numerator / denominator;
        if ( 0.0f <= u && u <= 1.0f ) {
            float t = inpStart.subtractV(origStart).crossV(inpDelta) / denominator;
            if ( 0.0f <= t && t <= 1.0f)
                return new Vector2f(t, u);
        }

        return null;
    }*/

}
