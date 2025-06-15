uniform sampler2D u_noise_tex;
uniform float u_adisk_inner_radius;
uniform float u_adisk_outer_radius;
uniform float u_adisk_speed;
uniform float u_adisk_lit;
uniform float u_coverage_lit;
uniform float u_adisk_thickness;

uniform float u_adisk_noise_scale;
uniform float u_time;
uniform int u_adisk_noise_LOD_1;
uniform int u_adisk_noise_LOD_2;
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
///from https://www.shadertoy.com/view/lstSRS
///----
const vec3 MainColor = vec3(1.0);
float saturate(float x)
{
    return clamp(x, 0.0, 1.0);
}

vec3 saturate(vec3 x)
{
    return clamp(x, vec3(0.0), vec3(1.0));
}
float pcurve(float x, float a, float b)
{
    float k = pow(a + b, a + b) / (pow(a, a) * pow(b, b));
    return k * pow(x, a) * pow(1.0 - x, b);
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
float sdTorus(vec3 p, vec2 t)
{
    vec2 q = vec2(length(p.xz) - t.x, p.y);
    return length(q) - t.y;
}
void adisk_color(vec3 pos, inout vec3 color, inout float alpha, float scl) {
    //GasDisc
    float discInner = u_adisk_inner_radius;
    float discOuter = u_adisk_outer_radius;
    float discWidth = discOuter - discInner;
    float discRadius = discOuter - discWidth * 0.5;
    vec3 origin = vec3(0.0, 0.0, 0.0);
    vec3 discNormal = normalize(vec3(0.0, 1.0, 0.0));
    float discThickness = u_adisk_thickness;

    float distFromCenter = distance(pos, origin);
    float distFromDisc = dot(discNormal, pos - origin);
    float radialGradient = 1.0 - saturate((distFromCenter - discInner) / discWidth * 0.5);

    float coverage = pcurve(radialGradient, 4.0, 0.9);

    discThickness *= radialGradient;
    coverage *= saturate(1.0 - abs(distFromDisc) / discThickness);

    vec3 dustColorLit = MainColor;
    vec3 dustColorDark = vec3(0.0, 0.0, 0.0);

    float dustGlow = 1.0 / (pow(1.0 - radialGradient, 2.0) * 290.0 + 0.002);
    vec3 dustColor = dustColorLit * dustGlow * 8.2;

    coverage = saturate(coverage * 0.7);

    float fade = pow((abs(distFromCenter - discInner) + 0.4), 4.0) * 0.04;
    float bloomFactor = 1.0 / (pow(distFromDisc, 2.0) * 40.0 + fade + 0.00002);
    vec3 b = dustColorLit * pow(bloomFactor, 1.5);

    b *= mix(vec3(1.7, 1.1, 1.0), vec3(0.5, 0.6, 1.0), vec3(pow(radialGradient, 2.0)));
    b *= mix(vec3(1.7, 0.5, 0.1), vec3(1.0), vec3(pow(radialGradient, 0.5)));

    dustColor = mix(dustColor, b * 150.0, saturate(1.0 - coverage * 1.0));
    coverage = saturate(coverage + bloomFactor * bloomFactor * 0.1);
    coverage *= scl;
    if (coverage < 0.01)
    {
        return;
    }
    vec3 radialCoords;
    float p = atan2(-pos.x, -pos.z);
    radialCoords.x = distFromCenter * 1.2 + 0.55;
    radialCoords.y = p * (1.0 - radialGradient * 0.5);
    radialCoords.z = distFromDisc * 1.0;

    radialCoords *= 0.95;
    vec3 rc = radialCoords + 0.0;
    vec3 offset = vec3(1.0, 0.02, 0.0) * u_time * u_adisk_speed * 7.0;
    float accum = 0.0;
    float alpha_ = 0.5;
    float octAlpha = 0.87;
    float octScale = u_adisk_noise_scale;
    float octShift = (octAlpha / octScale) / float(u_adisk_noise_LOD_1);
    for (int i = 0; i < u_adisk_noise_LOD_1; i++) {
        accum += alpha_ * iqnoise(rc);
        rc = (rc + offset) * octScale;
        alpha_ *= octAlpha;
    }
    float fbm = accum + octShift;
    fbm = fbm * fbm;
    fbm = fbm * fbm;
    rc = radialCoords + 30.0;
    accum = 0.0;
    alpha_ = 0.5;
    octAlpha = 0.87;
    octScale = u_adisk_noise_scale;
    octShift = (octAlpha / octScale) / float(u_adisk_noise_LOD_2);
    for (int i = 0; i < u_adisk_noise_LOD_2; i++) {
        accum += alpha_ * iqnoise(rc);
        rc = (rc + offset) * octScale;
        alpha_ *= octAlpha;
    }
    float fbm2 = accum + octShift;
    fbm2 = fbm2 * fbm2;
    fbm2 = fbm2 * fbm2;
    /*
                                                                                                                                float noise1 = 1.0;
                                                                                                                                float start = u_adisk_noise_scale;
                                                                                                                                for (int i = 0; i < u_adisk_noise_LOD_1; i++) {
                                                                                                                                    rc.y -= float((i % 2) * 2 - 1) * u_time * u_adisk_speed;
                                                                                                                                    noise1 *= 0.5 * iqnoise(rc * start) + 0.5;
                                                                                                                                    start *= 2.0;
                                                                                                                                }
                                                                                                                                float noise2 = 2.0;
                                                                                                                                start = u_adisk_noise_scale;
                                                                                                                                rc = radialCoords + 30.0;
                                                                                                                                for (int i = 0; i < u_adisk_noise_LOD_2; i++) {
                                                                                                                                    noise2 *= 0.5 * iqnoise(rc * start) + 0.5;
                                                                                                                                    start *= 2.0;
                                                                                                                                    rc.y -= float((i % 2) * 2 - 1) * u_time * u_adisk_speed;
                                                                                                                                }*/
    coverage *= fbm2;
    dustColor *= fbm * 0.998 + 0.002;

    float gr = 1.0 - radialGradient;
    gr = gr * gr;
    float glowStrength = 1.0 / (gr * gr * 400.0 + 0.002);
    vec3 glow = Blackbody(2700.0 + glowStrength * 50.0) * glowStrength;
    glow *= sin(p - 1.07) * 0.75 + 1.0;

    dustColor *= glow;

    coverage = saturate(coverage * 1200.0 / float(u_max_steps));
    dustColor = min(vec3(20.0), max(vec3(0.0), dustColor));

    coverage *= pcurve(radialGradient, 4.0, 0.9) * u_coverage_lit;
    float stepTransmittance = exp2(-coverage * 7.0);
    float integral = 1.0 - stepTransmittance;

    color += (1.0 - alpha) * u_adisk_lit * dustColor * integral * 1.4;

    alpha = (1.0 - alpha) * integral + alpha;
    alpha = min(alpha, 1.0);
    //Haze
    vec2 t = vec2(1.0, 0.01);

    float torusDist = length(sdTorus(pos + vec3(0.0, -0.05, 0.0), t));

    float bloomDisc = 1.0 / (pow(torusDist, 2.0) + 0.001);
    vec3 col = MainColor;
    bloomDisc *= length(pos) < 0.5 ? 0.0 : 1.0;

    color += col * bloomDisc * (2.9 / 600.0) * (1.0 - alpha * 1.0);
}

///---
