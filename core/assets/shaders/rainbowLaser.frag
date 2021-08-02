#define HIGHP
uniform sampler2D u_texture;
varying vec2 v_texCoords;
varying lowp vec4 v_color;
uniform vec2 iResolution,u_grow,u_pos,u_vecRot,u_screenPos;
uniform float u_time,u_length,u_scl,u_bulletRot;
uniform vec3 u_offset;
float dst(vec2  to,vec2 from){
    return distance(to,from);
}
bool isIn(vec2 to,vec2 i,float offset){
    if (to.x>i.x-offset && to.x<i.x+offset){

        if (to.y>i.y-offset && to.y<i.y+offset)return true;
    }
    return false;
}
vec2 rotVec2(vec2 vec,float deg){
    float rad=radians(deg);
    float c=cos(rad);
    float s=sin(rad);
    float nx=vec.x*c-vec.y*s;
    float ny=vec.x*s+vec.y*c;
    vec.x=nx;
    vec.y=ny;
    return vec;
}
vec4 getPix(in vec2 fragCoord )
{
    vec4 color = texture2D(u_texture, v_texCoords);
    if (color.a==0.)return vec4(0);
    /* if (isIn(fragCoord,u_screenPos.xy,10.)){
         return vec4(1.);
     }
    if (true) return vec4(0);*/
    float time=(u_time);
    float dis=dst(fragCoord,u_screenPos);
float app=((dis)/u_length)*1.;
    vec2 disVec=vec2(0.);
    disVec.x=app;
    disVec=rotVec2(disVec,u_bulletRot);
    disVec.x*=-1.;
    vec3 col = 0.50+0.50*cos(u_time+disVec.xyx+u_offset);

    // Output to screen
    return vec4(col,1.0);
}
void main(){
    vec4 rainbow=vec4(getPix(vec2(gl_FragCoord.x,gl_FragCoord.y)));
    vec4 color = texture2D(u_texture, v_texCoords);

    gl_FragColor = vec4(rainbow.rgb*color.rgb, color.a*v_color.a);
}