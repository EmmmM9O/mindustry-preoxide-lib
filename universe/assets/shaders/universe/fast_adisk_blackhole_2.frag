uniform sampler2D u_ray_map;
uniform sampler2D u_color_map;
uniform float u_adisk_inner_radius;
uniform float u_adisk_outer_radius;
uniform float u_adisk_speed;
uniform float u_color_scl;
uniform float u_max_light;
uniform float u_time;

const float pi = 3.14159265;

float atan2(float y, float x)
{
    if (x > 0.0)
    {
        return atan(y / x);
    }
    else if (x == 0.0)
    {
        if (y > 0.0)
        {
            return pi / 2.0;
        }
        else if (y < 0.0)
        {
            return -(pi / 2.0);
        }
        else
        {
            return 0.0;
        }
    }
    else //(x < 0.0)
    {
        if (y >= 0.0)
        {
            return atan(y / x) + pi;
        }
        else
        {
            return atan(y / x) - pi;
        }
    }
}

float decode(uint b1, uint b2) {
    uint c = (b1 << 8) | b2;
    return float(c) / 32767.5 - 1.0;
}
const float PI = 3.141592653;

const float size = 8.0;
float getLen(int p, vec2 tile) {
    int cha = p % 4;
    int idx = int(p / 4);
    float ddx = float(idx % (int(size))) * 1.0 / size;
    float ddy = float(idx / (int(size))) * 1.0 / size;
    vec4 adisk = texture(u_ray_map, tile + vec2(ddx, ddy));
    float res = 0.0;
    if (cha == 0) res = adisk.x;
    if (cha == 1) res = adisk.y;
    if (cha == 2) res = adisk.z;
    if (cha == 3) res = adisk.w;
    res *= u_start_distance;
    return res;
}
vec4 get_ray(vec2 uv, vec3 pos, vec3 dir, vec3 h) {
    float outer = u_adisk_outer_radius;
    float inner = u_adisk_inner_radius;
    uv = clamp(uv, vec2(0.0), vec2(1.0 - 1.0 / 2042.0));
    vec2 tile = uv / size;

    vec4 coord = texture(u_ray_map, tile);
    const vec3 adisk_up = vec3(0.0, 1.0, 0.0);
    vec3 color = vec3(0.0);
    float alpha = 1.0;

    vec3 d = cross(adisk_up, h);
    vec3 e_y = normalize(cross(h, dir));
    float dx = dot(d, dir);
    float dy = dot(d, e_y);
    float t = atan(dy / dx);
    float resx = cos(t);
    float resy = sin(t);
    const float psize = (size * size - 1.0) * 4.0;
    if (t <= 0.0) t = PI + t;
    float dd = t / PI * psize;
    int p1 = int(floor(dd)) + 4;
    float res = getLen(p1, tile);
    if (res <= outer && res >= inner) {
        vec3 pos = cos(t) * res * dir + sin(t) * res * e_y;
        float p = atan2(pos.x, pos.z);
        p += u_time * u_adisk_speed * pi;
        p += u_time * u_adisk_speed * pi * res / 5.6;
        p = mod(p + pi, pi * 2.0) - pi;
        vec4 coord = texture(u_color_map, vec2((res - inner) / (outer - inner), (p + pi) / pi / 2.0));
        color += coord.rgb * alpha * u_color_scl;
        alpha = 1.0 - coord.a;
    }
    color = clamp(color, vec3(0.0), vec3(u_max_light));
    if (coord.x <= 0.0001 && coord.y <= 0.0001 && coord.z <= 0.0001 && coord.w <= 0.0001) {
        color += vec3(0.0) * alpha;
    } else {
        vec3 u = -normalize(h);

        float cosv = decode(uint(coord.x * 255.0), uint(coord.y * 255.0));
        float sinv = decode(uint(coord.z * 255.0), uint(coord.w * 255.0));
        if (abs(cosv * cosv + sinv * sinv - 1.0) >= 0.1) {
            cosv = 1.0;
            sinv = 0.0;
        }
        dir = dir * cosv + cross(u, dir) * sinv + u * dot(u, dir) * (1.0 - cosv);
        color += texture(u_cubemap, dir).xyz * alpha;
    }

    return vec4(color, 1.0);
}
