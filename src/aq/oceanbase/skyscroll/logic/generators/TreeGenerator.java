package aq.oceanbase.skyscroll.logic.generators;

import aq.oceanbase.skyscroll.logic.tree.nodes.Node;

import java.util.ArrayList;
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

    public Node[] getNodes() {
        ArrayList<Node> tempNodes = new ArrayList<Node>();

        tempNodes.add(new Node(-5.0f, 0.0f, 0.0f));
        tempNodes.add(new Node(0.0f, 1.0f, -5.0f));
        tempNodes.add(new Node(5.0f, 2.0f, 0.0f));
        tempNodes.add(new Node(0.0f, 3.0f, 5.0f));

        tempNodes.add(new Node(4.33f, 5.0f, 2.5f));
        tempNodes.add(new Node(-2.5f, 6.0f, 4.33f));
        tempNodes.add(new Node(-4.33f, 7.0f, -2.5f));
        tempNodes.add(new Node(2.5f, 8.0f, -4.33f));

        tempNodes.add(new Node(2.5f, 10.0f, 4.33f));
        tempNodes.add(new Node(-4.33f, 11.0f, 2.5f));
        tempNodes.add(new Node(-2.5f, 12.0f, -4.33f));
        tempNodes.add(new Node(4.33f, 13.0f, -2.5f));

        tempNodes.add(new Node(0.0f, 15.0f, 5.0f));
        tempNodes.add(new Node(-5.0f, 16.0f, 0.0f));
        tempNodes.add(new Node(0.0f, 17.0f, -5.0f));
        tempNodes.add(new Node(5.0f, 18.0f, 0.0f));

        tempNodes.add(new Node(-5.0f, 20.0f, 0.0f));
        tempNodes.add(new Node(0.0f, 21.0f, -5.0f));
        tempNodes.add(new Node(5.0f, 22.0f, 0.0f));
        tempNodes.add(new Node(0.0f, 23.0f, 5.0f));

        tempNodes.add(new Node(4.33f, 25.0f, 2.5f));
        tempNodes.add(new Node(-2.5f, 26.0f, 4.33f));
        tempNodes.add(new Node(-4.33f, 27.0f, -2.5f));
        tempNodes.add(new Node(2.5f, 28.0f, -4.33f));

        tempNodes.add(new Node(2.5f, 30.0f, 4.33f));
        tempNodes.add(new Node(-4.33f, 31.0f, 2.5f));
        tempNodes.add(new Node(-2.5f, 32.0f, -4.33f));
        tempNodes.add(new Node(4.33f, 33.0f, -2.5f));

        tempNodes.add(new Node(0.0f, 35.0f, 5.0f));
        tempNodes.add(new Node(-5.0f, 36.0f, 0.0f));
        tempNodes.add(new Node(0.0f, 37.0f, -5.0f));
        tempNodes.add(new Node(5.0f, 38.0f, 0.0f));

        return tempNodes.toArray(new Node[] {});
        //return nodes;
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

}
