uniform sampler2D u_ray_map;
float decode(uint b1, uint b2) {
    uint c = (b1 << 8) | b2;
    return float(c) / 32767.5 - 1.0;
}
vec4 get_ray(vec2 uv, vec3 dir, vec3 h) {
    vec4 coord = texture(u_ray_map, uv);
    if (coord.x <= 0.0001 && coord.y <= 0.0001 && coord.z <= 0.0001 && coord.w <= 0.0001) {
        return vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        vec3 u = -normalize(h);
        float cosv = decode(uint(coord.x * 255.0), uint(coord.y * 255.0));
        float sinv = decode(uint(coord.z * 255.0), uint(coord.w * 255.0));
        if (abs(cosv * cosv + sinv * sinv - 1.0) >= 0.01) {
            cosv = 1.0;
            sinv = 0.0;
        }
        dir = dir * cosv + cross(u, dir) * sinv + u * dot(u, dir) * (1.0 - cosv);
        return texture(u_cubemap, dir);
    }
}
