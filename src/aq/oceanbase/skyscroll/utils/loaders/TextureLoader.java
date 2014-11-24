package aq.oceanbase.skyscroll.utils.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

public class TextureLoader {
    public static int loadTexture(final Context context, final int resourceId, final BitmapFactory.Options options, int filterMode) {
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        return loadTexture(bitmap, filterMode);
    }

    public static int loadTexture(final Context context, final int resourceId, final BitmapFactory.Options options) {
        return loadTexture(context, resourceId, options, GLES20.GL_NEAREST);
    }

    public static int loadTexture(final Context context, final int resourceId, int filterMode) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return loadTexture(context, resourceId, options, filterMode);
    }

    public static int loadTexture(final Context context, final int resourceId) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        return loadTexture(context, resourceId, options, GLES20.GL_NEAREST);
    }

    public static int loadTexture(Bitmap bitmap) {
        return loadTexture(bitmap, GLES20.GL_NEAREST);
    }

    public static int loadTexture(Bitmap bitmap, int filterMode) {
        final int[] textureHandler = new int[1];

        GLES20.glGenTextures(1, textureHandler, 0);
        if (filterMode != GLES20.GL_LINEAR && filterMode != GLES20.GL_NEAREST)
            filterMode = GLES20.GL_NEAREST;

        if (textureHandler[0] != 0) {

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandler[0]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, filterMode);
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
