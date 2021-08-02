#define HIGHP
uniform sampler2D u_texture;
uniform float u_time;
varying vec2 v_texCoords;
varying lowp vec4 v_color;
uniform vec2 iResolution;

vec4 getPix(in vec2 fragCoord )
{
    float time=(u_time);
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = fragCoord/(iResolution.xy*2.);

    // Time varying pixel color
    vec3 col = 0.60+0.40*cos(time+uv.xyx+vec3(0,2,4));
    col = 0.70+0.30*sin(time/2.+uv.xyx+vec3(-2,2,-0));

    // Output to screen
    return vec4(col,1.0);
}
void main(){
    vec4 rainbow=vec4(getPix(vec2(gl_FragCoord.x,gl_FragCoord.y)));
    //color.r*=v_color.r;
    //color.g*=v_color.g;
    //color.b*=v_color.b;
    //color.a*=v_color.a;
    //vec4 color = texture2D(v_texCoords, u_projTrans.xy);
    vec4 color = texture2D(u_texture, v_texCoords);
    gl_FragColor = vec4(rainbow.rgb*color.rgb, color.a);
    //    gl_FragColor = v_color * mix(color, vec4(rainbow.rgb, color.a), color.a);
    /*
    vec4 color = texture2D(u_texture, v_texCoord.xy);
    float t = clamp((sin(u_time * .01 + gl_FragCoord.x * .01 + gl_FragCoord.y * .005) + 1.) / 2., 0., 1.);
    vec3 c = vec3(mix(0., 1., t), mix(.89, .39, t), mix(1., .85, t));
    gl_FragColor = vec4(color.rgb * rainbow.rgb, color.a);
    */
    //gl_FragColor = vec4(rainbow.rgb*v_color.rgb,1.);
}