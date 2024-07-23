#version 330
#extension GL_OES_standard_derivatives : enable

precision highp float;

uniform float iTime;
uniform vec2 iResolution;

const int octaves = 4;
const float seed = 2058.5453123;
const float seed2 = 2.8473192;

const float PI = 3.1415927;

const float ARROW_TILE_SIZE = 80.0;
const float ARROW_HEAD_ANGLE = 10.0 * PI / 180.0;
const float ARROW_HEAD_LENGTH = ARROW_TILE_SIZE / 6.0;
const float ARROW_SHAFT_THICKNESS = 1.0;

float random(float val) {
    return fract(sin(val) * seed);
}

vec2 random2(vec2 st, float seed){
    st = vec2(dot(st, vec2(127.1, 311.7)),
              dot(st, vec2(269.5, 183.3)));
    return -1.0 + 2.0 * fract(sin(st) * seed);
}

float random2d(vec2 uv) {
    return fract(sin(dot(uv.xy, vec2(12.9898, 78.233))) * seed);
}

float noise(vec2 st, float seed) {
    vec2 i = floor(st);
    vec2 f = fract(st);

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(mix(dot(random2(i + vec2(0.0, 0.0), seed), f - vec2(0.0, 0.0)),
                   dot(random2(i + vec2(1.0, 0.0), seed), f - vec2(1.0, 0.0)), u.x),
               mix(dot(random2(i + vec2(0.0, 1.0), seed), f - vec2(0.0, 1.0)),
                   dot(random2(i + vec2(1.0, 1.0), seed), f - vec2(1.0, 1.0)), u.x), u.y);
}

vec3 permute(vec3 x) {
    return mod(((x * 34.0) + 1.0) * x, 289.0);
}

float snoise(vec2 v) {
    const vec4 C = vec4(0.211324865405187, 0.366025403784439,
                        -0.577350269189626, 0.024390243902439);
    vec2 i = floor(v + dot(v, C.yy));
    vec2 x0 = v - i + dot(i, C.xx);
    vec2 i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;
    i = mod(i, 289.0);
    vec3 p = permute(permute(i.y + vec3(0.0, i1.y, 1.0)) +
                     i.x + vec3(0.0, i1.x, 1.0));
    vec3 m = max(0.5 - vec3(dot(x0, x0), dot(x12.xy, x12.xy),
                            dot(x12.zw, x12.zw)), 0.0);
    m = m * m;
    m = m * m;
    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;
    m *= 1.79284291400159 - 0.85373472095314 * (a0 * a0 + h * h);
    vec3 g;
    g.x = a0.x * x0.x + h.x * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;
    return 130.0 * dot(m, g);
}

vec3 plotCircle(vec2 pos, vec2 uv, float size) {
    return vec3(smoothstep(size, size + 0.05, length(uv - pos)));
}

float fbm(in vec2 st, float seed) {
    float value = 0.0;
    float amplitude = .5;

    for (int i = 0; i < octaves; i++) {
        value += amplitude * abs(noise(st, seed));
        st *= 2.0;
        amplitude *= .5;
    }
    return clamp(value, 0.0, 1.0);
}

float fbm1(in vec2 st, float seed) {
    float value = 0.0;
    float amplitude = .5;

    for (int i = 0; i < octaves; i++) {
        value += amplitude * fract(noise(st, seed));
        st *= 2.0;
        amplitude *= .5;
    }
    return value;
}

vec2 arrowTileCenterCoord(vec2 pos) {
    return (floor(pos / ARROW_TILE_SIZE) + 0.5) * ARROW_TILE_SIZE;
}

float arrow(vec2 p, vec2 v) {
    p -= arrowTileCenterCoord(p);

    float mag_v = length(v);
    float mag_p = length(p);

    if (mag_v > 0.0) {
        vec2 dir_p = p / mag_p;
        vec2 dir_v = v / mag_v;

        mag_v = clamp(mag_v, 5.0, ARROW_TILE_SIZE / 2.0);
        v = dir_v * mag_v;

        float dist =
            max(
                ARROW_SHAFT_THICKNESS / 4.0 -
                max(abs(dot(p, vec2(dir_v.y, -dir_v.x))),
                    abs(dot(p, dir_v)) - mag_v + ARROW_HEAD_LENGTH / 2.0),
                min(0.0, dot(v - p, dir_v) - cos(ARROW_HEAD_ANGLE / 2.0) * length(v - p)) * 2.0 +
                min(0.0, dot(p, dir_v) + ARROW_HEAD_LENGTH - mag_v)
            );

        return clamp(1.0 + dist, 0.0, 1.0);
    } else {
        return max(0.0, 1.2 - mag_p);
    }
}

vec3 hsb2rgb(in vec3 c) {
    vec3 rgb = clamp(abs(mod(c.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0,
                     0.0, 1.0);
    rgb = rgb * rgb * (3.0 - 2.0 * rgb);
    return c.z * mix(vec3(1.0), rgb, c.y);
}

vec2 field(vec2 pos) {
    vec2 uv = (pos - 0.5 * iResolution.xy) / iResolution.y;

    uv *= .5;
    uv.x += cos(iTime / 10000.0) * 200.0;
    uv.y += sin(iTime / 10000.0) * 200.0;

    float val1 = fbm(uv, seed);
    float val2 = fbm(uv + vec2(100.0), seed);

    return vec2(val1, val2) * 6.0 - 1.0;
}

void main() {
    vec2 uv = gl_FragCoord.xy;
    uv *= 1.5;
    vec2 _field = field(uv);
    gl_FragColor = vec4(_field * 0.7 + 0.5, 0.5, 1.0);
}