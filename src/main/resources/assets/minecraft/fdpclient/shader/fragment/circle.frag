#version 120

uniform vec2 size;
uniform vec4 color;

float sdCircle(in vec2 p, in float r)
{
    return length(p) - r;
}

void main()
{
    vec2 halfSize = size * 0.5;

    float b = sdCircle((gl_TexCoord[0].xy * size) - halfSize, min(halfSize.x, halfSize.y) - 1.0);
    vec4 c = mix(color, vec4(color.rgb, 0.0), smoothstep(0.0, 1.0, b));

    gl_FragColor = c;
}