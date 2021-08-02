#define HIGHP
varying vec2 v_texCoords;
varying lowp vec4 v_color;
varying vec4 v_pos;
varying vec2 v_viewportInverse;


uniform sampler2D u_texture;
uniform float u_time;
uniform vec2 iResolution;
uniform vec2 u_texsize;
uniform vec2 u_size;
uniform float u_timeMul;
uniform float u_mul1;
uniform float u_scl;
uniform vec4 colorFrom;
uniform vec4 colorTo;
vec4 getPix(vec2 fragCoord )
{
    float time=u_time;
    vec2 uv = fragCoord/(iResolution.xy*2.);

    vec3 col = 0.60+0.40*cos(time+uv.xyx+vec3(0,2,4));
    col = 0.70+0.30*cos(time/2.+uv.xyx+vec3(-2,2,-0));

    // Output to screen
    return vec4(col,1.0);
}

float rand(vec2 co){return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);}
float rand (vec2 co, float l) {return rand(vec2(rand(co), l));}
float rand (vec2 co, float l, float t) {return rand(vec2(rand(co, l), t));}

float perlin(vec2 p, float dim, float time) {
    float M_PI=3.141592;
    vec2 pos = floor(p * dim);
    vec2 posx = pos + vec2(1.0, 0.0);
    vec2 posy = pos + vec2(0.0, 1.0);
    vec2 posxy = pos + vec2(1.0);

    float c = rand(pos, dim, time);
    float cx = rand(posx, dim, time);
    float cy = rand(posy, dim, time);
    float cxy = rand(posxy, dim, time);

    vec2 d = fract(p * dim);
    d = -0.5 * cos(d * M_PI) + 0.5;

    float ccx = mix(c, cx, d.x);
    float cycxy = mix(cy, cxy, d.x);
    float center = mix(ccx, cycxy, d.y);

    return center * 2.0 - 1.0;
}
float perlin(vec2 p, float dim) {
    return perlin(p, dim, 0.0);
}
void main(){

    float iTime=u_time;
    vec2 texCoord=vec2(v_texCoords.xy);
    vec2 fragCoord=vec2(gl_FragCoord.xy);
    //    texCoord.x+=cos(fragCoord.y+u_timeMul)/(iResolution.y*u_mul1);
    vec4 rainbow=vec4(getPix(fragCoord.xy));
    vec4 color = texture2D(u_texture,texCoord);


    vec3 mulColor=mix(colorFrom.rgb,colorTo.rgb,v_texCoords.y*u_scl);
    if (u_texsize.x<=texCoord.x){
        color.a=0.0;
    }
    gl_FragColor = vec4(mix(mulColor.rgb*color.rgb,color.rgb,0.3), color.a);
}