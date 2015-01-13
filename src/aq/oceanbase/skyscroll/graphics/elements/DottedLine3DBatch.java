package aq.oceanbase.skyscroll.graphics.elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.opengl.GLES20;
import aq.oceanbase.skyscroll.graphics.TextureRegion;
import aq.oceanbase.skyscroll.graphics.elements.Line3D;
import aq.oceanbase.skyscroll.graphics.elements.Line3DBatch;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.utils.loaders.TextureLoader;

public class DottedLine3DBatch extends Line3DBatch {
    private float mUnitSize;
    private Bitmap mBitmap;

    public DottedLine3DBatch(float dashFraction, float unitSize, boolean smoothed, int batchSize) {
        super(1, smoothed, batchSize);
        if (dashFraction >= 1.0f) dashFraction = 1.0f;
        else if (dashFraction <= 0.0f) dashFraction = 0.0f;

        generateBitmap(dashFraction);
        mUnitSize = unitSize;
    }

    public DottedLine3DBatch(float dashFraction, float unitSize, boolean smoothed) {
        this(dashFraction, unitSize, smoothed, MAX_BATCHSIZE);
    }

    public DottedLine3DBatch(float dashFraction, float unitSize) {
        this(dashFraction, unitSize, false, MAX_BATCHSIZE);
    }

    private void generateBitmap(float fraction) {
        mBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);

        canvas.clipRect(0, (int)(64*fraction), 64, 0 );
        canvas.drawARGB(255, 255, 255, 255);
    }

    @Override
    public void batchElement(Line3D line) {
        TextureRegion texRgn = new TextureRegion();
        texRgn.v2 = line.getRay().getLength() / mUnitSize;
        line.setTexRgn(texRgn);
        super.batchElement(line);
    }

    @Override
    public void initialize(Context context, ProgramManager programManager) {
        this.mTextureHandle = TextureLoader.loadTexture(mBitmap);

        super.initialize(context, programManager);
    }

    @Override
    public void release() {
        GLES20.glDeleteTextures(1, new int[]{mTextureHandle}, 0);

        super.release();
    }
}
