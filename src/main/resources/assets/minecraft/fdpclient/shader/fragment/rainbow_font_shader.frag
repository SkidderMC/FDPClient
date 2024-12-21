#version 120

uniform float offset;
uniform vec2 strength;
uniform sampler2D font_texture;

void main() {
    vec4 tmpvar_1 = texture2D(font_texture, gl_TexCoord[0].xy);

    if (tmpvar_1.a == 0.0)
        discard;

    vec2 tmpvar_2 = gl_FragCoord.xy * strength;

    vec3 rainbowColor = clamp(
        abs(fract(vec3(mod((tmpvar_2.x + tmpvar_2.y + offset), 1.0))
        + vec3(1.0, 0.6666667, 0.3333333)) * 6.0 - vec3(3.0)) - vec3(1.0),
        0.0, 1.0
    );

    gl_FragColor = vec4(rainbowColor, tmpvar_1.a);
}