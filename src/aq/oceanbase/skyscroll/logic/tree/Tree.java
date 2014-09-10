package aq.oceanbase.skyscroll.logic.tree;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.*;
import aq.oceanbase.skyscroll.graphics.primitives.Sprite;
import aq.oceanbase.skyscroll.logic.generators.TreeGenerator;
import aq.oceanbase.skyscroll.utils.loaders.ShaderLoader;
import aq.oceanbase.skyscroll.utils.loaders.TextureLoader;
import aq.oceanbase.skyscroll.utils.math.Vector3f;
import aq.oceanbase.skyscroll.graphics.render.MainRenderer;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.logic.tree.nodes.Node;
import aq.oceanbase.skyscroll.logic.tree.nodes.NodeOrderUnit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class Tree implements Renderable {
    public static int posDataSize = 3;

    private boolean initialized = false;

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
    private SpriteBatch batch;

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
        return this.nodesPositionsBuffer;
    }

    public FloatBuffer getLinesPositionsB() {
        return this.linesPositionsBuffer;
    }

    public float getAngle() {
        return this.angle;
    }

    public void updateAngle(float amount) {
        this.angle = this.angle + amount;
        if (this.angle >= 360.0f) this.angle -= 360.0f;     //could be a problem with -=
        if (this.angle <= -360.0f) this.angle += 360.0f;        //could be a problem with +=. if works - change first line
    }


    public boolean performRaySelection(TouchRay tRay) {
        Matrix.setRotateM(this.modelMatrix, 0, -this.angle, 0.0f, 1.0f, 0.0f);      //derotating to world coordinates

        tRay = tRay.multiplyByMatrix(this.modelMatrix);

        int sel = -1;
        for (int i = 0; i < nodes.length; i++) {
            Vector3f curPos = nodes[i].getPosV();
            if (tRay.pointOnRay(curPos)) {
                if (sel == -1) sel = i;
                else if (!tRay.closestSelected(nodes[sel].getPosV(), curPos)) sel = i;
            }
        }

        //TODO: TEMPORARILY DISABLED SELECTION!!!
        if (sel == -1 || sel == selectedNode) {
            deselectNode(sel);
            return false;
        } else {
            selectNode(sel);
            return true;
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

    public void drawNodesBatch(Camera cam) {
        int cur;
        TextureRegion texRgn = new TextureRegion();
        float[] color;
        float[] spriteMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] convMatrix = new float[16];

        Matrix.multiplyMM(convMatrix, 0, cam.getViewM(), 0, modelMatrix, 0);        //multiply view matrix by model to calc distances from cam
        NodeOrderUnit[] renderOrder = this.buildDrawOrder(convMatrix);

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, -angle, 0.0f, 1.0f, 0.0f);

        batch.beginBatch(cam, rotationMatrix);
        //batch.beginBatch(cam);

        for (int i = 0; i < renderOrder.length; i++) {

            cur = renderOrder[i].getId();

            if (nodes[cur].isSelected()) color = new float[] {0.1f, 0.1f, 0.7f, 1.0f};
            else color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};

            Matrix.setIdentityM(spriteMatrix, 0);
            Matrix.translateM(spriteMatrix, 0, modelMatrix, 0, nodes[cur].posX, nodes[cur].posY, nodes[cur].posZ);

            batch.batchElement(2.0f, 2.0f, color, texRgn, spriteMatrix);
        }

        batch.endBatch();
    }


    public boolean isInitialized() {
        return this.initialized;
    }

    public void initialize(Context context, ProgramManager programManager) {
        lineShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.LINE);

        textureDataHandler = TextureLoader.loadTexture(context, R.drawable.node);

        batch = new SpriteBatch(SpriteBatch.COLORED_VERTEX_3D, textureDataHandler);
        batch.initialize(context, programManager);

        this.initialized = true;
    }

    public void release() {
        //TODO: FILL
    }

    public void draw(Camera cam) {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, angle, 0.0f, 1.0f, 0.0f);

        //TODO: redo enable/disable switch when performance optimizations are done
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        drawLines(cam);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        drawNodesBatch(cam);
    }
}
