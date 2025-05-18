attribute vec2 a_position;
attribute vec2 a_texCoord0;
varying vec2 v_uv;
void main() {
    v_uv = (a_position.xy + 1.0) * 0.5;
    gl_Position = vec4(a_position, 0.0, 1.0);
}
