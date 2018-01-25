uniform vec4 uBlendColor;

// Blend with the given given color, but leave the alpha the same.
// The alpha of the blend color is used as the mix factor.
vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    if (texColor.a == 0)
    {
        discard;
    }
    
    vec3 mixColor = mix(texColor.rgb, uBlendColor.rgb, uBlendColor.a);
    vec4 fragColor = vec4(mixColor.r, mixColor.g, mixColor.b, texColor.a);
    return fragColor;
}