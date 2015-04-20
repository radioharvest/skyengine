package aq.oceanbase.skyscroll.game.tree.nodes;

public class NodeOrderUnit implements Comparable {
    private int id;
    private float z;

    public NodeOrderUnit(int origid, float coordz) {
        this.id = origid;
        this.z = coordz;
    }

    public int getId() {
        return this.id;
    }

    public float getZ() {
        return this.z;
    }

    @Override
    public int compareTo(Object obj) {
        float opZ = ((NodeOrderUnit) obj).z;

        //if (this.z == opZ) return 0;
        if (this.z > opZ) return 1;
        else return -1;
    }
}
