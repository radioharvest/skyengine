package aq.oceanbase.skyscroll.generators;

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
