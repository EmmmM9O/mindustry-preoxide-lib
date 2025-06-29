uniform float u_fov_scale;
uniform float u_flare_scl;
uniform float u_len;
uniform vec2 u_resolution;
uniform vec3 u_camera_pos;
uniform mat3 u_camera_mat_inv;
uniform sampler2D u_input;
uniform sampler2D u_background;
const vec3 flareColor = vec3(0.643, 0.494, 0.867);
// from https://www.shadertoy.com/view/XdfXRX
vec3 lensflares(vec2 uv, vec2 pos, out vec3 sunflare, out vec3 lensflare)
{
    vec2 main = uv - pos;
    vec2 uvd = uv * (length(uv));

    float ang = atan(main.y, main.x);
    float dist = length(main);
    dist = pow(dist, 0.1);

    float f0 = 1.0 / (length(uv - pos) * 25.0 + 1.0);
    f0 = pow(f0, 2.0);

    f0 = f0 + f0 * (sin((ang + 1.0 / 18.0) * 12.0) * .1 + dist * .1 + .8);

    float f2 = max(1.0 / (1.0 + 32.0 * pow(length(uvd + 0.8 * pos), 2.0)), .0) * 00.25;
    float f22 = max(1.0 / (1.0 + 32.0 * pow(length(uvd + 0.85 * pos), 2.0)), .0) * 00.23;
    float f23 = max(1.0 / (1.0 + 32.0 * pow(length(uvd + 0.9 * pos), 2.0)), .0) * 00.21;

    vec2 uvx = mix(uv, uvd, -0.5);

    float f4 = max(0.01 - pow(length(uvx + 0.4 * pos), 2.4), .0) * 6.0;
    float f42 = max(0.01 - pow(length(uvx + 0.45 * pos), 2.4), .0) * 5.0;
    float f43 = max(0.01 - pow(length(uvx + 0.5 * pos), 2.4), .0) * 3.0;

    uvx = mix(uv, uvd, -.4);

    float f5 = max(0.01 - pow(length(uvx + 0.2 * pos), 5.5), .0) * 2.0;
    float f52 = max(0.01 - pow(length(uvx + 0.4 * pos), 5.5), .0) * 2.0;
    float f53 = max(0.01 - pow(length(uvx + 0.6 * pos), 5.5), .0) * 2.0;

    uvx = mix(uv, uvd, -0.5);

    float f6 = max(0.01 - pow(length(uvx - 0.3 * pos), 1.6), .0) * 6.0;
    float f62 = max(0.01 - pow(length(uvx - 0.325 * pos), 1.6), .0) * 3.0;
    float f63 = max(0.01 - pow(length(uvx - 0.35 * pos), 1.6), .0) * 5.0;

    lensflare = vec3(f2 + f4 + f5 + f6, f22 + f42 + f52 + f62, f23 + f43 + f53 + f63);

    return lensflare;
}
void main()
{
    vec2 uv = gl_FragCoord.xy / u_resolution.xy;

    float aspect = u_resolution.x / u_resolution.y;
    vec3 color = vec3(0.0, 0.0, 0.0);
    color += texture(u_input, uv).rgb * 28.0;
    color += texture(u_background, uv).rgb;
    uv -= vec2(0.5);
    uv.x *= aspect;
    vec3 dir = normalize(-u_camera_pos) * u_camera_mat_inv;
    dir *= 1.0 / dir.z;

    //    vec3 sunflare, lensflare;
    //    vec3 flare = lensflares(uv * u_len, dir.xy / vec2(-u_fov_scale, u_fov_scale) * u_len, sunflare, lensflare);
    //    color += flare * flareColor * u_flare_scl;
    gl_FragColor = vec4(color, 1.0);
}
