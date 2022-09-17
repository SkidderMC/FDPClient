#version 120

uniform vec2 size;
uniform vec4 color;
uniform float radius;
uniform float smoothness;

float udRoundBox(in vec2 p, in vec2 b, in float r)
{
    return length(max(abs(p) - b + r, 0.0)) - r;
}

void main()
{
    vec2 halfSize = size / 2;
    float minSize = min(halfSize.x, halfSize.y);

    float b = udRoundBox((gl_TexCoord[0].xy * size) - halfSize, halfSize - smoothness, minSize * radius);
    vec4 c = mix(color, vec4(color.rgb, 0.0), smoothstep(0.0, smoothness, b));

    gl_FragColor = c;
}