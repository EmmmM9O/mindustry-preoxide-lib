varying vec2 v_uv;
uniform sampler2D texture0;

void main() {
    gl_FragColor = texture2D(texture0, v_uv);
}
