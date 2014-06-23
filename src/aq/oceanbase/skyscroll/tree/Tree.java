package aq.oceanbase.skyscroll.tree;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.Renderable;
import aq.oceanbase.skyscroll.graphics.primitives.Sprite;
import aq.oceanbase.skyscroll.generators.TreeGenerator;
import aq.oceanbase.skyscroll.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.loaders.TextureLoader;
import aq.oceanbase.skyscroll.math.Vector3f;
import aq.oceanbase.skyscroll.graphics.Camera;
import aq.oceanbase.skyscroll.render.MainRenderer;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.tree.nodes.Node;
import aq.oceanbase.skyscroll.tree.nodes.NodeOrderUnit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class Tree implements Renderable {
    public static int posDataSize = 3;

    public Node[] nodes;
    private int selectedNode;

    private FloatBuffer nodesPositionsBuffer;
    private FloatBuffer linesPositionsBuffer;

    private int lineShaderProgram;
    private int nodeShaderProgram;

    private int textureDataHandler;

    private float angle;
    private float[] modelMatrix = new float[16];
    private Sprite sprite;

    public Tree() {
        TreeGenerator generator = new TreeGenerator();
        final float[] nodesPositionData = generator.getNodesPositionData();
        nodesPositionsBuffer = ByteBuffer.allocateDirect(nodesPositionData.length * MainRenderer.mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        nodesPositionsBuffer.put(nodesPositionData).position(0);

        final float[] linesPositionData = generator.getLinesPositionData();
        linesPositionsBuffer = ByteBuffer.allocateDirect(linesPositionData.length * MainRenderer.mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        linesPositionsBuffer.put(linesPositionData).position(0);

        nodes = generator.getNodes();

        sprite = new Sprite();
    }

    public FloatBuffer getNodesPositionsB() {
        return nodesPositionsBuffer;
    }

    public FloatBuffer getLinesPositionsB() {
        return linesPositionsBuffer;
    }

    public float getAngle() {
        return angle;
    }

    public void updateAngle(float amount) {
        this.angle = this.angle + amount;
        if (this.angle >= 360.0f) this.angle -= 360.0f;     //could be a problem with -=
        if (this.angle <= -360.0f) this.angle += 360.0f;        //could be a problem with +=. if works - change first line
    }


    public NodeOrderUnit[] getDrawOrder(float[] conversionMatrix) {
        NodeOrderUnit[] drawOrder = new NodeOrderUnit[nodes.length];
        float[] tempPos = new float[4];

        for (int i = 0; i < nodes.length; i++) {
            Matrix.multiplyMV(tempPos, 0, conversionMatrix, 0, nodes[i].getPos4f(), 0);
            drawOrder[i] = new NodeOrderUnit(i, tempPos[2]);
        }

        Arrays.sort(drawOrder);

        return drawOrder;
    }


    public void performRaySelection(TouchRay tRay) {
        int sel = -1;
        for (int i = 0; i < nodes.length; i++) {
            Vector3f curPos = nodes[i].getPosV();
            if (tRay.pointOnRay(curPos)) {
                if (sel == -1) sel = i;
                else if (!tRay.closestSelected(nodes[sel].getPosV(), curPos)) sel = i;
            }
        }

        if (sel == -1 || sel == selectedNode) {
            deselectNode(sel);
        } else {
            selectNode(sel);
        }
    }

    public void selectNode(int id) {
        if (selectedNode != -1) nodes[selectedNode].deselect();
        nodes[id].select();
        selectedNode = id;
    }

    public void deselectNode(int id) {
        if (id != -1) nodes[id].deselect();
        else if (selectedNode != -1) nodes[selectedNode].deselect();
        selectedNode = -1;
    }


    private NodeOrderUnit[] buildDrawOrder(float[] conversionMatrix) {
        NodeOrderUnit[] drawOrder = new NodeOrderUnit[nodes.length];
        float[] tempPos = new float[4];

        for (int i = 0; i < nodes.length; i++) {
            Matrix.multiplyMV(tempPos, 0, conversionMatrix, 0, nodes[i].getPos4f(), 0);
            drawOrder[i] = new NodeOrderUnit(i, tempPos[2]);
        }

        Arrays.sort(drawOrder);

        return drawOrder;
    }

    private void drawLines(Camera cam) {
        float[] MVPMatrix = new float[16];
        GLES20.glUseProgram(lineShaderProgram);

        int MVPMatrixHandler = GLES20.glGetUniformLocation(lineShaderProgram, "u_MVPMatrix");
        int positionHandler = GLES20.glGetAttribLocation(lineShaderProgram, "a_Position");

        GLES20.glVertexAttribPointer(positionHandler, posDataSize, GLES20.GL_FLOAT, false, 0, linesPositionsBuffer);
        GLES20.glEnableVertexAttribArray(positionHandler);

        Matrix.multiplyMM(MVPMatrix, 0, cam.getViewM(), 0, modelMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, cam.getProjM(), 0, MVPMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, MVPMatrix, 0);

        GLES20.glLineWidth(2.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 56);
    }

    private void drawNodes(Camera cam) {
        int cur;
        float[] color;
        float[] spriteMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] convMatrix = new float[16];
        float[] MVPMatrix = new float[16];

        Matrix.multiplyMM(convMatrix, 0, cam.getViewM(), 0, modelMatrix, 0);
        NodeOrderUnit[] renderOrder = this.buildDrawOrder(convMatrix);


        GLES20.glUseProgram(nodeShaderProgram);

        int MVPMatrixHandler = GLES20.glGetUniformLocation(nodeShaderProgram, "u_MVPMatrix");
        int textureUniformHandler = GLES20.glGetUniformLocation(nodeShaderProgram, "u_Texture");
        int spriteMatrixHandler = GLES20.glGetUniformLocation(nodeShaderProgram, "u_SpriteMatrix");
        int spriteRotMatrixHandler = GLES20.glGetUniformLocation(nodeShaderProgram, "u_RotationMatrix");

        int positionHandler = GLES20.glGetAttribLocation(nodeShaderProgram, "a_Position");
        int texCoordHandler = GLES20.glGetAttribLocation(nodeShaderProgram, "a_TexCoordinate");
        int colorHandler = GLES20.glGetAttribLocation(nodeShaderProgram, "a_Color");


        Matrix.multiplyMM(MVPMatrix, 0, cam.getViewM(), 0, modelMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, cam.getProjM(), 0, MVPMatrix, 0);

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, -angle, 0.0f, 1.0f, 0.0f);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, MVPMatrix, 0);
        GLES20.glUniformMatrix4fv(spriteRotMatrixHandler, 1, false, rotationMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureDataHandler);
        GLES20.glUniform1i(textureUniformHandler, 0);

        sprite.setVertPosition(0);
        GLES20.glVertexAttribPointer(positionHandler, Sprite.posDataSize, GLES20.GL_FLOAT, false, 0, sprite.spriteVertices);
        GLES20.glEnableVertexAttribArray(positionHandler);

        sprite.setTexPosition(0);
        GLES20.glVertexAttribPointer(texCoordHandler, Sprite.texCoordDataSize, GLES20.GL_FLOAT, false, 0, sprite.spriteTexCoordinates);
        GLES20.glEnableVertexAttribArray(texCoordHandler);


        for (int i = 0; i < renderOrder.length; i++) {

            cur = renderOrder[i].getId();

            if (nodes[cur].isSelected()) color = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
            else color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

            Matrix.setIdentityM(spriteMatrix, 0);
            Matrix.translateM(spriteMatrix, 0, nodes[cur].posX, nodes[cur].posY, nodes[cur].posZ);

            GLES20.glUniformMatrix4fv(spriteMatrixHandler, 1, false, spriteMatrix, 0);

            GLES20.glVertexAttrib4f(colorHandler, color[0], color[1], color[2], color[3]);
            GLES20.glDisableVertexAttribArray(colorHandler);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        }

    }


    public void initialize(Context context, String shaderFolder) {
        lineShaderProgram = ShaderLoader.
                getShaderProgram(shaderFolder + "/lines/lineVertex.glsl", shaderFolder + "/lines/lineFragment.glsl");
        nodeShaderProgram = ShaderLoader.
                getShaderProgram(shaderFolder + "/sprites/spriteVertex.glsl", shaderFolder + "/sprites/spriteFragment.glsl");

        textureDataHandler = TextureLoader.loadTexture(context, R.drawable.node);
    }

    public void draw(Camera cam) {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, angle, 0.0f, 1.0f, 0.0f);

        //TODO: redo enable/disable switch when performance optimizations are done
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        drawLines(cam);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        drawNodes(cam);
    }
}
