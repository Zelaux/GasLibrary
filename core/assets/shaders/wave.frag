#define HIGHP
uniform sampler2D u_texture;
uniform sampler2D region;
varying vec2 v_texCoords;
varying lowp vec4 v_color;
uniform vec2 u_resolution, u_pos, u_uv, u_uv2;
uniform float u_time, u_dscl, u_scl, u_delta,u_forcePercent,u_otherAxisMul;
uniform int u_xAxis;
float trans(float value){
    return value*2.-0.5;
}
void main(){
    vec4 color = texture2D(u_texture, v_texCoords);
    vec2 size=(u_uv2-u_uv);
    vec2 coords=(v_texCoords.xy-u_uv)/size;
    coords.x=trans(coords.x);
    coords.y=trans(coords.y);
    if (u_xAxis==1){
        coords.x+=sin(coords.y*u_otherAxisMul+u_time)*u_forcePercent;
    } else{
        coords.y+=sin(coords.x*u_otherAxisMul+u_time)*u_forcePercent;
    }

    color = texture2D(u_texture, u_uv+coords*size);
    if (coords.x>1. || coords.y>1. || coords.x<0. || coords.y<0.){
        color.a=0.;
    }
    gl_FragColor = vec4(color.rgb, color.a*v_color.a);
}