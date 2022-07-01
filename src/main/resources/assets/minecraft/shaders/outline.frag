#version 120

uniform vec2 texelSize, direction;
uniform sampler2D texture;
uniform float radius;
uniform vec3 color;

#define offset direction * texelSize

void main() {
    float centerAlpha = texture2D(texture, gl_TexCoord[0].xy).a;
    float innerAlpha = centerAlpha;
    for (float r = 1.0; r <= radius; r++) {
        float alphaCurrent1 = texture2D(texture, gl_TexCoord[0].xy + offset * r).a;
        float alphaCurrent2 = texture2D(texture, gl_TexCoord[0].xy - offset * r).a;

       innerAlpha += alphaCurrent1 + alphaCurrent2;
    }

    gl_FragColor = vec4(color, innerAlpha) * step(0.0, -centerAlpha);
}

