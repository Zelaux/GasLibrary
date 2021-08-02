#define HIGHP
varying vec2 v_texCoords;
varying lowp vec4 v_color;
varying vec4 v_pos;
varying vec2 v_viewportInverse;


uniform sampler2D u_texture;
uniform vec2 u_resolution,u_texsize,u_size,u_uv2,u_uv;
uniform float u_time,u_timeMul,u_force,u_scl,u_yOffset;
vec4 getPix(vec2 fragCoord )
{
    float time=u_time;
    vec2 uv = fragCoord/(u_resolution.xy*2.);

    vec3 col = 0.60+0.40*cos(time+uv.xyx+vec3(0,2,4));
    col = 0.70+0.30*cos(time/2.+uv.xyx+vec3(-2,2,-0));

    // Output to screen
    return vec4(col,1.0);
}
void main(){

    float iTime=u_time;
    vec2 texCoord=vec2(v_texCoords.xy);
    vec2 fragCoord=vec2(gl_FragCoord.xy);
    texCoord.x+=cos(texCoord.y+u_timeMul)/(u_force);
//    texCoord.x+=sin(u_timeMul/u_force)*2.-1.;
    vec4 rainbow=vec4(getPix(fragCoord.xy));
    vec4 color = texture2D(u_texture,texCoord);
//    color.rgb=vec3(texCoord.x*texCoord.y);
    vec2 v = vec2(1.0/u_texsize.x, 1.0/u_texsize.y);
//
//    vec3 mulColor=mix(colorFrom.rgb,colorTo.rgb,v_texCoords.y*u_scl);
    if ( texCoord.x>u_uv2.x-v.x || texCoord.x<u_uv.x){
//        color.a+=0.5;
//        color.r=0;
    } else{
//        color.a=0.0;
    }
    gl_FragColor = vec4(color.rgb, color.a);
}