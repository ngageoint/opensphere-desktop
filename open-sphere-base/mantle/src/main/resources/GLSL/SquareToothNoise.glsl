// Range 0 to 1
uniform float uBeginVal;

// Range 0 to 1
uniform float uEndVal;

// Range 0 to 1
uniform float uReplaceVal;

vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    vec4 fragColor = vec4(0.0);
    if (((texColor.g > 0.01 || texColor.r > 0.01 || texColor.b > 0.01) && (texColor.g > uBeginVal && texColor.g < uEndVal))
        && ((texColor.r > uBeginVal && texColor.r < uEndVal) || (texColor.b > uBeginVal && texColor.b < uEndVal)))
    {
        fragColor = texColor;
        fragColor.a = uReplaceVal;
        if (uReplaceVal < 0.01)
        {
            discard;
        }
    }     
    else
    {
        // TODO move blend with gl_Color to bottom so that it is always blended.
        fragColor.r = clamp(texColor.r + gl_Color.r, 0.0, 1.0);
        fragColor.g = clamp(texColor.g + gl_Color.g, 0.0, 1.0);
        fragColor.b = clamp(texColor.b + gl_Color.b, 0.0, 1.0);
        fragColor.a = texColor * gl_Color.a;
    }
    
    return fragColor;
}
