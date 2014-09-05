package aq.oceanbase.skyscroll.graphics.primitives;

import aq.oceanbase.skyscroll.graphics.render.MainRenderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Sprite {
    public static int posDataSize = 3;
    public static int texCoordDataSize = 2;

    private final float[] vertexData;
    private final float[] spriteTextureCoordinateData;

    public final FloatBuffer spriteVertices;
    public final FloatBuffer spriteTexCoordinates;

    public Sprite () {
        vertexData =
                new float[] {
                        -1.0f,  1.0f, 0.0f,
                        -1.0f, -1.0f, 0.0f,
                        1.0f, -1.0f, 0.0f,

                        1.0f, -1.0f, 0.0f,
                        1.0f,  1.0f, 0.0f,
                        -1.0f,  1.0f, 0.0f
                };

        spriteTextureCoordinateData =
                new float[] {
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,

                        1.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 0.0f
                };

        spriteVertices = ByteBuffer.allocateDirect(vertexData.length * MainRenderer.mBytesPerFloat).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        spriteVertices.put(vertexData).position(0);

        spriteTexCoordinates = ByteBuffer.allocateDirect(spriteTextureCoordinateData.length * MainRenderer.mBytesPerFloat).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        spriteTexCoordinates.put(spriteTextureCoordinateData).position(0);
    }

    public void setVertPosition(int pos) {
        spriteVertices.position(pos);
    }

    public void setTexPosition(int pos) {
        spriteTexCoordinates.position(pos);
    }

    public FloatBuffer getSpriteVertices() {
        return spriteVertices;
    }

    public FloatBuffer getSpriteTexCoords() {
        return spriteTexCoordinates;
    }
}
