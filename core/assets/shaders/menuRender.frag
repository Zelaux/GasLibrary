#define HIGHP
uniform sampler2D u_texture;
uniform float u_time;
varying vec2 v_texCoords;
varying lowp vec4 v_color;
uniform vec2 iResolution;

vec4 getPix(in vec2 fragCoord )
{
    float time=u_time;
    vec2 uv = fragCoord/(iResolution.xy*2.);

    vec3 col = 0.60+0.40*cos(time+uv.xyx+vec3(0,2,4));
    col = 0.70+0.30*cos(time/2.+uv.xyx+vec3(-2,2,-0));

    // Output to screen
    return vec4(col,1.0);
}
void main(){
    vec4 rainbow=vec4(getPix(vec2(gl_FragCoord.x,gl_FragCoord.y)));
    vec4 color = texture2D(u_texture, v_texCoords);
    //vec4 colorLogo = texture2D(u_textureLogo, v_texCoords);
    //gl_FragColor = vec4(rainbow.rgb, 1);
    float val=-0.5;
    gl_FragColor = vec4(color.rgb*vec3(val,val,val), v_color.a);
}