attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_texCoord0;
attribute vec4 a_mix_color;
uniform mat4 u_projTrans;
varying vec4 v_color;
varying vec2 v_texCoords;
uniform float u_time,u_dscl,u_scl,u_delta;

void main(){
    v_color = a_color;
    v_texCoords = a_texCoord0;
    vec4 mulPos=a_position;

    //mulPos.x+=sin(mulPos.y+u_time)*10;
    gl_PointSize=sin(u_time)*10.+10.;
    vec4 pos=u_projTrans * mulPos;
//    pos.x+=0.01;
    gl_Position = pos;
}