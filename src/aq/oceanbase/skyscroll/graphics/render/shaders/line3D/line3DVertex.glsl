uniform mat4 u_VPMatrix;
uniform mat4 u_ModelMatrix;
uniform mat4 u_OrientationMatrix;

attribute vec4 a_Position;
attribute vec4 a_Color;

varying vec4 v_Color;

void main() {
    v_Color = a_Color;
    gl_Position = u_VPMatrix * u_ModelMatrix * u_OrientationMatrix * a_Position;
}