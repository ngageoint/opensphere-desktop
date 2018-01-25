// If the fragment is visible, make it fully black and fully opaque.
vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    if (texColor.a == 0)
    {
        discard;
    }
    
    return vec4(0.0, 0.0, 0.0, 1.0);
}