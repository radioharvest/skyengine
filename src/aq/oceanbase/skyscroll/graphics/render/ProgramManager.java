package aq.oceanbase.skyscroll.graphics.render;

import aq.oceanbase.skyscroll.utils.loaders.ShaderLoader;

public class ProgramManager {
    public static enum PROGRAM {
        BACKGROUND, LINE, SPRITEBATCH, WINDOW, WINDOWCONTENT
    }

    private String shaderFolder;

    private int bckgrndShaderProgram = 0;
    private int lineShaderProgram = 0;
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
            case SPRITEBATCH:
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

        if (spriteBatchShaderProgram != 0)
            spriteBatchShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/sprites/spriteBatchVertex.glsl", shaderFolder + "/sprites/spriteBatchFragment.glsl");

        if (windowShaderProgram != 0)
            windowShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/window/windowVertex.glsl", shaderFolder + "/window/windowFragment.glsl");

        if (windowContentShaderProgram != 0)
            windowContentShaderProgram = ShaderLoader.getShaderProgram(shaderFolder + "/window/windowContentVertex.glsl", shaderFolder + "/window/windowContentFragment.glsl");

    }
}
