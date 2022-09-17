#version 120

uniform vec2 size;
uniform vec4 color;
uniform vec4 radius;
uniform float smoothness;

float sdRoundBox(in vec2 p, in vec2 b, in vec4 r)
{
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;

    vec2 q = abs(p) - b + r.x;

    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

void main()
{
    vec2 halfSize = size / 2;
    float minSize = min(halfSize.x, halfSize.y);

    float b = sdRoundBox((gl_TexCoord[0].xy * size) - halfSize, halfSize - smoothness, minSize * radius);
    vec4 c = mix(color, vec4(color.rgb, 0.0), smoothstep(0.0, smoothness, b));

    gl_FragColor = c;
}