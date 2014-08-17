package aq.oceanbase.skyscroll.graphics;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class TextureRegion {
    public float u1, v1;            //Top Left UV coordinate
    public float u2, v2;            //Bottom Right UV coordinate

    private FloatBuffer textureCoordinateBuffer;

    public TextureRegion() {
        this.u1 = 0.0f;
        this.v1 = 0.0f;

        this.u2 = 1.0f;
        this.v2 = 1.0f;
    }

    public TextureRegion(int texWidth, int texHeight, float posX, float posY, float width, float height) {
        this.u1 = posX/texWidth;                    //left
        this.v1 = posY/texHeight;                   //top

        this.u2 = this.u1 + width/texWidth;         //right
        this.v2 = this.v1 + height/texHeight;       //bottom
    }

    public void moveHorizontally(float amount, float min, float max) {
        if ( this.u1 + amount < min ) amount = min - this.u1;
        if ( this.u2 + amount > max ) amount = max - this.u2;


        this.u1 += amount;
        this.u2 += amount;

    }

    public void moveVertically(float amount, float min, float max) {
        Log.e("Draw", new StringBuilder("Amount input: ").append(amount).toString());
        Log.e("Draw", new StringBuilder("Min: ").append(min).toString());
        Log.e("Draw", new StringBuilder("Max: ").append(max).toString());
        Log.e("Draw", new StringBuilder("TexRgn: ").append(u1).append(" ").append(v1).append(" ").append(u2).append(" ").append(v2).toString());

        if ( this.v1 + amount < min ) {
            Log.e("Draw", new StringBuilder("Lower: ").append(amount).toString());
            amount = min - this.v1;
        }
        if ( this.v2 + amount > max ) {
            Log.e("Draw", new StringBuilder("Higher: ").append(amount).toString());
            amount = max - this.v2;
        }

        Log.e("Draw", new StringBuilder("Amount in func: ").append(amount).toString());

        this.v1 += amount;
        this.v2 += amount;
        Log.e("Draw", new StringBuilder().append("MOVED").toString());
    }

    public void moveHorizontally(float amount) {
        this.moveHorizontally(amount, 0.0f, 1.0f);
    }

    public void moveVertically(float amount) {
        this.moveVertically(amount, 0.0f, 1.0f);
    }
}
