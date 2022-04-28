#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform vec2 InSize;

uniform vec2 BlurDir;
uniform vec2 BlurXY;
uniform vec2 BlurCoord;
uniform float Radius;

float SCurve (float x) {


    // ---- by CeeJayDK

    x = x * 2.0 - 1.0;
    return -x * abs(x) * 0.5 + x + 0.5;

    //return dot(vec3(-x, 2.0, 1.0 ),vec3(abs(x), x, 1.0)) * 0.5; // possibly faster version




    // ---- original for posterity

    // How to do this without if-then-else?
    // +edited the too steep curve value

    // if (value < 0.5)
    // {
    //    return value * value * 2.0;
    // }

    // else
    // {
    // 	value -= 1.0;

    // 	return 1.0 - value * value * 2.0;
    // }
}

vec4 BlurH (sampler2D source, vec2 size, vec2 uv, float radius) {

    if (uv.x / oneTexel.x >= BlurXY.x * 2 && uv.y / oneTexel.y >= BlurXY.y * 2 && uv.x / oneTexel.x <= (BlurCoord.x + BlurXY.x) * 2 && uv.y / oneTexel.y <= (BlurCoord.y + BlurXY.y) * 2)
    {
        vec4 A = vec4(0.0);
        vec4 C = vec4(0.0);

        float divisor = 0.0;
        float weight = 0.0;

        float radiusMultiplier = 1.0 / radius;

        // Hardcoded for radius 20 (normally we input the radius
        // in there), needs to be literal here

        for (float x = -radius; x <= radius; x++)
        {
            A = texture2D(source, uv + vec2(x * size) * BlurDir);

            weight = SCurve(1.0 - (abs(x) * radiusMultiplier));

            C += A * weight;

            divisor += weight;
        }

        return vec4(C.r / divisor, C.g / divisor, C.b / divisor, 1.0);
    }

    return texture2D(source, uv);
}

void main() {
    vec4 blurred = BlurH(DiffuseSampler, oneTexel, texCoord, Radius);
    gl_FragColor = blurred;
}
