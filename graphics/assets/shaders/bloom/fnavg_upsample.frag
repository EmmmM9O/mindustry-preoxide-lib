varying vec2 v_uv;

uniform vec2 resolution;
uniform sampler2D texture0;
uniform sampler2D texture1;

void main() {
    vec2 inputTexelSize = 1.0 / resolution * 0.5;
    vec4 o = inputTexelSize.xyxy * vec4(-1.0, -1.0, 1.0, 1.0); // Offset
    gl_FragColor =
        0.25 * (texture(texture0, v_uv + o.xy) + texture(texture0, v_uv + o.zy) +
                texture(texture0, v_uv + o.xw) + texture(texture0, v_uv + o.zw));

    gl_FragColor += texture(texture1, v_uv);
    gl_FragColor.a = 1.0;
}
