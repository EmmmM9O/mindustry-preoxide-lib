
varying vec2 v_uv;
uniform sampler2D u_texture0;
void main() {
    vec4 color = texture2D(u_texture0, v_uv);
    if (color.a < 0.1) discard;
    gl_FragColor = color;
}
