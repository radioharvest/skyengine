package aq.oceanbase.skyscroll.tree;

import android.opengl.Matrix;
import aq.oceanbase.skyscroll.generators.TreeGenerator;
import aq.oceanbase.skyscroll.math.Vector3f;
import aq.oceanbase.skyscroll.render.MainRenderer;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.tree.nodes.Node;
import aq.oceanbase.skyscroll.tree.nodes.NodeOrderUnit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class Tree {
    public Node[] nodes;
    private int selectedNode;

    private FloatBuffer nodesPositionsBuffer;
    private FloatBuffer linesPositionsBuffer;

    private float angle;
    private NodeOrderUnit[] drawOrder;

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

    public NodeOrderUnit[] getDrawOrder(float[] conversionMatrix) {
        NodeOrderUnit[] renderOrder = new NodeOrderUnit[nodes.length];
        float[] tempPos = new float[4];

        for (int i = 0; i < nodes.length; i++) {
            Matrix.multiplyMV(tempPos, 0, conversionMatrix, 0, nodes[i].getPos4f(), 0);
            renderOrder[i] = new NodeOrderUnit(i, tempPos[2]);
        }

        Arrays.sort(renderOrder);

        return renderOrder;
    }

    public void updateAngle(float amount) {
        this.angle = this.angle + amount;
        if (this.angle >= 360.0f) this.angle -= 360.0f;     //could be a problem with -=
        if (this.angle <= -360.0f) this.angle += 360.0f;        //could be a problem with +=. if works - change first line
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
}
