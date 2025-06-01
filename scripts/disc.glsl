#version 300 es
precision highp float;
#define OCTAVES 5
#define OUTER 8.6
#define INNER 2.6
#define AMPLITUDE 0.5
#define H 1.0
#define POS vec3(0.0)
#define PSCALE 4.0
#define LOD1 4
#define OFFSET vec3(pnoise2,0.0,0.0)
#define PNOISE max(createNoise(puv,10.0,0.2),createNoise(puv*1.5,10.0,0.4)*0.7)
#define GEN
out vec4 fragColor;
uniform vec2 resolution;
uniform float time;
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
vec4 permute(vec4 x) {
    return mod(((x * 34.0) + 1.0) * x, 289.0);
}
vec4 taylorInvSqrt(vec4 r) {
    return 1.79284291400159 - 0.85373472095314 * r;
}

float snoise(vec3 v) {
    const vec2 C = vec2(1.0 / 6.0, 1.0 / 3.0);
    const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

    // First corner
    vec3 i = floor(v + dot(v, C.yyy));
    vec3 x0 = v - i + dot(i, C.xxx);

    // Other corners
    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);

    //  x0 = x0 - 0. + 0.0 * C
    vec3 x1 = x0 - i1 + 1.0 * C.xxx;
    vec3 x2 = x0 - i2 + 2.0 * C.xxx;
    vec3 x3 = x0 - 1. + 3.0 * C.xxx;

    // Permutations
    i = mod(i, 289.0);
    vec4 p = permute(permute(permute(i.z + vec4(0.0, i1.z, i2.z, 1.0)) + i.y +
                    vec4(0.0, i1.y, i2.y, 1.0)) +
                i.x + vec4(0.0, i1.x, i2.x, 1.0));

    // Gradients
    // ( N*N points uniformly over a square, mapped onto an octahedron.)
    float n_ = 1.0 / 7.0; // N=7
    vec3 ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z * ns.z); //  mod(p,N*N)

    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_); // mod(j,N)

    vec4 x = x_ * ns.x + ns.yyyy;
    vec4 y = y_ * ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);

    vec4 s0 = floor(b0) * 2.0 + 1.0;
    vec4 s1 = floor(b1) * 2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;

    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);

    // Normalise gradients
    vec4 norm =
        taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    // Mix final noise value
    vec4 m =
        max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m;
    return 42.0 *
        dot(m * m, vec4(dot(p0, x0), dot(p1, x1), dot(p2, x2), dot(p3, x3)));
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
float saturate(float x)
{
    return clamp(x, 0.0, 1.0);
}

vec3 saturate(vec3 x)
{
    return clamp(x, vec3(0.0), vec3(1.0));
}
vec3 aces(vec3 x) {
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}
vec2 fade(vec2 t) {
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}
float hash(vec2 p) {
    p = 50.0 * fract(p * 0.3183099 + vec2(0.71, 0.113));
    return -1.0 + 2.0 * fract(p.x * p.y * (p.x + p.y));
}

