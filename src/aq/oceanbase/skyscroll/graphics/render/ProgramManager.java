package aq.oceanbase.skyscroll.graphics.render;

import aq.oceanbase.skyscroll.utils.loaders.ShaderLoader;

public class ProgramManager {
    public static enum PROGRAM {
        BACKGROUND,
        LINE,
        LINE3D,
        LINE3D_TEXTURED,
        LINE3D_BATCH,
        LINE3D_BATCH_TEXTURED,
        SPRITE,
        SPRITE_BATCH,
        WINDOW,
        WINDOWCONTENT
    }

    private String shaderFolder;

    private int bckgrndShaderProgram = 0;
    private int lineShaderProgram = 0;
    private int line3DShaderProgram = 0;
    private int line3DTexturedShaderProgram = 0;
    private int line3DBatchShaderProgram = 0;
    private int line3DBatchTexturedShaderProgram = 0;
    private int spriteShaderProgram = 0;
    private int spriteBatchShaderProgram = 0;
    private int windowShaderProgram = 0;
    private int windowContentShaderProgram = 0;

    public ProgramManager(String shaderFolder) {
        this.shaderFolder = shaderFolder;
    }

    public int getProgram(PROGRAM type) {
        switch (type) {
            case BACKGROUND:
                if (bckgrndShaderProgram == 0) {
                    bckgrndShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/background/bckgrndVertex.glsl", shaderFolder + "/background/bckgrndFragment.glsl");
                }
                return bckgrndShaderProgram;
            case LINE:
                if (lineShaderProgram == 0) {
                    lineShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/lines/lineVertex.glsl", shaderFolder + "/lines/lineFragment.glsl");
                }
                return lineShaderProgram;
            case LINE3D:
                if (line3DShaderProgram == 0) {
                    line3DShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/line3D/line3DVertex.glsl", shaderFolder + "/line3D/line3DFragment.glsl");
                }
                return line3DShaderProgram;
            case LINE3D_TEXTURED:
                if (line3DTexturedShaderProgram == 0) {
                    line3DTexturedShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/line3D/line3DVertex.glsl", shaderFolder + "/line3D/line3DTexturedFragment.glsl");
                }
                return line3DTexturedShaderProgram;
            case LINE3D_BATCH:
                if (line3DBatchShaderProgram == 0) {
                    line3DBatchShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/line3D/line3DBatchVertex.glsl", shaderFolder + "/line3D/line3DBatchFragment.glsl");
                }
                return line3DBatchShaderProgram;
            case LINE3D_BATCH_TEXTURED:
                if (line3DBatchTexturedShaderProgram == 0) {
                    line3DBatchTexturedShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/line3D/line3DBatchVertex.glsl", shaderFolder + "/line3D/line3DBatchTexturedFragment.glsl");
                }
                return line3DBatchTexturedShaderProgram;
            case SPRITE:
                if (spriteShaderProgram == 0) {
                    spriteShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/sprites/spriteVertex.glsl", shaderFolder + "/sprites/spriteFragment.glsl");
                }
                return spriteShaderProgram;
            case SPRITE_BATCH:
                if (spriteBatchShaderProgram == 0) {
                    spriteBatchShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/sprites/spriteBatchVertex.glsl", shaderFolder + "/sprites/spriteBatchFragment.glsl");
                }
                return spriteBatchShaderProgram;
            case WINDOW:
                if (windowShaderProgram == 0) {
                    windowShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/window/windowVertex.glsl", shaderFolder + "/window/windowFragment.glsl");
                }
                return windowShaderProgram;
             case WINDOWCONTENT:
                 if (windowContentShaderProgram == 0) {
                     windowContentShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/window/windowContentVertex.glsl", shaderFolder + "/window/windowContentFragment.glsl");
                 }
                 return windowContentShaderProgram;
             default:
                 return 0;
        }
    }

    public void reload() {
        if (bckgrndShaderProgram != 0)
            bckgrndShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/background/bckgrndVertex.glsl", shaderFolder + "/background/bckgrndFragment.glsl");

        if (lineShaderProgram != 0)
            lineShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/lines/lineVertex.glsl", shaderFolder + "/lines/lineFragment.glsl");

        if (line3DShaderProgram != 0)
            line3DShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/line3D/line3DVertex.glsl", shaderFolder + "/line3D/line3DFragment.glsl");

        if (line3DTexturedShaderProgram != 0)
            line3DTexturedShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/line3D/line3DVertex.glsl", shaderFolder + "/line3D/line3DTexturedFragment.glsl");

        if (line3DBatchShaderProgram != 0)
            line3DBatchShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/line3D/line3DBatchVertex.glsl", shaderFolder + "/line3D/line3DBatchFragment.glsl");

        if (line3DBatchTexturedShaderProgram != 0)
            line3DBatchTexturedShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/line3D/line3DBatchVertex.glsl", shaderFolder + "/line3D/line3DBatchTexturedFragment.glsl");

        if (spriteShaderProgram != 0)
            spriteShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/sprites/spriteVertex.glsl", shaderFolder + "/sprites/spriteFragment.glsl");

        if (spriteBatchShaderProgram != 0)
            spriteBatchShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/sprites/spriteBatchVertex.glsl", shaderFolder + "/sprites/spriteBatchFragment.glsl");

        if (windowShaderProgram != 0)
            windowShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/window/windowVertex.glsl", shaderFolder + "/window/windowFragment.glsl");

        if (windowContentShaderProgram != 0)
            windowContentShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/window/windowContentVertex.glsl", shaderFolder + "/window/windowContentFragment.glsl");

    }
}
