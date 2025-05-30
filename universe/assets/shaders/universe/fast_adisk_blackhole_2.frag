uniform sampler2D u_ray_map;
uniform sampler2D u_noise_tex;
uniform float u_adisk_inner_radius;
uniform float u_adisk_outer_radius;
uniform float u_adisk_speed;
uniform float u_coverage_lit;
uniform float u_adisk_thickness;

uniform float u_adisk_noise_scale;
uniform float u_time;
uniform int u_adisk_noise_LOD_1;
uniform float u_max_light;
vec3 Blackbody(float temperature) {
    // https://en.wikipedia.org/wiki/Planckian_locus
    const mat2x4 splineX = mat2x4(-0.2661293e9, -0.2343589e6, 0.8776956e3, 0.179910,
            -3.0258469e9, 2.1070479e6, 0.2226347e3, 0.240390);

    const mat3x4 splineY = mat3x4(-1.1063814, -1.34811020, 2.18555832, -0.20219683,
            -0.9549476, -1.37418593, 2.09137015, -0.16748867,
            3.0817580, -5.87338670, 3.75112997, -0.37001483);

    float rt = 1.0 / temperature;
    float rt2 = rt * rt;
    vec4 coeffX = vec4(rt2 * rt, rt2, rt, 1.0);

    float x = dot(coeffX, temperature < 4000.0 ? splineX[0] : splineX[1]);
    float x2 = x * x;
    vec4 coeffY = vec4(x2 * x, x2, x, 1.0);

    float z = 1.0 / dot(coeffY, temperature < 2222.0 ? splineY[0] : temperature < 4000.0 ? splineY[1] : splineY[2]);

    vec3 xyz = vec3(x * z, 1.0, z);
    xyz.z -= xyz.x + 1.0;

    const mat3 xyzToSrgb = mat3(3.24097, -0.96924, 0.05563,
            -1.53738, 1.87597, -0.20398,
            -0.49861, 0.04156, 1.05697);

    return max(xyzToSrgb * xyz, vec3(0.0));
}
//from https://www.shadertoy.com/view/4sfGzS
float iqnoise(in vec3 x)
{
    vec3 p = floor(x);
    vec3 f = fract(x);
    //f = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
    f = f * f * (3.0 - 2.0 * f);
    vec2 uv = (p.xy + vec2(37.0, 17.0) * p.z) + f.xy;
    vec2 rg = textureLod(u_noise_tex, (uv + 0.5) / 256.0, 0.0).yx;
    return -1.0 + 2.0 * mix(rg.x, rg.y, f.z);
}
float saturate(float x)
{
    return clamp(x, 0.0, 1.0);
}

vec3 saturate(vec3 x)
{
    return clamp(x, vec3(0.0), vec3(1.0));
}
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
float pcurve(float x) {
    float x2 = x * x;
    return 12.207 * x2 * x2 * (1.0 - x);
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
    if (res <= outer) {
        for (int j = 3; j >= -2; j--)
        {
            const float scl = 3.0;
            float tres = res + float(j) / scl;
            if (tres >= u_adisk_outer_radius)
                tres -= 2.0 * float(j) / scl;
            if (tres <= u_adisk_inner_radius)
                tres += float(j) * 2.0 / scl;
            vec3 pos = resx * tres * dir + resy * tres * e_y;
            float discInner = u_adisk_inner_radius;
            float discOuter = u_adisk_outer_radius;
            float discWidth = discOuter - discInner;
            float discRadius = discOuter - discWidth * 0.5;
            vec3 origin = vec3(0.0, 0.0, 0.0);
            vec3 discNormal = normalize(vec3(0.0, 1.0, 0.0));
            float distFromCenter = distance(pos, origin);
            float distFromDisc = dot(discNormal, pos - origin);

            float radialGradient = 1.0 - saturate((distFromCenter - discInner) / discWidth * 0.5);
            float dist = abs(distFromDisc);
            float discThickness = u_adisk_thickness;
            discThickness *= radialGradient;

            float fade = pow((abs(distFromCenter - discInner) + 0.4), 4.0) * 0.04;
            float bloomFactor = 1.0 / (pow(distFromDisc, 2.0) * 40.0 + fade + 0.00002);
            bloomFactor *= saturate(2.0 - abs(dist) / discThickness);
            bloomFactor = bloomFactor * bloomFactor;
            float dr = pcurve(radialGradient);
            float density = pcurve(radialGradient);
            density *= saturate(1.0 - abs(dist) / discThickness);
            density = saturate(density * 0.7);
            density = saturate(density + bloomFactor * 0.1);
            if (density >= 0.0001) {
                float p = atan2(-pos.x, -pos.z);
                vec3 radialCoords;
                radialCoords.x = distFromCenter;
                radialCoords.y = p * (1.0 - radialGradient * 0.5);
                radialCoords.z = distFromDisc * 0.1;
                radialCoords *= 3.5;
                vec3 offset = vec3(0.03, 0.07, 0.0) * u_time * u_adisk_speed * 10.0;
                float accum = 0.0;
                float alpha_ = 0.5;
                float octAlpha = 0.87;
                float octScale = u_adisk_noise_scale;
                float octShift = (octAlpha / octScale) / float(u_adisk_noise_LOD_1);
                for (int i = 0; i < u_adisk_noise_LOD_1; i++) {
                    accum += alpha_ * iqnoise(radialCoords);
                    radialCoords = (radialCoords + offset) * octScale;
                    alpha_ *= octAlpha;
                }
                float fbm = accum + octShift;
                fbm = fbm * fbm;
                fbm = fbm * fbm;
                density *= fbm * dr;
                float gr = 1.0 - radialGradient;
                gr = gr * gr;
                float glowStrength = 4.0 / (gr * gr * 400.0 + 0.002);
                vec3 glow = Blackbody(2700.0 + glowStrength * 50.0) * glowStrength;
                glow *= sin(p - 1.07) * 0.75 + 1.0;
                float stepTransmittance = exp2(-density * 20.0);
                float integral = 1.0 - stepTransmittance;
                alpha *= stepTransmittance;
                color += integral * alpha * glow;
            }

            vec2 t = vec2(1.0, 0.01);
            float torusDist = length(length(pos + vec3(0.0, -0.05, 0.0)) - t);
            float bloomDisc = 4.0 / (pow(torusDist, 3.5) + 0.001);
            vec3 col = Blackbody(12000.0);
            bloomDisc *= step(0.5, distFromCenter);

            color += col * bloomDisc * 1.0 * alpha;
        }
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
