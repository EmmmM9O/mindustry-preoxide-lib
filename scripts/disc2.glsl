#version 300 es
precision highp float;
out vec4 fragColor;
uniform vec2 resolution;
uniform float time;
const float pi=3.14159265;
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
    float amplitude = 0.5;
    for (int i = 0; i < octaves; i++) {
        value += amplitude * (periodicPerlinNoise(p, vec2(10000.0, 4.0)) + 1.0) / 2.0;
        p *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}
float createRings(float radius,float angle, float speed) {

    float ringScale = 10.0; 
    float warpFactor = 0.2;
    
    angle += time * speed*4.0;

    vec2 polar = vec2(log(radius) * ringScale, 
                      mod(angle+ warpFactor * radius+pi,2.0*pi)/pi/2.0*4.0);
    
    float rings = discFbm(polar, 5);

    rings = smoothstep(0.3, 0.7, rings);

    float falloff = 1.0 - smoothstep(0.1, 0.9, radius);
    
    return rings * falloff;
}
void main(void) {
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    float outer = 12.6;
    float inner = 2.6;
    float ut = uv.x;
    float phi = uv.y * 2.0 * pi - pi;
    
    float rings1 = createRings(ut,phi, 0.1);
    float rings2 = createRings(ut * 1.5,phi, 0.1);
    float rings3 = createRings(ut * 0.7,phi, 0.1);
    float rings = max(rings1, rings2 * 0.7);
    rings = max(rings, rings3 * 0.5);
    vec3 color = mix(vec3(0.0, 0.1, 0.3), 
                    vec3(0.9, 0.5, 0.1), 
                    pow(rings, 0.5));
    float glow = smoothstep(0.7, 0.3, ut) * 0.5;
    color += vec3(0.8, 0.6, 0.2) * glow * rings;
    color = aces(color);
    color = pow(color, vec3(1.0 / 2.8));
    fragColor = vec4(color,1.0);
}