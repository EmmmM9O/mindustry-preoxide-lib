#define HIGHP
varying vec2 v_uv;

uniform float u_fov_scale;
uniform vec2 u_resolution;
uniform vec3 u_camera_pos;
uniform mat3 u_camera_mat;

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution.xy - vec2(0.5);
    uv.x *= u_resolution.x / u_resolution.y;

    vec3 dir = normalize(vec3(-uv.x * u_fov_scale, uv.y * u_fov_scale, 1.0));
    dir = normalize(dir * u_camera_mat);
    gl_FragColor = vec4(dir,1.0);
}
