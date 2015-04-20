package aq.oceanbase.skyscroll.engine.utils.math;

// this class is used to store 3d angle
public class AngleVector3 {
    public float pitch;     //yz
    public float yaw;       //xz
    public float roll;      //xy

    public AngleVector3(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public float[] getVector() {
        return new float[] {pitch, yaw, roll};
    }

}
