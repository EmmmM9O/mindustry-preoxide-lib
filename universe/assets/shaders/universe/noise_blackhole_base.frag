#define HIGHP
varying vec2 v_uv;

uniform float u_scl;
uniform float u_fov_scale;
uniform float u_start_distance;
uniform float u_start_distance_2;
uniform vec2 u_resolution;
uniform vec3 u_camera_pos;
uniform mat3 u_camera_mat;
uniform samplerCube u_cubemap;
uniform samplerCube u_cubemap_ori;

uniform float u_len_scl;
uniform float u_max_distance_2;
uniform float u_radius_2;
uniform float u_min_scl;
uniform float u_max_scl;
uniform float u_scl_t;
uniform float u_scl_r;
uniform float u_lit;

uniform float u_step_size;
uniform int u_max_steps;

uniform float u_max_light;
void adisk_color(vec3 pos, inout vec3 color, inout float alpha, float scl);
vec4 get_color(vec3 pos, vec3 dir) {
    dir *= u_step_size;
    vec3 h = cross(pos, dir);
    float h2 = dot(h, h);
    float sp2 = u_step_size * u_step_size;
    if (h2 >= u_start_distance_2 * sp2) {
	return vec4(0.0);
        //return texture(u_cubemap_ori, dir / u_step_size);
    }
    float t = dot(-pos, dir / u_step_size);
    float dt = sqrt(u_start_distance_2 - h2 / sp2);
    float t2 = t + dt;
    if (t2 < 0.0) {
	return vec4(0.0);
        //return texture(u_cubemap_ori, dir / u_step_size);
    }
    float to = (length(pos) - u_start_distance);
    if (to >= 0.0)
        pos += dir * to / u_step_size;
    vec3 color = vec3(0.0);
    float alpha = 0.0;
    for (int i = 0; i < u_max_steps; i++) {
        float len = dot(pos, pos);
        if (len >= u_max_distance_2)
            break;
        if (len <= u_radius_2) return vec4(color, 1.0);
        float scl = mix(u_min_scl, u_max_scl, 1.0 - smoothstep(0.0f, u_scl_t, inversesqrt(len) / u_scl_r));
        adisk_color(pos, color, alpha, scl);
        float r5 = pow(len, 2.5);
        dir += -1.5 * h2 * pos / r5 * u_len_scl * scl;
        pos += dir * scl;
    }
    color *= u_lit;
    color = clamp(color, vec3(0.0), vec3(u_max_light));
    color += texture(u_cubemap, dir).rgb * (1.0 - alpha);
    return vec4(color, 1.0);
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution.xy - vec2(0.5);
    uv.x *= u_resolution.x / u_resolution.y;

    vec3 dir = normalize(vec3(-uv.x * u_fov_scale, uv.y * u_fov_scale, 1.0));
    dir = normalize(dir * u_camera_mat);
    gl_FragColor = get_color(u_camera_pos * u_scl, dir);
}
