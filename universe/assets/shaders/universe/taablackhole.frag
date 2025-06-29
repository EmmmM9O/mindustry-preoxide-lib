#define HIGHP
#define TEMPORAL_AA
uniform float u_time;

uniform float u_fov_scale;
uniform vec2 u_resolution;
uniform vec3 u_camera_pos;
uniform mat3 u_camera_mat;
uniform int u_max_steps;
uniform int u_LOD_1;
uniform int u_LOD_2;
uniform float u_start_distance;
uniform float u_speed;
uniform samplerCube u_cubemap;

uniform sampler2D u_backbuffer;
uniform float u_blend_weight;

uniform sampler2D u_noise;

layout(location = 0) out vec4 taa;
layout(location = 1) out vec4 background;
const vec3 MainColor = vec3(1.1, 1.1, 1.0);

float iqnoise(in vec3 x)
{
    vec3 p = floor(x);
    vec3 f = fract(x);
    f = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
    //f = f * f * (3.0 - 2.0 * f);
    vec2 uv = (p.xy + vec2(37.0, 17.0) * p.z) + f.xy;
    vec2 rg = textureLod(u_noise, (uv + 0.5) / 256.0 / 1.0, 0.0).yx;
    return -0.7 + 2.0 * mix(rg.x, rg.y, f.z);
}
float saturate(float x)
{
    return clamp(x, 0.0, 1.0);
}

vec3 saturate(vec3 x)
{
    return clamp(x, vec3(0.0), vec3(1.0));
}

