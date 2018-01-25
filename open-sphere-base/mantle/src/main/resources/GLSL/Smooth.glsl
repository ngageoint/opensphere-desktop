// TODO this method creates color distortion at the tile edges.
// TODO this also assumes the pixel width and height of the image. These should be uniforms.
vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    vec4 fragColor = vec4(0.0);
    
    vec2 offset = gl_TexCoord[0].xy + vec2(0,1/512.0);
    // TODO these values should probably be clamped instead of discarded.
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture, offset);
    }

    offset = gl_TexCoord[0].xy + vec2(1/512.0,0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture, offset);
    }

    offset = gl_TexCoord[0].xy + vec2(0,-1/512.0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture, offset);
    }

    offset = gl_TexCoord[0].xy + vec2(-1/512.0,0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture, offset);
    }
    
    offset = gl_TexCoord[0].xy + vec2(-1/512.0,-1/512.0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture, offset);
    }
    
    offset = gl_TexCoord[0].xy + vec2(-1/512.0,1/512.0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture, offset);
    }
    
    offset = gl_TexCoord[0].xy + vec2(1/512.0,1/512.0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture, offset);
    }
    
    offset = gl_TexCoord[0].xy + vec2(1/512.0,-1/512.0);
    if(offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
    {
        fragColor = fragColor + texture2D(inTexture,  offset);
    }
    
    fragColor = (fragColor + texColor) / 9.0;
    
    // TODO blend with gl_Color
    return fragColor;
}