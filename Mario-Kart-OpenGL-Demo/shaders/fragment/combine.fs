// combine.fs
//
// take incoming textures and
// add them together, then
// tonemap the result

uniform bool afterGlow;

uniform sampler2D sampler0;
uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D sampler3;
uniform sampler2D sampler4;
uniform sampler2D sampler5;

const float blurIntensity = 0.20;

void main(void)
{
    vec4 temp;

    temp  = texture2D(sampler0, gl_TexCoord[0].st);
    temp += texture2D(sampler1, gl_TexCoord[0].st);
    temp += texture2D(sampler2, gl_TexCoord[0].st);
    temp += texture2D(sampler3, gl_TexCoord[0].st);
    temp += texture2D(sampler4, gl_TexCoord[0].st);

    if (afterGlow)
    {
       temp *= 1.0 - blurIntensity;
       temp += blurIntensity * texture2D(sampler5, gl_TexCoord[0].st);
    }

    gl_FragColor = vec4(temp.rgb, texture2D(sampler0, gl_TexCoord[0].st).a);
}