float rand(vec2 coord)
{
    return saturate(fract(sin(dot(coord, vec2(12.9898, 78.223))) * 43758.5453));
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

float sdSphere(vec3 p, float r)
{
    return length(p) - r;
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
void Haze(inout vec3 color, vec3 pos, float alpha)
{
    vec2 t = vec2(1.0, 0.01);

    float torusDist = length(sdTorus(pos + vec3(0.0, -0.05, 0.0), t));

    float bloomDisc = 1.0 / (pow(torusDist, 2.0) + 0.001);
    vec3 col = MainColor;
    bloomDisc *= length(pos) < 0.5 ? 0.0 : 1.0;

    color += col * bloomDisc * (10.0 / float(u_max_steps)) * (1.0 - alpha * 1.0);
}
void GasDisc(inout vec3 color, inout float alpha, vec3 pos)
{
    float discRadius = 2.4;
    float discWidth = 3.8;
    float discInner = discRadius - discWidth * 0.5;
    float discOuter = discRadius + discWidth * 0.5;

    vec3 origin = vec3(0.0, 0.0, 0.0);
    float mouseZ = 0.0;
    vec3 discNormal = normalize(vec3(0.0, 1.0, 0.0));
    float discThickness = 0.05;

    float distFromCenter = distance(pos, origin);
    float distFromDisc = dot(discNormal, pos - origin) * 1.5;

    float radialGradient = 1.0 - saturate((distFromCenter - discInner) / discWidth * 0.5);

    float coverage = pcurve(radialGradient, 4.0, 0.9) * 1.2;

    discThickness *= radialGradient;
    coverage *= saturate(1.0 - abs(distFromDisc) / discThickness);

    vec3 dustColorLit = MainColor;
    vec3 dustColorDark = vec3(0.0, 0.0, 0.0);

    float dustGlow = 1.0 / (pow(1.0 - radialGradient, 2.0) * 290.0 + 0.002);
    vec3 dustColor = dustColorLit * dustGlow * 8.2;

    coverage = saturate(coverage * 0.7);

    float fade = pow((abs(distFromCenter - discInner) + 0.4), 4.0) * 0.04;
    float bloomFactor = 1.0 / (pow(distFromDisc, 2.0) * 80.0 + fade + 0.00002);
    vec3 b = dustColorLit * pow(bloomFactor, 1.7);

    b *= mix(vec3(1.7, 1.1, 1.0), vec3(0.5, 0.6, 1.0), vec3(pow(radialGradient, 2.0)));
    b *= mix(vec3(1.7, 0.5, 0.1), vec3(1.0), vec3(pow(radialGradient, 0.5)));

    dustColor = mix(dustColor, b * 150.0, saturate(1.0 - coverage * 1.0));
    coverage = saturate(coverage + bloomFactor * bloomFactor * 0.1);
    if (distFromCenter >= discOuter) return;
    if (coverage < 0.01)
    {
        return;
    }

    float p = atan2(-pos.x, -pos.z);
    vec3 radialCoords;
    radialCoords.x = distFromCenter * 4.5 + 0.55;
    radialCoords.y = p * 0.1;
    radialCoords.z = distFromDisc * 4.5;

    radialCoords *= 1.2;

    float noise1 = 1.15;
    vec3 rc = radialCoords + 0.0;
    float start = 3.0;
    for (int i = 0; i < u_LOD_1; i++) {
        rc.y -= float((i % 2) * 2 - 1) * u_time * u_speed;
        noise1 *= 0.5 * iqnoise(rc * start) + 0.5;
        start *= 2.0;
    }
    start = 3.0;
    float noise2 = 3.0;
    rc = radialCoords + 30.0;
    for (int i = 0; i < u_LOD_2; i++) {
        rc.y -= float((i % 2) * 2 - 1) * u_time * u_speed;
        noise2 *= 0.5 * iqnoise(rc * start) + 0.5;
        start *= 2.0;
    }

    dustColor *= noise1 * 0.998 + 0.002;
    coverage *= noise2;

    radialCoords.y += u_time * u_speed * 1.0;

    coverage = saturate(coverage * 1200.0 / float(u_max_steps));
    dustColor = max(vec3(0.0), dustColor);

    coverage *= pcurve(radialGradient, 4.0, 0.9);
    float gr = 1.0 - radialGradient;
    gr = gr * gr;
    float glowStrength = 1.0 / (pow(gr, 1.35) * 500.0 + 1.0);
    vec3 glow = Blackbody(2700.0 + glowStrength * 200.0) * glowStrength;
    glow *= sin(p - 1.07) * 0.75 + 1.0;
    dustColor *= pow(glow, vec3(0.95));
    color = (1.0 - alpha) * dustColor * coverage + color;

    alpha = (1.0 - alpha) * coverage + alpha;
}

void WarpSpace(inout vec3 eyevec, inout vec3 raypos)
{
    vec3 origin = vec3(0.0, 0.0, 0.0);

    float singularityDist = distance(raypos, origin);
    float warpFactor = 1.0 / (pow(singularityDist, 2.0) + 0.000001);

    vec3 singularityVector = normalize(origin - raypos);

    float warpAmount = 5.0;

    eyevec = normalize(eyevec + singularityVector * warpFactor * warpAmount / float(u_max_steps));
}
void main()
{
    vec2 uv = gl_FragCoord.xy / u_resolution.xy;

    float aspect = u_resolution.x / u_resolution.y;

    vec2 uveye = uv;
    uveye -= vec2(0.5);
    float scl = 1.4;
    #ifdef TEMPORAL_AA
    uveye.x += (rand(uv + sin(u_time * 1.0)) / u_resolution.x) * scl;
    uveye.y += (rand(uv + 1.0 + sin(u_time * 1.0)) / u_resolution.y) * scl;
    #endif
    uveye.x *= aspect;
    vec3 eyevec =
        normalize(vec3(-uveye.x * u_fov_scale, uveye.y * u_fov_scale, 1.0));

    eyevec = normalize(eyevec * u_camera_mat);

    vec3 color = vec3(0.0, 0.0, 0.0);

    float dither = rand(uv
                #ifdef TEMPORAL_AA
                + sin(u_time * 1.0)
        #endif
        ) * 2.0;

    float alpha = 0.0;
    vec3 h = cross(u_camera_pos, eyevec);
    float h2 = dot(h, h);
    float s2 = u_start_distance * u_start_distance;
    if (h2 >= s2) {
        taa = vec4(0.0);
        background = vec4(texture(u_cubemap, eyevec).rgb * 1.0, 1.0);
        return;
    }
    float len2 = dot(u_camera_pos, u_camera_pos);
    float du = len2 - h2;
    vec3 raypos = u_camera_pos + eyevec * (dither * u_start_distance / float(u_max_steps) /* + max((length(u_camera_pos) - u_start_distance), 0.0)*/
                    + max(sqrt(max(du, 0.0)) - u_start_distance, 0.0));
    bool flag;
    for (int i = 0; i < u_max_steps; i++)
    {
        WarpSpace(eyevec, raypos);
        raypos += eyevec * (u_start_distance + 2.0f) / float(u_max_steps);
        GasDisc(color, alpha, raypos);
        Haze(color, raypos, alpha);
        if (length(raypos) <= 0.2) {
            flag = true;
            background = vec4(0.0, 0.0, 0.0, 1.0);
        }
    }
    color *= 0.0011;

    #ifdef TEMPORAL_AA
    const float p = 1.0;

    vec3 previous = pow(texture(u_backbuffer, uv).rgb, vec3(1.0 / p));
    color = pow(color, vec3(1.0 / p));
    color = mix(color, previous, u_blend_weight);
    color = pow(color, vec3(p));
    #endif

    taa = vec4(saturate(color), 1.0);
    if (!flag)
        background = vec4(texture(u_cubemap, eyevec).rgb * (1.0 - alpha), 1.0);
}
