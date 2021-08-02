
varying lowp vec4 v_color;
varying highp vec2 v_texCoords;
uniform highp sampler2D u_texture;
uniform float u_resolution,toY,lenght;
void main(){
    vec4 c = texture2D(u_texture, v_texCoords);
    float y=gl_FragCoord.y;
    float i=(y<=toY?1.:0.);
//    i=1.;
    gl_FragColor = vec4(c.rgb,i*c.a)*v_color;
}