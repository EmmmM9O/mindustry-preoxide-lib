attribute vec4 a_position;
uniform mat4 u_proj;
void main() {
    gl_Position = u_proj * a_position;
}
