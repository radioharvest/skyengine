package aq.oceanbase.skyscroll.game.tree;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import aq.oceanbase.skyscroll.R;
import aq.oceanbase.skyscroll.engine.graphics.Camera;
import aq.oceanbase.skyscroll.engine.graphics.TextureRegion;
import aq.oceanbase.skyscroll.engine.graphics.batches.DottedLine3DBatch;
import aq.oceanbase.skyscroll.engine.graphics.batches.Line3DBatch;
import aq.oceanbase.skyscroll.engine.graphics.batches.SpriteBatch;
import aq.oceanbase.skyscroll.engine.graphics.primitives.Line3D;
import aq.oceanbase.skyscroll.engine.graphics.primitives.Sprite;
import aq.oceanbase.skyscroll.engine.graphics.OrderUnit;
import aq.oceanbase.skyscroll.engine.graphics.ProgramManager;
import aq.oceanbase.skyscroll.engine.graphics.RenderableObject;
import aq.oceanbase.skyscroll.game.generators.TreeGenerator;
import aq.oceanbase.skyscroll.game.tree.connections.NodeConnection;
import aq.oceanbase.skyscroll.game.tree.connections.NodeConnectionOrderUnit;
import aq.oceanbase.skyscroll.game.tree.nodes.NodeConnectionSocket;
import aq.oceanbase.skyscroll.engine.utils.loaders.TextureLoader;
import aq.oceanbase.skyscroll.engine.utils.math.Ray3v;
import aq.oceanbase.skyscroll.engine.utils.math.Vector3f;
import aq.oceanbase.skyscroll.engine.input.touch.TouchRay;
import aq.oceanbase.skyscroll.game.tree.nodes.Node;
import aq.oceanbase.skyscroll.game.tree.nodes.NodeOrderUnit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tree extends RenderableObject {
    public static enum CONNECTIONSTATE {
        IDLE, OPEN, ACTIVE, INACTIVE
    }

    public static int posDataSize = 3;

    private Node[] nodes;
    public NodeConnection[] connections;

    private int selectedNode;

    private FloatBuffer nodesPositionsBuffer;
    private FloatBuffer linesPositionsBuffer;

    private int lineShaderProgram;

    private int textureDataHandler;

    private int mCorrectNodeTextureHandle;
    private int mWrongNodeTextureHandle;
    private int mNodeSocketTextureHandle;

    private float angle;
    private float[] modelMatrix = new float[16];
    private SpriteBatch mNodeBatch;
    private SpriteBatch mSocketBatch;
    private Line3DBatch mConnectionsBatch;
    private DottedLine3DBatch mDottedBatch;

    private Sprite mSprite;

    private Line3D mLine;

    public Tree() {
        TreeGenerator generator = new TreeGenerator();

        nodes = generator.getNodes();
        buildNodeConnections();

        final float[] nodesPositionData = getNodesPositionData();
        nodesPositionsBuffer = ByteBuffer.allocateDirect(nodesPositionData.length * (Float.SIZE / 8))
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        nodesPositionsBuffer.put(nodesPositionData).position(0);

        final float[] linesPositionData = getConnectionsPositionData();
        //for ( int i = 0; i < linesPositionData.length; i++ ) Log.e("Debug", new StringBuilder().append(linesPositionData[i]).toString());
        linesPositionsBuffer = ByteBuffer.allocateDirect(linesPositionData.length * (Float.SIZE / 8))
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
            //Log.e("Debug", new StringBuilder("Nodeid: ").append(nodes[i].id).toString());
        }

        return posData;
    }

    private float[] getConnectionsPositionData() {
        float[] posData = new float[connections.length * 6];
        Node node;
        NodeConnectionSocket socket;
        //Log.e("Error", new StringBuilder("ConnLength: ").append(posData.length).toString());

        for (int i = 0; i < connections.length; i++) {
            //Log.e("Debug", new StringBuilder("Connection ").append(i).append(" Origin: ").append(connections[i].originNode).append(" End: ").append(connections[i].endNode).toString());
            node = nodes[connections[i].originNode];
            socket = node.getSocketByConnectionId(i);
            posData[i*6 + 0] = node.posX + socket.posX;
            posData[i*6 + 1] = node.posY + socket.posY;
            posData[i*6 + 2] = node.posZ + socket.posZ;

            node = nodes[connections[i].endNode];
            socket = node.getSocketByConnectionId(i);
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
            /*case WRONG:
                nodes[id].setState(Node.NODESTATE.OPEN);
                updateNodeConnections(id, nodes[id].getInboundConnections());
                updateNodeConnections(id, nodes[id].getOutboundConnections());
                break;*/
            case IDLE:
                nodes[id].setState(Node.NODESTATE.OPEN);
                break;
            default:            // any other case (CORRECT, OPEN)
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

    public boolean setNodeCorrect(int id) {
        if (nodes[id].getState() != Node.NODESTATE.OPEN) return false;
        nodes[id].setState(Node.NODESTATE.CORRECT);
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
        if (this.angle >= 360.0f) this.angle -= 360.0f;         // could be a problem with -=
        if (this.angle <= -360.0f) this.angle += 360.0f;        // could be a problem with +=. if works - change first line
    }


    // take outcoming connections from node and build connections array
    private void buildNodeConnections() {
        List<List<Integer>> inboundList = new ArrayList<List<Integer>>();              // list of inbound connections for each node
        List<List<NodeConnectionSocket>> sockets = new ArrayList<List<NodeConnectionSocket>>();     // list of sockets for each node

        List<NodeConnection> connectionList = new ArrayList<NodeConnection>();          // list of node connections

        // filling up lists
        for (int i = 0; i < this.nodes.length; i++) {
            inboundList.add(new ArrayList<Integer>());
            sockets.add(new ArrayList<NodeConnectionSocket>());
        }

        int[] outArray;
        for (int i = 0; i < this.nodes.length; i++) {
            outArray = nodes[i].getOutboundConnections();
            for (int k = 0; k < outArray.length; k++) {
                inboundList.get(outArray[k]).add(i);

                // because of going from bottom to the top
                // it is checked whether current out id is higher, then current node id
                if (outArray[k] > i) {
                    int connId = connectionList.size();

                    // TODO: add socket position calculation here
                    NodeConnectionSocket currNodeSocket = new NodeConnectionSocket(0, 0, 0, connId, outArray[k]);
                    NodeConnectionSocket endNodeSocket = new NodeConnectionSocket(0, 0, 0, connId, i);

                    Vector3f startPos = nodes[i].getPosV().addV(currNodeSocket.getPosV());
                    Vector3f endPos = nodes[outArray[k]].getPosV().addV(endNodeSocket.getPosV());

                    NodeConnection currConnection = new NodeConnection(i, outArray[k], connId);
                    currConnection.setLine( startPos, endPos );

                    connectionList.add(currConnection);

                    sockets.get(i).add(currNodeSocket);
                    sockets.get(outArray[k]).add(endNodeSocket);
                }
            }
        }

        for (int i = 0; i < this.nodes.length; i++) {
            nodes[i].setInboundConnections(inboundList.get(i));
            nodes[i].setSockets(sockets.get(i).toArray( new NodeConnectionSocket[ sockets.get(i).size() ] ));
        }

        this.connections = connectionList.toArray( new NodeConnection[connectionList.size()] );
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




    private OrderUnit[] buildCompleteDrawOrder(Vector3f camPos) {
        OrderUnit[] drawOrder = new OrderUnit[connections.length + nodes.length];

        int index = 0;

        for (int i = 0; i < connections.length; i++, index++) {

            drawOrder[index] = new OrderUnit(   connections[i].getId(),
                                                OrderUnit.ORDERUNITTYPE.Connection,
                                                connections[i].getLine().getRay().getCenterPos(),
                                                camPos  );
        }

        for (int i = 0; i < nodes.length; i++, index++) {

            drawOrder[index] = new OrderUnit(   nodes[i].id,
                                                OrderUnit.ORDERUNITTYPE.Node,
                                                nodes[i].getPosV(),
                                                camPos  );
        }

        Arrays.sort(drawOrder);

        return drawOrder;
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

    private NodeConnectionOrderUnit[] buildConnectionDrawOrder(Vector3f camPos) {
        NodeConnectionOrderUnit[] drawOrder = new NodeConnectionOrderUnit[connections.length];
        float[] tempPos = new float[4];

        for (int i = 0; i < connections.length; i++) {
            drawOrder[i] = new NodeConnectionOrderUnit(i, connections[i].getLine(), camPos);
        }

        Arrays.sort(drawOrder);

        return drawOrder;
    }

    private void computeNodeSocketsPositions(Vector3f camPos) {
        // for each node call updateSocketPosition for each connection
        // returning vector will be the culling position. update the positions
        // but DO NOT update the line itself - it have to be updated in buildConnectionDrawOrder
        // for double calculation reduction (updating line once for both points
        // instead of updating once for each point)

        for ( int i = 0; i < nodes.length; i++ ) {
            NodeConnectionSocket[] sockets = nodes[i].getSockets();
            for ( int k = 0; k < sockets.length; k++ ) {
                Vector3f lineEndPoint;


                if ( connections[ sockets[k].connectionId ].endNode == i )              // inverse point from current node
                    lineEndPoint = connections[ sockets[k].connectionId ].getLine().getRay().getStartPos();
                else
                    lineEndPoint = connections[ sockets[k].connectionId ].getLine().getRay().getEndPos();


                Vector3f intersection = new Ray3v( camPos, lineEndPoint ).findIntersectionWithPlane( nodes[i].getPosV(), nodes[i].getSprite().getLookVector() );
                intersection = intersection.subtractV( nodes[i].getPosV() );


                if ( intersection.lengthSqr() < Math.pow(nodes[i].getRadius(), 2.0f) )
                    nodes[i].getSocket(k).setPos(Vector3f.getZero());
                else
                    nodes[i].getSocket(k).setPos(intersection.normalize().multiplySf(nodes[i].getRadius()));


                float fraction = nodes[i].getRadius()/intersection.length();
                float amount = connections[ sockets[k].connectionId ].getLine().getRay().getLength() * fraction;
                //Log.e("Debug", "Fraction: " + fraction);

                if ( connections[ sockets[k].connectionId ].originNode == i )
                    connections[ sockets[k].connectionId ].getLine().occludeStartPoint( amount );
                else
                    connections[ sockets[k].connectionId ].getLine().occludeEndPoint( amount );

            }
        }
    }


    private void drawConnections(Camera cam) {
        NodeConnectionOrderUnit[] renderOrder = buildConnectionDrawOrder(cam.getPos());

        mConnectionsBatch.beginBatch(cam, modelMatrix);
        mDottedBatch.beginBatch(cam, modelMatrix);

        for (int i = 0; i < renderOrder.length; i++) {
            Line3D currentLine = connections[renderOrder[i].getId()].getLine();

            if (currentLine.isDotted())
                mDottedBatch.batchElement(currentLine);
            else
                mConnectionsBatch.batchElement(currentLine);
        }

        mConnectionsBatch.endBatch();
        mDottedBatch.endBatch();
    }

    public void drawNodesBatched(Camera cam) {
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

        mNodeBatch.beginBatch(cam, rotationMatrix);
        //batch.beginBatch(cam);

        for (int i = 0; i < renderOrder.length; i++) {

            cur = renderOrder[i].getId();

            /*if (nodes[cur].isSelected()) color = new float[] {0.1f, 0.1f, 0.7f, 1.0f};
            else color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};*/
            switch (nodes[cur].getState()) {
                case CORRECT:
                    color = new float[] {0.0f, 0.8f, 0.0f, 1.0f};
                    break;
                case WRONG:
                    color = new float[] {0.8f, 0.0f, 0.0f, 1.0f};
                    break;
                case OPEN:
                    color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
                    break;
                default:            //when IDLE
                    color = new float[] {0.7f, 0.7f, 0.7f, 1.0f};
                    break;
            }

            Matrix.setIdentityM(spriteMatrix, 0);
            Matrix.translateM(spriteMatrix, 0, modelMatrix, 0, nodes[cur].posX, nodes[cur].posY, nodes[cur].posZ);

            float diam = nodes[cur].getRadius() * 2;

            mNodeBatch.batchElement(diam, diam, color, texRgn, spriteMatrix);

        }

        mNodeBatch.endBatch();

    }

    public void drawNodes(Camera cam) {
        int cur;
        float[] color;
        float[] spriteMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        float[] convMatrix = new float[16];

        Matrix.multiplyMM(convMatrix, 0, cam.getViewM(), 0, modelMatrix, 0);        //multiply view matrix by model to calc distances from cam
        NodeOrderUnit[] renderOrder = this.buildDrawOrder(convMatrix);

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, -angle, 0.0f, 1.0f, 0.0f);

        for (int i = 0; i < renderOrder.length; i++) {

            cur = renderOrder[i].getId();

            /*if (nodes[cur].isSelected()) color = new float[] {0.1f, 0.1f, 0.7f, 1.0f};
            else color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};*/
            switch (nodes[cur].getState()) {
                case CORRECT:
                    color = new float[] {0.0f, 0.8f, 0.0f, 1.0f};
                    nodes[cur].getSprite().setColor(color).setTexture(mCorrectNodeTextureHandle);
                    break;
                case WRONG:
                    color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
                    nodes[cur].getSprite().setColor(color).setTexture(mWrongNodeTextureHandle);
                    break;
                case OPEN:
                    color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
                    nodes[cur].getSprite().setColor(color).setTexture(mCorrectNodeTextureHandle);
                    break;
                default:            //when IDLE
                    color = new float[] {0.7f, 0.7f, 0.7f, 1.0f};
                    nodes[cur].getSprite().setColor(color).setTexture(mCorrectNodeTextureHandle);
                    break;
            }

            Matrix.setIdentityM(spriteMatrix, 0);
            Matrix.translateM(spriteMatrix, 0, modelMatrix, 0, nodes[cur].posX, nodes[cur].posY, nodes[cur].posZ);

            nodes[cur].getSprite().setModelMatrix(spriteMatrix).setOrientationMatrix(rotationMatrix).draw(cam);

            NodeConnectionSocket[] sockets = nodes[cur].getSockets();
            mSocketBatch.beginBatch(cam, rotationMatrix);

            for (int k = 0; k < sockets.length; k++) {
                Matrix.setIdentityM(spriteMatrix, 0);
                Matrix.translateM(spriteMatrix, 0, modelMatrix, 0, nodes[cur].posX + sockets[k].posX, nodes[cur].posY + sockets[k].posY, nodes[cur].posZ + sockets[k].posZ);

                //sockets[k].getPosV().print("Debug", "Node " + i + " socket " + k);

                float diam = sockets[k].getRadius() * 2;

                mSocketBatch.batchElement(diam, diam, sockets[k].getSprite().getTexRgn(), spriteMatrix);
            }

            mSocketBatch.endBatch();
        }


    }

    private void drawOneNode(int id, float[] rotationMatrix, Camera cam) {
        float[] color;
        float[] spriteMatrix = new float[16];

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, -angle, 0.0f, 1.0f, 0.0f);

        switch (nodes[id].getState()) {
            case CORRECT:
                color = new float[] {0.0f, 0.8f, 0.0f, 1.0f};
                nodes[id].getSprite().setColor(color).setTexture(mCorrectNodeTextureHandle);
                break;
            case WRONG:
                color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
                nodes[id].getSprite().setColor(color).setTexture(mWrongNodeTextureHandle);
                break;
            case OPEN:
                color = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
                nodes[id].getSprite().setColor(color).setTexture(mCorrectNodeTextureHandle);
                break;
            default:            //when IDLE
                color = new float[] {0.7f, 0.7f, 0.7f, 1.0f};
                nodes[id].getSprite().setColor(color).setTexture(mCorrectNodeTextureHandle);
                break;
        }

        Matrix.setIdentityM(spriteMatrix, 0);
        Matrix.translateM(spriteMatrix, 0, modelMatrix, 0, nodes[id].posX, nodes[id].posY, nodes[id].posZ);

        nodes[id].getSprite().setModelMatrix(spriteMatrix).setOrientationMatrix(rotationMatrix).draw(cam);


        NodeConnectionSocket[] sockets = nodes[id].getSockets();
        mSocketBatch.beginBatch(cam, rotationMatrix);


        for (int k = 0; k < sockets.length; k++) {
            Matrix.setIdentityM(spriteMatrix, 0);
            Matrix.translateM(spriteMatrix, 0, modelMatrix, 0, nodes[id].posX + sockets[k].posX, nodes[id].posY + sockets[k].posY, nodes[id].posZ + sockets[k].posZ);

            float diam = sockets[k].getRadius() * 2;

            mSocketBatch.batchElement(diam, diam, sockets[k].getSprite().getTexRgn(), spriteMatrix);
        }

        mSocketBatch.endBatch();

    }

    public void drawAll(Camera cam) {
        float[] rotationMatrix = new float[16];
        boolean typeSwitchDirty = false;

        OrderUnit[] renderOrder = buildCompleteDrawOrder( cam.getPos() );

        Matrix.setIdentityM(rotationMatrix, 0);
        Matrix.rotateM(rotationMatrix, 0, -angle, 0.0f, 1.0f, 0.0f);

        for (int i = 0; i < renderOrder.length; i++) {
            if (renderOrder[i].getType() == OrderUnit.ORDERUNITTYPE.Node) {
                if ( !typeSwitchDirty ) {
                    mConnectionsBatch.endBatch();
                    mDottedBatch.endBatch();
                }

                drawOneNode(renderOrder[i].getId(), rotationMatrix, cam);
                typeSwitchDirty = true;
            } else {
                if (typeSwitchDirty) {
                    mConnectionsBatch.beginBatch(cam, modelMatrix);
                    mDottedBatch.beginBatch(cam, modelMatrix);
                    typeSwitchDirty = false;
                }

                Line3D currentLine = connections[renderOrder[i].getId()].getLine();
                if (currentLine.isDotted())
                    mDottedBatch.batchElement(currentLine);
                else
                    mConnectionsBatch.batchElement(currentLine);
            }
        }



    }


    @Override
    public void initialize(Context context, ProgramManager programManager) {
        lineShaderProgram = programManager.getProgram(ProgramManager.PROGRAM.LINE);

        textureDataHandler = TextureLoader.loadTexture(context, R.drawable.game209, GLES20.GL_LINEAR);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        int socketTextureDataHandler = TextureLoader.loadTexture(context, R.drawable.node_socket, GLES20.GL_LINEAR);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        mCorrectNodeTextureHandle = TextureLoader.loadTexture(context, R.drawable.game209, GLES20.GL_LINEAR);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        mWrongNodeTextureHandle = TextureLoader.loadTexture(context, R.drawable.game210, GLES20.GL_LINEAR);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        mNodeBatch = new SpriteBatch(SpriteBatch.COLORED_VERTEX_3D, textureDataHandler);
        mNodeBatch.setFiltered(true);
        mNodeBatch.initialize(context, programManager);

        mSocketBatch = new SpriteBatch(SpriteBatch.VERTEX_3D, socketTextureDataHandler);
        mSocketBatch.setFiltered(true);
        mSocketBatch.useDepthTest(false);
        mSocketBatch.initialize(context, programManager);

        mConnectionsBatch = new Line3DBatch();
        mConnectionsBatch.initialize(context, programManager);

        mDottedBatch = new DottedLine3DBatch(0.7f, 0.8f);
        mDottedBatch.initialize(context, programManager);

        mSprite = new Sprite(Vector3f.getZero(), 2.0f, 2.0f);
        mSprite.setFiltered(true);
        mSprite.setTexture(textureDataHandler);
        mSprite.initialize(context, programManager);

        for (int i = 0; i < nodes.length; i++) {
            nodes[i].getSprite().setFiltered(true).initialize(context, programManager);
        }

        super.initialize(context, programManager);
    }

    @Override
    public void release() {
        //TODO: FILL

        super.release();
    }

    @Override
    public void draw(Camera cam) {
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.rotateM(modelMatrix, 0, angle, 0.0f, 1.0f, 0.0f);

        Camera updCam = new Camera(cam);
        updCam.setPos(cam.getPos().rotate(-angle, 0.0f, 1.0f, 0.0f));                   // camera position is changed WITHOUT updating matrices

        computeNodeSocketsPositions(updCam.getPos());
        //TODO: redo enable/disable switch when performance optimizations are done
        //GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        drawConnections(updCam);
        //GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        drawNodes(updCam);

        //GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
}

// TODO: fix the displaying of dotted line
// TODO: fix calculation of occluded part if line is in front of the node, not behind it
// TODO: change IF structure in ProgramManager to CASE
// TODO: probably, it's a good idea to make the TextureManager that will hold references to loaded textures. maybe map will be good way to do this
// TODO: Renderable object as detached drawing object to include as a member to logic classes
// TODO: update SpriteBatch to fit Sprite architecture