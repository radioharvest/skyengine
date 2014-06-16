package aq.oceanbase.skyscroll.tree;

import aq.oceanbase.skyscroll.math.Vector3f;
import aq.oceanbase.skyscroll.render.MainRenderer;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.tree.nodes.Node;
import aq.oceanbase.skyscroll.tree.nodes.NodeOrderUnit;

import java.nio.FloatBuffer;

public class Tree {
    public Node[] nodes;
    private int selectedNode;

    private FloatBuffer nodesPositionsBuffer;
    private FloatBuffer linesPositionsBuffer;

    private float angle;
    private NodeOrderUnit[] drawOrder;

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
        selectedNode = -1;
    }
}
