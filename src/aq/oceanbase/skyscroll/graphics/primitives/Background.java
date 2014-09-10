package aq.oceanbase.skyscroll.graphics.primitives;

import android.content.Context;
import android.opengl.GLES20;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.graphics.ProgramManager;
import aq.oceanbase.skyscroll.graphics.Renderable;
import aq.oceanbase.skyscroll.utils.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.utils.loaders.TextureLoader;

public class Background extends Sprite implements Renderable {
    private boolean initialized = false;

    private int textureId;
    private int textureHandler;

    private int bckgndShaderProgram;

    public Background(final int resourceId) {
        super();
        this.textureId = resourceId;
    }

    public boolean isInitialized() {
        return this.initialized;
    }

    public void initialize(Context context, ProgramManager programManager) {
        bckgndShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.BACKGROUND);

        textureHandler = TextureLoader.loadTexture(context, textureId);

        this.initialized = true;
    }

    public void release() {
        //TODO: FILL
    }

    public void draw(Camera cam) {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glUseProgram(bckgndShaderProgram);

        int textureUniformHandler = GLES20.glGetUniformLocation(bckgndShaderProgram, "u_Texture");

        int positionHandler = GLES20.glGetAttribLocation(bckgndShaderProgram, "a_Position");
        int texCoordHandler = GLES20.glGetAttribLocation(bckgndShaderProgram, "a_TexCoordinate");

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandler);
        GLES20.glUniform1i(textureUniformHandler, 0);

        spriteVertices.position(0);
        GLES20.glVertexAttribPointer(positionHandler, posDataSize, GLES20.GL_FLOAT, false, 0, spriteVertices);
        GLES20.glEnableVertexAttribArray(positionHandler);

        spriteTexCoordinates.position(0);
        GLES20.glVertexAttribPointer(texCoordHandler, texCoordDataSize, GLES20.GL_FLOAT, false, 0, spriteTexCoordinates);
        GLES20.glEnableVertexAttribArray(texCoordHandler);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}
