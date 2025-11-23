#version 120

uniform sampler2D inTexture;
uniform vec2 halfpixel, offset;

void main() {
    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st + vec2(-halfpixel.x * 2.0, 0.0) * offset);
    sum += texture2D(inTexture, gl_TexCoord[0].st + vec2(-halfpixel.x, halfpixel.y) * offset) * 2.0;
    sum += texture2D(inTexture, gl_TexCoord[0].st + vec2(0.0, halfpixel.y * 2.0) * offset);
    sum += texture2D(inTexture, gl_TexCoord[0].st + vec2(halfpixel.x, halfpixel.y) * offset) * 2.0;
    sum += texture2D(inTexture, gl_TexCoord[0].st + vec2(halfpixel.x * 2.0, 0.0) * offset);
    sum += texture2D(inTexture, gl_TexCoord[0].st + vec2(halfpixel.x, -halfpixel.y) * offset) * 2.0;
    sum += texture2D(inTexture, gl_TexCoord[0].st + vec2(0.0, -halfpixel.y * 2.0) * offset);
    sum += texture2D(inTexture, gl_TexCoord[0].st + vec2(-halfpixel.x, -halfpixel.y) * offset) * 2.0;

    gl_FragColor = vec4(sum.rgb / 12.0, 1.);
}
