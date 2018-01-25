vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    vec4 fragColor = vec4(0.0);
        
    // TODO this needs to be scaled for magnified images
    vec2 offset = gl_TexCoord[0].xy + vec2(0, 1/512.0);
    if (offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture,  offset);
    }

    offset = gl_TexCoord[0].xy + vec2(1/512.0, 0);
    if( offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture,  offset);
    }

    offset = gl_TexCoord[0].xy + vec2(0, -1/512.0);
    if (offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture,  offset);
    }

    offset = gl_TexCoord[0].xy + vec2(-1/512.0, 0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture,  offset);
    }
    
    // TODO clamp the color and blend with gl_Color
    fragColor = fragColor - (4 * texColor);
    fragColor.a = step(0.1, fragColor.r + fragColor.g + fragColor.b);

    return fragColor;
}