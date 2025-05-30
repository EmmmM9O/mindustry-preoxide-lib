uniform sampler2D u_noise_tex;
uniform sampler2D u_color_map;
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
    radialCoords.x = distFromCenter * 1.5 + 0.55;
    radialCoords.y = atan2(-pos.x, -pos.z) * 1.5;
    radialCoords.z = distFromDisc * 1.5;

    radialCoords *= 0.95;
    vec3 rc = radialCoords + 0.0;
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
    }
    dustColor *= noise1 * 0.998 + 0.002;
    coverage *= noise2;

    radialCoords.y += u_time * u_adisk_speed * 0.5;

    dustColor *= pow(texture(u_color_map, radialCoords.yx * vec2(0.15, 0.27)).rgb, vec3(2.0)) * 4.0;

    coverage = saturate(coverage * 1200.0 / float(u_max_steps));
    dustColor = max(vec3(0.0), dustColor);

    coverage *= pcurve(radialGradient, 4.0, 0.9) * u_coverage_lit;

    color += (1.0 - alpha) * u_adisk_lit * dustColor * coverage;

    alpha = (1.0 - alpha) * coverage + alpha;
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
