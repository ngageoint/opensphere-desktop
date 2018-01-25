uniform vec3 uOverrideColor;

// Override any color to the given color, but leave the alpha the same.
vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    vec4 fragColor = vec4(0.0);
    
    fragColor.r = clamp(uOverrideColor.r + gl_Color.r, 0.0, 1.0);
    fragColor.g = clamp(uOverrideColor.g + gl_Color.g, 0.0, 1.0);
    fragColor.b = clamp(uOverrideColor.b + gl_Color.b, 0.0, 1.0);
    fragColor.a = texColor.a * gl_Color.a;
    
    return fragColor;
}