package aq.oceanbase.skyscroll.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class TextureLoader {
    public static int loadTexture(final Context context, final int resourceId, final BitmapFactory.Options options) {
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        return loadTexture(bitmap);
    }

    public static int loadTexture(final Context context, final int resourceId) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return loadTexture(context, resourceId, options);
    }

    public static int loadTexture(Bitmap bitmap) {
        final int[] textureHandler = new int[1];

        GLES20.glGenTextures(1, textureHandler, 0);

        if (textureHandler[0] != 0) {

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandler[0]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            bitmap.recycle();
        }

        if (textureHandler[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandler[0];
    }
}
