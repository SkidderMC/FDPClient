#version 120

uniform sampler2D texture;
uniform vec2 texelSize;
uniform float radius;
uniform float alpha;
uniform float intensity;

void main() {
    vec4 color = vec4(0.0);
    float count = 0.0;

    // Sample pixels in a radius for the blur effect
    for(float x = -radius; x <= radius; x++) {
        for(float y = -radius; y <= radius; y++) {
            vec2 offset = vec2(x, y) * texelSize;
            color += texture2D(texture, gl_TexCoord[0].xy + offset);
            count += 1.0;
        }
    }

    // Average the sampled colors and add white tint based on intensity
    color = color / count;
    color = mix(color, vec4(1.0), intensity);
    gl_FragColor = vec4(color.rgb, color.a * alpha);
}