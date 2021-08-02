#define HIGHP
uniform sampler2D u_texture;
uniform sampler2D region;
varying vec2 v_texCoords;
varying lowp vec4 v_color;
uniform vec2 u_resolution, u_pos, u_uv, u_uv2;
uniform float u_time, u_dscl, u_scl, u_delta;
float sd=0.;
bool isIn(vec2 to, vec2 i, float offset){
    if (to.x>i.x-offset && to.x<i.x+offset){

        if (to.y>i.y-offset && to.y<i.y+offset)return true;
    }
    return false;
}
float dst(vec2  to, vec2 from){
    return distance(to, from);
}
float trans(float value){
    return value*2.-0.5;
}
void main(){
    vec2 uv =gl_FragCoord.xy/ (u_resolution.xy);
    vec3 gray=vec3(uv.x*uv.y);
    if (isIn(gl_FragCoord.xy, u_pos, 8.*u_dscl)){
        gray=vec3(0.);
    } else {
        gray=vec3(1.);
    }
    if (dst(u_pos, gl_FragCoord.xy)<8.*u_dscl){
        gray.r=0.;
    } else {
        gray.r=1.;
    }
    vec4 color = texture2D(u_texture, v_texCoords);
    vec2 size=(u_uv2-u_uv);
    vec2 coords=(v_texCoords.xy-u_uv)/size;
    //    color=texture2D(u_texture,uv*(u_ruv2-u_ruv)+u_ruv);
    vec2 muls=abs(coords)/coords;
//    coords.x=coords.x/0.5-0.25*muls.x;
//    coords.y=coords.y/0.5-0.25*muls.y;
    coords.x=trans(coords.x);
    coords.y=trans(coords.y);
    coords.x+=sin(coords.y*10.+u_time)*0.25*muls.x;

    color = texture2D(u_texture, u_uv+coords*size);
    if (coords.x>1. || coords.y>1. || coords.x<0. || coords.y<0.){
        color.a=0.;
    }
    //    color = texture2D(u_texture, v_texCoords);
    //    gray=vec3(v_texCoords.x*v_texCoords.y);
    gl_FragColor = vec4(color.rgb, color.a*v_color.a);
}