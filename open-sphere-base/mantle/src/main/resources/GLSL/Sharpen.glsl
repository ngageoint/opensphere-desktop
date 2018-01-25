// Scale factor range 0 to 1
uniform float uScaleFactor;

// TODO this method causes some color distortion on tile boundaries
vec4 getFragColor(sampler2D inTexture)
{
    vec4 fragColor = vec4(0.0);
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    
    fragColor = fragColor + (4 * texColor);
    
    vec2 offset = gl_TexCoord[0].xy + vec2(0,1/512.0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor - texture2D(inTexture,  offset);
    }

    offset = gl_TexCoord[0].xy + vec2(1/512.0,0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor - texture2D(inTexture,  offset);
    }

    offset = gl_TexCoord[0].xy + vec2(0,-1/512.0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor - texture2D(inTexture,  offset);
    }

    offset = gl_TexCoord[0].xy + vec2(-1/512.0,0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor - texture2D(inTexture,  offset);
    }

    fragColor = uScaleFactor * fragColor + texColor;
    
    // TODO blend with gl_Color
    return fragColor;
}