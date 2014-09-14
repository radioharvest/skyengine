package aq.oceanbase.skyscroll.logic.generators;

import aq.oceanbase.skyscroll.logic.tree.NodeConnection;
import aq.oceanbase.skyscroll.logic.tree.nodes.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreeGenerator {
    private float height;
    private float radius;
    private int nodesAmount;

    private int levels;
    private float levelSize;
    private int[] levelDensity;

    private float pointRadius;
    private float varietyFactor;

    private Random generator;
    private long seed;

    private List<Node> nodeList;
    private List<NodeConnection> connectionList;


    private float[] nodesPositionData;
    private float[] linesPositionData;

    private float[] nodesColorData;
    private float[] linesColorData;


    //<editor-fold desc="Constructors">

    public TreeGenerator() {};

    public TreeGenerator(float height, float radius, int nodesAmount) {
        this.height = height;
        this.radius = radius;
        this.nodesAmount = nodesAmount;

        this.levels = 10;
        this.levelSize = this.height/this.levels;

        this.pointRadius = this.levelSize/10;
        this.varietyFactor = (this.levelSize/2) - 2*this.pointRadius;
    }

    public TreeGenerator(float height, float radius, int nodesAmount, int levels) {
        this.height = height;
        this.radius = radius;
        this.nodesAmount = nodesAmount;

        this.levels = levels;
        this.levelSize = this.height/this.levels;

        this.pointRadius = this.levelSize/10;
        this.varietyFactor = (this.levelSize/2) - 2*this.pointRadius;
    }

    public TreeGenerator(float height, float radius, int nodesAmount, int levels, float pointRadius) {
        this.height = height;
        this.radius = radius;
        this.nodesAmount = nodesAmount;

        this.levels = levels;
        this.levelSize = this.height/this.levels;

        this.pointRadius = pointRadius;
        this.varietyFactor = (this.levelSize/2) - 2*this.pointRadius;

        if (this.varietyFactor <= 0) this.varietyFactor = 0.0f;
    }

    public TreeGenerator(float height, float radius, int nodesAmount, int levels, long seed) {
        this.height = height;
        this.radius = radius;
        this.nodesAmount = nodesAmount;

        this.levels = levels;
        this.levelSize = this.height/this.levels;

        this.pointRadius = this.levelSize/10;
        this.varietyFactor = (this.levelSize/2) - 2*this.pointRadius;

        this.seed = seed;
    }

    public TreeGenerator(float height, float radius, int nodesAmount, int levels, float pointRadius, long seed) {
        this.height = height;
        this.radius = radius;
        this.nodesAmount = nodesAmount;

        this.levels = levels;
        this.levelSize = this.height/this.levels;

        this.pointRadius = pointRadius;
        this.varietyFactor = (this.levelSize/2) - 2*this.pointRadius;

        if (this.varietyFactor <= 0) this.varietyFactor = 0.0f;

        this.seed = seed;
    }

    //</editor-fold>


    //<editor-fold desc="Getters">

    public float[] getNodesPositionData() {
        //return this.nodePositionData;
        float[] data = {
                //Remember counter-clockwise orientation of vertices in OpenGL

                // Front face
                -5.0f, 0.0f, 0.0f,
                0.0f, 1.0f, -5.0f,
                5.0f, 2.0f, 0.0f,
                0.0f, 3.0f, 5.0f,

                // Right face
                4.33f, 5.0f, 2.5f,
                -2.5f, 6.0f, 4.33f,
                -4.33f, 7.0f, -2.5f,
                2.5f, 8.0f, -4.33f,

                // Back face
                2.5f, 10.0f, 4.33f,
                -4.33f, 11.0f, 2.5f,
                -2.5f, 12.0f, -4.33f,
                4.33f, 13.0f, -2.5f,

                // Left face
                0.0f, 15.0f, 5.0f,
                -5.0f, 16.0f, 0.0f,
                0.0f, 17.0f, -5.0f,
                5.0f, 18.0f, 0.0f,

                // Front face
                -5.0f, 20.0f, 0.0f,
                0.0f, 21.0f, -5.0f,
                5.0f, 22.0f, 0.0f,
                0.0f, 23.0f, 5.0f,

                // Right face
                4.33f, 25.0f, 2.5f,
                -2.5f, 26.0f, 4.33f,
                -4.33f, 27.0f, -2.5f,
                2.5f, 28.0f, -4.33f,

                // Back face
                2.5f, 30.0f, 4.33f,
                -4.33f, 31.0f, 2.5f,
                -2.5f, 32.0f, -4.33f,
                4.33f, 33.0f, -2.5f,

                // Left face
                0.0f, 35.0f, 5.0f,
                -5.0f, 36.0f, 0.0f,
                0.0f, 37.0f, -5.0f,
                5.0f, 38.0f, 0.0f,
        };
        return data;
    }

    public float[] getLinesPositionData() {
        //return this.connectionsPositionData;
        float[] data = {
                -5.0f, 0.0f, 0.0f,
                4.33f, 5.0f, 2.5f,

                0.0f, 1.0f, -5.0f,
                -2.5f, 6.0f, 4.33f,

                5.0f, 2.0f, 0.0f,
                -4.33f, 7.0f, -2.5f,

                0.0f, 3.0f, 5.0f,
                2.5f, 8.0f, -4.33f,

                // Right face
                4.33f, 5.0f, 2.5f,
                2.5f, 10.0f, 4.33f,

                -2.5f, 6.0f, 4.33f,
                -4.33f, 11.0f, 2.5f,

                -4.33f, 7.0f, -2.5f,
                -2.5f, 12.0f, -4.33f,

                2.5f, 8.0f, -4.33f,
                4.33f, 13.0f, -2.5f,

                // Back face
                2.5f, 10.0f, 4.33f,
                0.0f, 15.0f, 5.0f,

                -4.33f, 11.0f, 2.5f,
                -5.0f, 16.0f, 0.0f,

                -2.5f, 12.0f, -4.33f,
                0.0f, 17.0f, -5.0f,

                4.33f, 13.0f, -2.5f,
                5.0f, 18.0f, 0.0f,

                // Left face
                0.0f, 15.0f, 5.0f,
                -5.0f, 20.0f, 0.0f,

                -5.0f, 16.0f, 0.0f,
                0.0f, 21.0f, -5.0f,

                0.0f, 17.0f, -5.0f,
                5.0f, 22.0f, 0.0f,

                5.0f, 18.0f, 0.0f,
                0.0f, 23.0f, 5.0f,

                // Front face
                -5.0f, 20.0f, 0.0f,
                4.33f, 25.0f, 2.5f,

                0.0f, 21.0f, -5.0f,
                -2.5f, 26.0f, 4.33f,

                5.0f, 22.0f, 0.0f,
                -4.33f, 27.0f, -2.5f,

                0.0f, 23.0f, 5.0f,
                2.5f, 28.0f, -4.33f,

                // Right face
                4.33f, 25.0f, 2.5f,
                2.5f, 30.0f, 4.33f,

                -2.5f, 26.0f, 4.33f,
                -4.33f, 31.0f, 2.5f,

                -4.33f, 27.0f, -2.5f,
                -2.5f, 32.0f, -4.33f,

                2.5f, 28.0f, -4.33f,
                4.33f, 33.0f, -2.5f,

                // Back face
                2.5f, 30.0f, 4.33f,
                0.0f, 35.0f, 5.0f,

                -4.33f, 31.0f, 2.5f,
                -5.0f, 36.0f, 0.0f,

                -2.5f, 32.0f, -4.33f,
                0.0f, 37.0f, -5.0f,

                4.33f, 33.0f, -2.5f,
                5.0f, 38.0f, 0.0f,
        };
        return data;
    }

    public float[] getNodesColorData() {
        return this.nodesColorData;
    }

    public float[] getLinesColorData() {
        return this.linesColorData;
    }

    /*public Node[] getNodes() {
        ArrayList<Node> tempNodes = new ArrayList<Node>();

        tempNodes.add(new Node(0, -5.0f, 0.0f, 0.0f));
        tempNodes.add(new Node(1, 0.0f, 1.0f, -5.0f));
        tempNodes.add(new Node(2, 5.0f, 2.0f, 0.0f));
        tempNodes.add(new Node(3, 0.0f, 3.0f, 5.0f));

        tempNodes.add(new Node(4, 4.33f, 5.0f, 2.5f));
        tempNodes.add(new Node(5, -2.5f, 6.0f, 4.33f));
        tempNodes.add(new Node(6, -4.33f, 7.0f, -2.5f));
        tempNodes.add(new Node(7, 2.5f, 8.0f, -4.33f));

        tempNodes.add(new Node(8, 2.5f, 10.0f, 4.33f));
        tempNodes.add(new Node(9, -4.33f, 11.0f, 2.5f));
        tempNodes.add(new Node(10, -2.5f, 12.0f, -4.33f));
        tempNodes.add(new Node(11, 4.33f, 13.0f, -2.5f));

        tempNodes.add(new Node(12, 0.0f, 15.0f, 5.0f));
        tempNodes.add(new Node(13, -5.0f, 16.0f, 0.0f));
        tempNodes.add(new Node(14, 0.0f, 17.0f, -5.0f));
        tempNodes.add(new Node(15, 5.0f, 18.0f, 0.0f));

        tempNodes.add(new Node(16, -5.0f, 20.0f, 0.0f));
        tempNodes.add(new Node(17, 0.0f, 21.0f, -5.0f));
        tempNodes.add(new Node(18, 5.0f, 22.0f, 0.0f));
        tempNodes.add(new Node(19, 0.0f, 23.0f, 5.0f));

        tempNodes.add(new Node(20, 4.33f, 25.0f, 2.5f));
        tempNodes.add(new Node(21, -2.5f, 26.0f, 4.33f));
        tempNodes.add(new Node(22, -4.33f, 27.0f, -2.5f));
        tempNodes.add(new Node(23, 2.5f, 28.0f, -4.33f));

        tempNodes.add(new Node(24, 2.5f, 30.0f, 4.33f));
        tempNodes.add(new Node(25, -4.33f, 31.0f, 2.5f));
        tempNodes.add(new Node(26, -2.5f, 32.0f, -4.33f));
        tempNodes.add(new Node(27, 4.33f, 33.0f, -2.5f));

        tempNodes.add(new Node(28, 0.0f, 35.0f, 5.0f));
        tempNodes.add(new Node(29, -5.0f, 36.0f, 0.0f));
        tempNodes.add(new Node(30, 0.0f, 37.0f, -5.0f));
        tempNodes.add(new Node(31, 5.0f, 38.0f, 0.0f));

        return tempNodes.toArray(new Node[] {});
        //return nodes;
    }*/

    public Node[] getNodes() {
        this.genNodes();

        return this.nodeList.toArray(new Node[nodeList.size()]);
        //return nodes;
    }

    public NodeConnection[] getConnections() {
        ArrayList<NodeConnection> tempConnections = new ArrayList<NodeConnection>();

        tempConnections.add(new NodeConnection(0, 4, 0));
        tempConnections.add(new NodeConnection(1, 5, 1));
        tempConnections.add(new NodeConnection(2, 6, 2));
        tempConnections.add(new NodeConnection(3, 7, 3, NodeConnection.CONNECTIONSTATE.ACTIVE));

        tempConnections.add(new NodeConnection(4, 8, 4));
        tempConnections.add(new NodeConnection(5, 9, 5));
        tempConnections.add(new NodeConnection(6, 10, 6));
        tempConnections.add(new NodeConnection(7, 11, 7, NodeConnection.CONNECTIONSTATE.ACTIVE));

        tempConnections.add(new NodeConnection(8, 12, 8));
        tempConnections.add(new NodeConnection(9, 13, 9));
        tempConnections.add(new NodeConnection(10, 14, 10));
        tempConnections.add(new NodeConnection(11, 15, 11, NodeConnection.CONNECTIONSTATE.ACTIVE));

        tempConnections.add(new NodeConnection(12, 16, 12));
        tempConnections.add(new NodeConnection(13, 17, 13));
        tempConnections.add(new NodeConnection(14, 18, 14));
        tempConnections.add(new NodeConnection(15, 19, 15, NodeConnection.CONNECTIONSTATE.ACTIVE));

        tempConnections.add(new NodeConnection(16, 20, 16));
        tempConnections.add(new NodeConnection(17, 21, 17));
        tempConnections.add(new NodeConnection(18, 22, 18));
        tempConnections.add(new NodeConnection(19, 23, 19, NodeConnection.CONNECTIONSTATE.ACTIVE));

        tempConnections.add(new NodeConnection(20, 24, 20));
        tempConnections.add(new NodeConnection(21, 25, 21));
        tempConnections.add(new NodeConnection(22, 26, 22));
        tempConnections.add(new NodeConnection(23, 27, 23, NodeConnection.CONNECTIONSTATE.ACTIVE));

        tempConnections.add(new NodeConnection(24, 28, 24));
        tempConnections.add(new NodeConnection(25, 29, 25));
        tempConnections.add(new NodeConnection(26, 30, 26));
        tempConnections.add(new NodeConnection(27, 31, 27, NodeConnection.CONNECTIONSTATE.ACTIVE));

        return tempConnections.toArray(new NodeConnection[] {});
    }

    //</editor-fold>


    public void buildLevelDensity() {
        //TODO: check the seed value
        if (seed != 0) generator = new Random(seed);
        else generator = new Random();
        levelDensity = new int[levels];
        int minimum = 6;
        int fluctuationHeap = nodesAmount - levels*minimum;
        levelDensity[0] = minimum;
        for (int i = 1; i < levels-2; i++) {
            int addition = generator.nextInt((int)fluctuationHeap/(levels-1) + 1);
            levelDensity[i] = minimum + addition;
            fluctuationHeap = fluctuationHeap - addition;
        }
        levelDensity[levels-1] = minimum + fluctuationHeap;
    }

    public void buildLevel(int amount) {

    }

    public float[] buildTree() {
        float[] nodeArray = {};
        return nodeArray;
    }

    public void genNodes() {
        this.nodeList = new ArrayList<Node>();

        nodeList.add(new Node(0, -5.0f, 0.0f, 0.0f).setOutboundConnections(new int[] {4}));
        nodeList.add(new Node(1, 0.0f, 1.0f, -5.0f).setOutboundConnections(new int[] {5}));
        nodeList.add(new Node(2, 5.0f, 2.0f, 0.0f).setOutboundConnections(new int[] {6}));
        nodeList.add(new Node(3, 0.0f, 3.0f, 5.0f).setOutboundConnections(new int[] {7}));

        nodeList.add(new Node(4, 4.33f, 5.0f, 2.5f).setOutboundConnections(new int[] {8}));
        nodeList.add(new Node(5, -2.5f, 6.0f, 4.33f).setOutboundConnections(new int[] {9}));
        nodeList.add(new Node(6, -4.33f, 7.0f, -2.5f).setOutboundConnections(new int[] {10}));
        nodeList.add(new Node(7, 2.5f, 8.0f, -4.33f).setOutboundConnections(new int[] {11}));

        nodeList.add(new Node(8, 2.5f, 10.0f, 4.33f).setOutboundConnections(new int[] {12}));
        nodeList.add(new Node(9, -4.33f, 11.0f, 2.5f).setOutboundConnections(new int[] {13}));
        nodeList.add(new Node(10, -2.5f, 12.0f, -4.33f).setOutboundConnections(new int[] {14}));
        nodeList.add(new Node(11, 4.33f, 13.0f, -2.5f).setOutboundConnections(new int[] {15}));

        nodeList.add(new Node(12, 0.0f, 15.0f, 5.0f).setOutboundConnections(new int[] {16}));
        nodeList.add(new Node(13, -5.0f, 16.0f, 0.0f).setOutboundConnections(new int[] {17}));
        nodeList.add(new Node(14, 0.0f, 17.0f, -5.0f).setOutboundConnections(new int[] {18}));
        nodeList.add(new Node(15, 5.0f, 18.0f, 0.0f).setOutboundConnections(new int[] {19}));

        nodeList.add(new Node(16, -5.0f, 20.0f, 0.0f).setOutboundConnections(new int[] {20}));
        nodeList.add(new Node(17, 0.0f, 21.0f, -5.0f).setOutboundConnections(new int[] {21}));
        nodeList.add(new Node(18, 5.0f, 22.0f, 0.0f).setOutboundConnections(new int[] {22}));
        nodeList.add(new Node(19, 0.0f, 23.0f, 5.0f).setOutboundConnections(new int[] {23}));

        nodeList.add(new Node(20, 4.33f, 25.0f, 2.5f).setOutboundConnections(new int[] {24}));
        nodeList.add(new Node(21, -2.5f, 26.0f, 4.33f).setOutboundConnections(new int[] {25}));
        nodeList.add(new Node(22, -4.33f, 27.0f, -2.5f).setOutboundConnections(new int[] {26}));
        nodeList.add(new Node(23, 2.5f, 28.0f, -4.33f).setOutboundConnections(new int[] {27}));

        nodeList.add(new Node(24, 2.5f, 30.0f, 4.33f).setOutboundConnections(new int[] {28}));
        nodeList.add(new Node(25, -4.33f, 31.0f, 2.5f).setOutboundConnections(new int[] {29}));
        nodeList.add(new Node(26, -2.5f, 32.0f, -4.33f).setOutboundConnections(new int[] {30}));
        nodeList.add(new Node(27, 4.33f, 33.0f, -2.5f).setOutboundConnections(new int[] {31}));

        nodeList.add(new Node(28, 0.0f, 35.0f, 5.0f));
        nodeList.add(new Node(29, -5.0f, 36.0f, 0.0f));
        nodeList.add(new Node(30, 0.0f, 37.0f, -5.0f));
        nodeList.add(new Node(31, 5.0f, 38.0f, 0.0f));
    }

}
