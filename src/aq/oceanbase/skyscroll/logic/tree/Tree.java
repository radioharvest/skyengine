package aq.oceanbase.skyscroll.logic.tree;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.graphics.*;
import aq.oceanbase.skyscroll.graphics.render.ProgramManager;
import aq.oceanbase.skyscroll.graphics.render.Renderable;
import aq.oceanbase.skyscroll.logic.generators.TreeGenerator;
import aq.oceanbase.skyscroll.logic.tree.nodes.NodeConnectionSocket;
import aq.oceanbase.skyscroll.utils.loaders.TextureLoader;
import aq.oceanbase.skyscroll.utils.math.Vector3f;
import aq.oceanbase.skyscroll.graphics.render.MainRenderer;
import aq.oceanbase.skyscroll.touch.TouchRay;
import aq.oceanbase.skyscroll.logic.tree.nodes.Node;
import aq.oceanbase.skyscroll.logic.tree.nodes.NodeOrderUnit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tree implements Renderable {
    public static enum CONNECTIONSTATE {
        IDLE, OPEN, ACTIVE, INACTIVE
    }

    public static int posDataSize = 3;

    private boolean initialized = false;

    private Node[] nodes;
    public NodeConnection[] connections;

    private int selectedNode;

    private FloatBuffer nodesPositionsBuffer;
    private FloatBuffer linesPositionsBuffer;

    private int lineShaderProgram;

    private int textureDataHandler;

    private float angle;
    private float[] modelMatrix = new float[16];
    private SpriteBatch batch;

    public Tree() {
        TreeGenerator generator = new TreeGenerator();

        nodes = generator.getNodes();
        buildNodeConnections();

        final float[] nodesPositionData = getNodesPositionData();
        nodesPositionsBuffer = ByteBuffer.allocateDirect(nodesPositionData.length * MainRenderer.mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        nodesPositionsBuffer.put(nodesPositionData).position(0);

        final float[] linesPositionData = getConnectionsPositionData();
        for ( int i = 0; i < linesPositionData.length; i++ ) Log.e("Debug", new StringBuilder().append(linesPositionData[i]).toString());
        linesPositionsBuffer = ByteBuffer.allocateDirect(linesPositionData.length * MainRenderer.mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        linesPositionsBuffer.put(linesPositionData).position(0);


    }

    public Tree(Node[] nodesInput) {

    }


    private float[] getNodesPositionData() {
        float[] posData = new float[nodes.length * 3];

        for (int i = 0; i < nodes.length; i++) {
            posData[i*3 + 0] = nodes[i].posX;
            posData[i*3 + 1] = nodes[i].posY;
            posData[i*3 + 2] = nodes[i].posZ;
            Log.e("Debug", new StringBuilder("Nodeid: ").append(nodes[i].id).toString());
        }

        return posData;
    }

    private float[] getConnectionsPositionData() {
        float[] posData = new float[connections.length * 6];
        Node node;
        NodeConnectionSocket socket;
        Log.e("Error", new StringBuilder("ConnLength: ").append(posData.length).toString());

        for (int i = 0; i < connections.length; i++) {
            Log.e("Debug", new StringBuilder("Connection ").append(i).append(" Origin: ").append(connections[i].originNode).append(" End: ").append(connections[i].endNode).toString());
            node = nodes[connections[i].originNode];
            socket = node.getSocket(i);
            posData[i*6 + 0] = node.posX + socket.posX;
            posData[i*6 + 1] = node.posY + socket.posY;
            posData[i*6 + 2] = node.posZ + socket.posZ;

            node = nodes[connections[i].endNode];
            socket = node.getSocket(i);
            posData[i*6 + 3] = node.posX + socket.posX;
            posData[i*6 + 4] = node.posY + socket.posY;
            posData[i*6 + 5] = node.posZ + socket.posZ;

            connections[i].setState(nodes[connections[i].originNode].getState(), nodes[connections[i].endNode].getState());
        }



        return posData;
    }


    public Node getNode(int i) {
        return this.nodes[i];
    }

    public int getNodesAmount() {
        return this.nodes.length;
    }


    public float getAngle() {
        return this.angle;
    }


    public void setNodeStateExplicitly(int id, Node.NODESTATE newState) {
        nodes[id].setState(newState);
    }

    public boolean setNodeOpen(int id) {
        switch (nodes[id].getState()) {
            case WRONG:
                nodes[id].setState(Node.NODESTATE.OPEN);
                updateNodeConnections(id, nodes[id].getInboundConnections());
                updateNodeConnections(id, nodes[id].getOutboundConnections());
                break;
            case IDLE:
                nodes[id].setState(Node.NODESTATE.OPEN);
                break;
            default:            // any other case (RIGHT, OPEN)
                return false;
        }
        return true;
    }

    public boolean setNodeWrong(int id) {
        if (nodes[id].getState() != Node.NODESTATE.OPEN) return false;
        nodes[id].setState(Node.NODESTATE.WRONG);
        updateNodeConnections(id, nodes[id].getInboundConnections());
        updateNodeConnections(id, nodes[id].getOutboundConnections());
        return true;
    }

    public boolean setNodeRight(int id) {
        if (nodes[id].getState() != Node.NODESTATE.OPEN) return false;
        nodes[id].setState(Node.NODESTATE.RIGHT);
        int[] outConns = nodes[id].getOutboundConnections();
        for (int i = 0; i < outConns.length; i++)
            setNodeOpen(outConns[i]);

        updateNodeConnections(id, nodes[id].getInboundConnections());
        updateNodeConnections(id, nodes[id].getOutboundConnections());

        return true;
    }


    public void updateNodeConnections(int id, int[] nodeConns) {
        int connId;
        for (int i = 0; i < nodeConns.length; i++) {
            connId = nodes[id].getConnectionId(nodeConns[i]);
            if (connId != -1) {
                this.connections[connId].setState(nodes[id].getState(), nodes[nodeConns[i]].getState());
            }
        }
    }

    public void updateAngle(float amount) {
        this.angle = this.angle + amount;
        if (this.angle >= 360.0f) this.angle -= 360.0f;     //could be a problem with -=
        if (this.angle <= -360.0f) this.angle += 360.0f;        //could be a problem with +=. if works - change first line
    }


    private void buildNodeConnections() {
        List<List<Integer>> inboundList = new ArrayList<List<Integer>>();
        List<List<NodeConnectionSocket>> sockets = new ArrayList<List<NodeConnectionSocket>>();

        List<NodeConnection> connectionList = new ArrayList<NodeConnection>();

        for (int i = 0; i < this.nodes.length; i++) {
            inboundList.add(new ArrayList<Integer>());
            sockets.add(new ArrayList<NodeConnectionSocket>());
        }

        int[] outArray;
        for (int i = 0; i < this.nodes.length; i++) {
            outArray = nodes[i].getOutboundConnections();
            for (int k = 0; k < outArray.length; k++) {
                inboundList.get(outArray[k]).add(i);

                if (outArray[k] > i) {
                    int connId = connectionList.size();

                    connectionList.add(new NodeConnection(i, outArray[k], connId));

                    sockets.get(i).add(new NodeConnectionSocket(0, 0, 0, connId, outArray[k]));
                    sockets.get(outArray[k]).add(new NodeConnectionSocket(0, 0, 0, connId, i));
                }
            }
        }

        for (int i = 0; i < this.nodes.length; i++) {
            nodes[i].setInboundConnections(inboundList.get(i));
            Log.e("Debug", new StringBuilder("Sockets ").append(i).append(" ").append(sockets.get(i).size()).toString());
            nodes[i].setSockets(sockets.get(i).toArray(new NodeConnectionSocket[sockets.get(i).size()]));
        }

        this.connections = connectionList.toArray(new NodeConnection[connectionList.size()]);
    }


    public int performRaySelection(TouchRay tRay) {
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

        return sel;
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

    private void drawConnections(Camera cam) {
        float[] MVPMatrix = new float[16];
        GLES20.glUseProgram(lineShaderProgram);

        int MVPMatrixHandler = GLES20.glGetUniformLocation(lineShaderProgram, "u_MVPMatrix");
        int positionHandler = GLES20.glGetAttribLocation(lineShaderProgram, "a_Position");
        int colorHandler = GLES20.glGetAttribLocation(lineShaderProgram, "a_Color");

        GLES20.glVertexAttribPointer(positionHandler, posDataSize, GLES20.GL_FLOAT, false, 0, linesPositionsBuffer);
        GLES20.glEnableVertexAttribArray(positionHandler);

        Matrix.multiplyMM(MVPMatrix, 0, cam.getViewM(), 0, modelMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, cam.getProjM(), 0, MVPMatrix, 0);

        GLES20.glUniformMatrix4fv(MVPMatrixHandler, 1, false, MVPMatrix, 0);

        for (int i = 0; i < connections.length; i++) {
            switch (connections[i].getState()) {
                case IDLE:
                    GLES20.glVertexAttrib4f(colorHandler, 0.5f, 0.5f, 0.5f, 1.0f);
                    GLES20.glDisableVertexAttribArray(colorHandler);
                    GLES20.glLineWidth(2.0f);
                    break;
                case OPEN:
                    GLES20.glVertexAttrib4f(colorHandler, 1.0f, 1.0f, 1.0f, 1.0f);
                    GLES20.glDisableVertexAttribArray(colorHandler);
                    GLES20.glLineWidth(2.0f);
                    break;
                case ACTIVE:
                    GLES20.glVertexAttrib4f(colorHandler, 1.0f, 1.0f, 1.0f, 1.0f);
                    GLES20.glDisableVertexAttribArray(colorHandler);
                    GLES20.glLineWidth(5.0f);
                    break;
                case INACTIVE:
                    GLES20.glVertexAttrib4f(colorHandler, 0.8f, 0.0f, 0.0f, 1.0f);
                    GLES20.glDisableVertexAttribArray(colorHandler);
                    GLES20.glLineWidth(2.0f);
                    break;
                default:
                    GLES20.glVertexAttrib4f(colorHandler, 0.5f, 0.5f, 0.5f, 1.0f);
                    GLES20.glDisableVertexAttribArray(colorHandler);
                    GLES20.glLineWidth(2.0f);
                    break;
            }
            /*if (connections[i].getState() == NodeConnection.CONNECTIONSTATE.IDLE) GLES20.glLineWidth(2.0f);
            else GLES20.glLineWidth(5.0f);*/
            GLES20.glDrawArrays(GLES20.GL_LINES, i*2, 2);

        }
        /*GLES20.glLineWidth(2.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 56);*/
    }

    public void drawNodes(Camera cam) {
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

            /*if (nodes[cur].isSelected()) color = new float[] {0.1f, 0.1f, 0.7f, 1.0f};
            else color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};*/
            switch (nodes[cur].getState()) {
                case RIGHT:
                    color = new float[] {0.0f, 0.8f, 0.0f, 1.0f};
                    break;
                case WRONG:
                    color = new float[] {0.8f, 0.0f, 0.0f, 1.0f};
                    break;
                case OPEN:
                    color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
                    break;
                default:            //when IDLE
                    color = new float[] {0.5f, 0.5f, 0.5f, 1.0f};
                    break;
            }

            Matrix.setIdentityM(spriteMatrix, 0);
            Matrix.translateM(spriteMatrix, 0, modelMatrix, 0, nodes[cur].posX, nodes[cur].posY, nodes[cur].posZ);

            batch.batchElement(2.2f, 2.2f, color, texRgn, spriteMatrix);
        }

        batch.endBatch();
    }


    public boolean isInitialized() {
        return this.initialized;
    }

    public void initialize(Context context, ProgramManager programManager) {
        lineShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.LINE);

        textureDataHandler = TextureLoader.loadTexture(context, R.drawable.node);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        batch = new SpriteBatch(SpriteBatch.COLORED_VERTEX_3D, textureDataHandler);
        batch.setFiltered(true);
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
        drawConnections(cam);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        drawNodes(cam);
    }
}
