uniform float iTime;
uniform vec2 iMouse;
uniform vec2 iResolution;


void main(void){

    vec2 uv = -1.0 + 2.0*gl_FragCoord.xy / iResolution.xy;
    uv.x *=  iResolution.x / iResolution.y;
    vec2 ms = (iMouse.xy / iResolution.xy);

    // background
    vec3 color = vec3(0.9 + 0.1*uv.y);

    // bubbles
    for( int i=0; i<40; i++ )
    {
        // bubble seeds
        float pha =      sin(float(i)*546.13+1.0)*0.5 + 0.5;
        float siz = pow( sin(float(i)*651.74+5.0)*0.5 + 0.5, 4.0 );
        float pox =      sin(float(i)*321.55+4.1) * iResolution.x / iResolution.y;

        // buble size, position and color
        float rad = 0.2 + 0.5*siz;
        vec2  pos = vec2( pox, -1.0-rad + (2.0+2.0*rad)*mod(pha+0.1*iTime*(0.1+0.1*siz),1.0));
        float distToMs = length(pos - ms);
        pos *= length(pos - (ms * 1.5-0.5));
        float dis = length( uv - pos );


        vec3  col = mix( vec3(0.1,0.2,0.8), vec3(0.1,0.4,0.8), 0.5+0.5*sin(float(i)*1.2+1.9));
        //    col+= 8.0*smoothstep( rad*0.95, rad, dis );

        // render
        float f = length(uv-pos)/rad;
        f = sqrt(clamp(1.0-f*f,0.0,1.0));
        color -= col.zyx *(1.0-smoothstep( rad*0.95, rad, dis )) * f;
    }

    // vigneting
    color *= sqrt(1.5-0.5*length(uv));

    gl_FragColor = vec4(color,1.0);
}