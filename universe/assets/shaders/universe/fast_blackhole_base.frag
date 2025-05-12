#define HIGHP
varying vec2 v_uv;

uniform float u_radius_2;
uniform float u_fov_scale;
uniform float u_start_distance;
uniform float u_start_distance_2;
uniform vec2 u_resolution;
uniform vec3 u_camera_pos;
uniform mat3 u_camera_mat;
uniform samplerCube u_cubemap;

vec4 get_ray(vec2 uv, vec3 dir, vec3 h);

vec4 get_color(vec3 pos, vec3 dir) {
    vec3 h = cross(pos, dir);
    float h2 = dot(h, h);
    float dis = sqrt(h2);
    if (dis >= u_start_distance - 0.1) {
        return texture(u_cubemap, dir);
    }
    float pos2 = dot(pos, pos);
    if (pos2 >= u_start_distance_2) pos2 = u_start_distance_2;
    float from = sqrt(pos2 - h2);
    float mf = sqrt(u_start_distance_2 - h2);
    return get_ray(vec2(dis / u_start_distance, from / mf), dir, h);
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution.xy - vec2(0.5);
    uv.x *= u_resolution.x / u_resolution.y;

    vec3 dir = normalize(vec3(-uv.x * u_fov_scale, uv.y * u_fov_scale, 1.0));
    dir = normalize(dir * u_camera_mat);
    gl_FragColor = get_color(u_camera_pos, dir);
}
