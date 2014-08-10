package aq.oceanbase.skyscroll.graphics;

public class TextureRegion {
    public float u1, v1;            //Top Left UV coordinate
    public float u2, v2;            //Bottom Right UV coordinate

    public TextureRegion() {
        this.u1 = 0.0f;
        this.v1 = 0.0f;

        this.u2 = 1.0f;
        this.v2 = 1.0f;
    }

    public TextureRegion(int texWidth, int texHeight, float posX, float posY, float width, float height) {
        this.u1 = posX/texWidth;                    //left
        this.v1 = posY/texHeight;                   //top

        this.u2 = this.u1 + width/texWidth;         //right
        this.v2 = this.v1 + height/texHeight;       //bottom
    }
}
