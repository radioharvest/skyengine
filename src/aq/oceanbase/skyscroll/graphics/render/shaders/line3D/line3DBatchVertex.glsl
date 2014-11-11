uniform mat4 u_VPMatrix;
uniform mat4 u_ModelMatrix;
uniform mat4 u_OrientationMatrix[16];

attribute vec4 a_Position;
attribute vec4 a_Color;
attribute vec2 a_TexCoordinate;
attribute float a_OrientationMatrixIndex;

varying vec4 v_Color;
varying vec2 v_TexCoordinate;

void main() {
    int orientationMatrixIndex = int(a_OrientationMatrixIndex);
    v_Color = a_Color;
    v_TexCoordinate = a_TexCoordinate;
    gl_Position = u_VPMatrix * u_ModelMatrix * u_OrientationMatrix[orientationMatrixIndex] * a_Position;
}