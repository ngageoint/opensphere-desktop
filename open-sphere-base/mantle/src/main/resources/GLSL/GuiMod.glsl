// uDrift range 0 to 1
uniform float uDrift;

vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    vec4 fragColor = vec4(0.0);
    
    // TODO can this be a step function?
    if ((texColor.g > 0.01 || texColor.r > 0.01 || texColor.b > 0.01) && (texColor.g > uDrift  || texColor.r > uDrift  || texColor.b > uDrift))
    {
        fragColor.r = 0.5;
        fragColor.g = 0.5;
        fragColor.b = 0.5;
        fragColor.a = 0.5;
    }
    else
    {
        fragColor.r = clamp(texColor.r + gl_Color.r, 0.0, 1.0);
        fragColor.g = clamp(texColor.g + gl_Color.g, 0.0, 1.0);
        fragColor.b = clamp(texColor.b + gl_Color.b, 0.0, 1.0);
        fragColor.a = texColor.a * gl_Color.a;
    }
    
    return fragColor;
}
