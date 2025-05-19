attribute vec3 a_position;
attribute vec2 a_texCoord0;
varying vec2 v_uv;
uniform mat4 u_proj;
uniform mat4 u_trans;
void main() {
    v_uv = a_texCoord0;
    gl_Position = u_proj * u_trans * vec4(a_position, 1.0);
}
