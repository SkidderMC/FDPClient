#version 120
uniform sampler2D textureIn;
uniform float force;
void main() {
    vec4 original = texture2D(textureIn, gl_TexCoord[0].st);
    float d = (original.r + original.b + original.g) / force;
    gl_FragColor = vec4(d, d, d, 1);
}
