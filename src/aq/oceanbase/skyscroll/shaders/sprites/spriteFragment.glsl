precision mediump float;

uniform sampler2D u_Texture;
varying vec4 v_Color;
varying vec2 v_TexCoordinate;

void main() {
    gl_FragColor = v_Color*(texture2D(u_Texture, v_TexCoordinate).rgba);
    //gl_FragColor = v_Color;
}