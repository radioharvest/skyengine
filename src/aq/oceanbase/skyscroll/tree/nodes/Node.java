package aq.oceanbase.skyscroll.tree.nodes;

public class Node {

    public float posX;
    public float posY;
    public float posZ;

    private String type;

    private String state;

    private int difficulty;

    private double questionId;

    private double inConnections[];

    private double outConnections[];


    public Node(float x, float y, float z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }
}
