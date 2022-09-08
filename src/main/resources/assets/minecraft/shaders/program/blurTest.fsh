#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform float Radius;

void main() {
    vec3 blurred = vec3(.0);
    float roughCalc = Radius * 2.0 + 1.0;
    float weight = 0.0;
    float alpha = 0.0;
    for (float r = -Radius; r <= Radius; r += 1.0) {
        vec4 mainTexture = texture2D(DiffuseSampler, texCoord + oneTexel * r * BlurDir);
        if (mainTexture.a <= 0.0) continue;
        blurred += mainTexture.rgb;
        alpha += mainTexture.a;
        weight += 1.0;
    }   
    gl_FragColor = vec4(blurred.r / weight, blurred.g / weight, blurred.b / weight, alpha / roughCalc);
}