float fade(float t) {
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

float periodicPerlinNoise(vec2 p, vec2 repeat) {
    vec2 pi = mod(floor(p), repeat);
    vec2 pf = fract(p);

    float h00 = hash(mod(pi + vec2(0.0, 0.0), repeat));
    float h10 = hash(mod(pi + vec2(1.0, 0.0), repeat));
    float h01 = hash(mod(pi + vec2(0.0, 1.0), repeat));
    float h11 = hash(mod(pi + vec2(1.0, 1.0), repeat));

    vec2 g00 = vec2(cos(h00 * 6.283), sin(h00 * 6.283));
    vec2 g10 = vec2(cos(h10 * 6.283), sin(h10 * 6.283));
    vec2 g01 = vec2(cos(h01 * 6.283), sin(h01 * 6.283));
    vec2 g11 = vec2(cos(h11 * 6.283), sin(h11 * 6.283));

    float d00 = dot(g00, pf - vec2(0.0, 0.0));
    float d10 = dot(g10, pf - vec2(1.0, 0.0));
    float d01 = dot(g01, pf - vec2(0.0, 1.0));
    float d11 = dot(g11, pf - vec2(1.0, 1.0));

    vec2 u = fade(pf);

    return mix(
        mix(d00, d10, u.x),
        mix(d01, d11, u.x),
        u.y
    );
}
float discFbm(vec2 p, int octaves) {
    float value = 0.0;
    float amplitude = AMPLITUDE;
    float to = exp2(-H);
    for (int i = 0; i < octaves; i++) {
        value += amplitude * (periodicPerlinNoise(p, vec2(10000.0, PSCALE)) + 1.0) / 2.0;
        p *= 2.0;
        amplitude *= to;
    }
    return value;
}
float createNoise(in vec2 uv, in float ringScale, in float warpFactor) {
    return smoothstep(0.3, 0.7, discFbm(vec2(log(uv.x) * ringScale,
                mod(uv.y + warpFactor * uv.x + pi, 2.0 * pi) / pi / 2.0 * PSCALE), OCTAVES));
}
void main(void) {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float outer = OUTER;
    float inner = INNER;
    float ut = uv.x * (outer - inner) + inner;
    float ph = uv.y * 2.0 * pi - pi;
    float phi = (uv.y * 2.0 * pi - pi);
    ph = 0.0;
    vec2 puv = vec2(uv.x, phi);
    float pnoise = PNOISE;
    float pnoise2 = pow(pnoise, 0.5);
    vec3 pos = vec3(cos(ph), 0.0, sin(ph)) * ut + POS;
    vec3 dir = -vec3(sin(ph), 0.0, cos(ph));
    dir *= 0.2;
    vec3 h = cross(pos, dir);
    float h2 = dot(h, h);
    float alpha = 1.0;
    vec3 color = vec3(0.0);

    for (int i = 0; i < 200; i++) {
        if (alpha <= 0.0001)
            break;
        float len = dot(pos, pos);
        if (len >= 30.0 * 30.0)
            break;
        if (len <= 1.0) break;
        {
            float discInner = inner;
            float discOuter = outer;
            float discWidth = discOuter - discInner;
            float discRadius = discOuter - discWidth * 0.5;
            vec3 origin = vec3(0.0, 0.0, 0.0);
            vec3 discNormal = normalize(vec3(0.0, 1.0, 0.0));
            float distFromCenter = distance(pos, origin);
            float distFromDisc = dot(discNormal, pos - origin);

            float radialGradient = 1.0 - saturate((distFromCenter - discInner) / discWidth * 0.5);
            float dist = abs(distFromDisc);
            float discThickness = 0.1;
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
                //float p = atan2(-pos.x, -pos.z);
                float p = ph;
                vec3 radialCoords;
                radialCoords.x = distFromCenter;
                radialCoords.y = mod(p + pi, 2.0 * pi) - pi * (1.0 - radialGradient * 0.5);
                radialCoords.z = distFromDisc * 0.1;
                radialCoords *= 3.5;
                vec3 offset = vec3(1.0, 0.07, 0.0) * 10.0
                        + OFFSET;
                float accum = 0.0;
                float alpha_ = 0.5;
                float octAlpha = 0.87;
                float octScale = 0.6;
                const int u_adisk_noise_LOD_1 = LOD1;
                float octShift = (octAlpha / octScale) / float(u_adisk_noise_LOD_1);
                for (int i = 0; i < u_adisk_noise_LOD_1; i++) {
                    accum += alpha_ * snoise(radialCoords);
                    radialCoords = (radialCoords + offset) * octScale;
                    alpha_ *= octAlpha;
                }
                float fbm = accum + octShift;
                fbm = fbm * fbm;
                fbm = fbm * fbm;
                density *= fbm * dr;

                float gr = 1.0 - radialGradient;
                gr = gr * gr;
                float glowStrength = 1.0 / (gr * gr * 400.0 + 0.002);
                vec3 glow = Blackbody(2700.0 + glowStrength * 50.0) * glowStrength;
                glow *= sin(p - 1.07) * 0.75 + 1.0;
                float stepTransmittance = exp2(-density * 4.0);
                float integral = 1.0 - stepTransmittance;
                alpha *= stepTransmittance;
                color += integral * alpha * glow;
            }
            vec2 t = vec2(1.0, 0.01);
            float torusDist = length(length(pos + vec3(0.0, -0.05, 0.0)) - t);
            float bloomDisc = 1.0 / (pow(torusDist, 3.5) + 0.001);
            vec3 col = Blackbody(12000.0);
            bloomDisc *= step(0.5, distFromCenter);

            color += col * bloomDisc * 0.1 * alpha;
        }
        float r5 = pow(len, 2.5);
        dir += -1.5 * h2 * pos / r5;
        pos += dir;
    }
    color *= pnoise;
    #ifdef GEN
    color /= 3.0;
    #else
    color = aces(color);
    color = pow(color, vec3(1.0 / 2.8));
    #endif
    fragColor = vec4(color, 1.0 - alpha);
}
