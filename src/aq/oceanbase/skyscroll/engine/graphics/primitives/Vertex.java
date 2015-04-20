package aq.oceanbase.skyscroll.engine.graphics.primitives;

import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;

public class Vertex {
    private float mPosX = 0.0f;
    private float mPosY = 0.0f;
    private float mPosZ = 0.0f;

    private float mColorR = 0.0f;
    private float mColorG = 0.0f;
    private float mColorB = 0.0f;
    private float mColorAlpha = 0.0f;

    private float mTexU = 0.0f;
    private float mTexV = 0.0f;

    public Vertex(float x, float y, float z) {
        this.mPosX = x;
        this.mPosY = y;
        this.mPosZ = z;
    }

    public Vertex(float x, float y, float z, float u, float v) {
        this(x, y, z);
        this.mTexU = u;
        this.mTexV = v;
    }

    public Vertex(float x, float y, float z, float r, float g, float b, float alpha) {
        this(x, y, z);
        this.mColorR = r;
        this.mColorG = g;
        this.mColorB = b;
        this.mColorAlpha = alpha;
    }

    public Vertex(float x, float y, float z, float r, float g, float b, float alpha, float u, float v) {
        this(x, y, z, r, g, b, alpha);
        this.mTexU = u;
        this.mTexV = v;
    }

    public float getX() {
        return mPosX;
    }

    public float getY() {
        return mPosY;
    }

    public float getZ() {
        return mPosZ;
    }

    public float[] getPos3f() {
        return new float[] {mPosX, mPosY, mPosZ};
    }

    public float[] getPos4f() {
        return new float[] {mPosX, mPosY, mPosZ, 0.0f};
    }

    public Vector3f getPosV() {
        return new Vector3f(mPosX, mPosY, mPosZ);
    }


    public float getColorRed() {
        return mColorR;
    }

    public float getColorGreen() {
        return mColorG;
    }

    public float getColorBlue() {
        return mColorB;
    }

    public float getColorAlpha() {
        return mColorAlpha;
    }

    public float[] getColor3f() {
        return new float[] {mColorR, mColorG, mColorB};
    }

    public float[] getColor4f() {
        return new float[] {mColorR, mColorG, mColorB, mColorAlpha};
    }


    public float getTexU() {
        return mTexU;
    }

    public float getTexV() {
        return mTexV;
    }


    public void setPos(Vector3f pos) {
        mPosX = pos.x;
        mPosY = pos.y;
        mPosZ = pos.z;
    }

    public void setPosX(float pos) {
        mPosX = pos;
    }

    public void setPosY(float pos) {
        mPosY = pos;
    }

    public void setPosZ(float pos) {
        mPosZ = pos;
    }
}
