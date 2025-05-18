varying vec2 v_uv;

uniform float exposure;
uniform float intensity;

uniform sampler2D texture0;
uniform sampler2D texture1;

void main() {
    gl_FragColor =
        min(mix(texture2D(texture0, v_uv), texture2D(texture1, v_uv), intensity) * exposure,10.0);
}
