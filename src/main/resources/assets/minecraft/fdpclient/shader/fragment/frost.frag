#version 120

uniform sampler2D texture;
uniform vec2 texelSize;
uniform float radius;
uniform float alpha;
uniform float intensity;
uniform vec3 tintColor;

// Enhanced Gaussian weights for stronger blur
const float weights[5] = float[5](0.27027, 0.21621, 0.13513, 0.08108, 0.02702);

float hash(vec2 p) {
    float h = dot(p, vec2(127.1, 311.7));
    return fract(sin(h) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

// Blur
vec4 blur13(sampler2D image, vec2 uv, vec2 direction) {
    vec4 color = texture2D(image, uv) * weights[0];
    float totalWeight = weights[0];

    for(int i = 1; i < 5; i++) {
        vec2 offset = direction * texelSize * float(i) * radius * 2.0; // Doubled sampling distance
        vec4 sampleA = texture2D(image, uv + offset);
        vec4 sampleB = texture2D(image, uv - offset);

        float weight = weights[i];
        color += sampleA * weight + sampleB * weight;
        totalWeight += 2.0 * weight;
    }

    return color / totalWeight;
}

// Distortion
vec2 distort(vec2 uv) {
    float distortionStrength = intensity * 0.05; // Increased distortion
    vec2 noise1 = vec2(noise(uv * 8.0), noise(uv * 8.0 + 0.5));
    vec2 noise2 = vec2(noise(uv * 15.0 + 1.0), noise(uv * 15.0 + 1.5));

    return uv + (noise1 + noise2 - 1.0) * distortionStrength;
}

void main() {
    vec2 uv = gl_TexCoord[0].xy;

    vec2 distortedUV = distort(uv);

    // two-pass gaussian blur with distortion
    vec4 color = blur13(texture, distortedUV, vec2(1.0, 0.0));
    color = blur13(texture, distortedUV, vec2(0.0, 1.0));

    float frost = noise(uv * 12.0) * noise(uv * 18.0) * 1.5;
    vec4 frostColor = vec4(tintColor * (0.75 + 0.25 * frost), 1.0);

    color = mix(color, frostColor, intensity * 0.8);

    float highlight = pow(frost, 3.0) * 0.8;
    color.rgb += vec3(highlight) * intensity * 1.5;

    // Add subtle frost edges
    float edge = smoothstep(0.4, 0.6, frost);
    color.rgb += tintColor * edge * intensity * 0.4;

    gl_FragColor = vec4(color.rgb, color.a * alpha);
}