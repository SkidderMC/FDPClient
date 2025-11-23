#version 120

uniform sampler2D inTexture;
uniform vec2 offset, halfpixel;

void main() {
    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st) * 4.0;
    sum += texture2D(inTexture, gl_TexCoord[0].st - halfpixel.xy * offset);
    sum += texture2D(inTexture, gl_TexCoord[0].st + halfpixel.xy * offset);
    sum += texture2D(inTexture, gl_TexCoord[0].st + vec2(halfpixel.x, -halfpixel.y) * offset);
    sum += texture2D(inTexture, gl_TexCoord[0].st - vec2(halfpixel.x, -halfpixel.y) * offset);
    gl_FragColor = vec4(sum.rgb / 8.0, 1.0);
}
