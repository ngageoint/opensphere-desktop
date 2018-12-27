// Blend with the given given color, but leave the alpha the same.
// The alpha of the blend color is used as the mix factor.
vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    vec4 fragColor = vec4(0.0);
    
    fragColor.r = clamp(1.0 - texColor.r, 0.0, 1.0);
    fragColor.g = clamp(1.0 - texColor.g, 0.0, 1.0);
    fragColor.b = clamp(1.0 - texColor.b, 0.0, 1.0);
    fragColor.a = texColor.a;
    
    return fragColor;
}