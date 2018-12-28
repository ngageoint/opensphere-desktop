uniform bool uInvertRed;
uniform bool uInvertGreen;
uniform bool uInvertBlue;

// Blend with the given given color, but leave the alpha the same.
// The alpha of the blend color is used as the mix factor.
vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    vec4 fragColor = vec4(0.0);
   
    if(uInvertRed) 
    { 
	    fragColor.r = clamp(1.0 - texColor.r, 0.0, 1.0);
    } 
    else 
    {
    	fragColor.r = texColor.r;
    }
    if(uInvertGreen) 
    { 
    	fragColor.g = clamp(1.0 - texColor.g, 0.0, 1.0);
    }
    else 
    { 
    	fragColor.g = texColor.g;
    }
    if(uInvertBlue) 
    {
    	fragColor.b = clamp(1.0 - texColor.b, 0.0, 1.0);
    }
    else 
    {
    	fragColor.b = texColor.b;
    }
    fragColor.a = texColor.a;
    
    return fragColor;
}