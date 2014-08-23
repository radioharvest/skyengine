uniform mat4 u_MVPMatrix;

attribute vec4 a_Color;
attribute vec4 a_Position;
attribute vec2 a_TexCoordinate;

varying vec2 v_TexCoordinate;
varying vec4 v_Color;

void main() {
    v_TexCoordinate = a_TexCoordinate;
    v_Color = a_Color;
    gl_Position = u_MVPMatrix * a_Position;
}