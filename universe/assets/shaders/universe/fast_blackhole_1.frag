uniform sampler2D u_ray_map;
vec4 get_ray(vec2 uv, vec3 dir, vec3 h) {
    vec4 coord = texture(u_ray_map, uv);
    if (coord.x <= 0.0001 && coord.y <= 0.0001) {
        return vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        vec3 u = -normalize(h);
        float sinv = (coord.y * 2.0 - 1.0);
        float cosv = (coord.x * 2.0 - 1.0);
        dir = dir * cosv + cross(u, dir) * sinv + u * dot(u, dir) * (1.0 - cosv);
        return texture(u_cubemap, dir);
    }
}
