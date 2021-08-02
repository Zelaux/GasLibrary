#define HIGHP
uniform sampler2D u_texture;
varying vec2 v_texCoords;
varying lowp vec4 v_color;
uniform vec2 iResolution;
uniform float u_time;
uniform vec2 u_pos;
uniform vec2 u_screenPos;
uniform float u_length;
uniform float u_scl;
uniform vec4 u_fromColor;
uniform vec4 u_toColor;
float dst(vec2  to,vec2 from){
    float b_x=from.x-to.x;
    float b_y=from.y-to.y;
    return sqrt(b_x*b_x+b_y*b_y);
}
bool isIn(vec2 to,vec2 i,float offset){
    if (to.x>i.x-offset && to.x<i.x+offset){

        if (to.y>i.y-offset && to.y<i.y+offset)return true;
    }
    return false;
}
float getMixValue(in vec2 fragCoord )
{
    vec4 color = texture2D(u_texture, v_texCoords);
    float time=(u_time);
    float dis=dst(fragCoord,u_screenPos);

    vec3 disVec=vec3((dis)/u_length);
    vec3 col = 0.50+0.50*cos(u_time/2.+disVec.xyz);

    return col.x;
}
void main(){
    float mixValue=getMixValue(gl_FragCoord.xy);
    vec4 rainbow=mix(u_fromColor,u_toColor,mixValue);
    vec4 color = texture2D(u_texture, v_texCoords);

    gl_FragColor = vec4(rainbow.rgb*color.rgb, color.a*v_color.a);
}