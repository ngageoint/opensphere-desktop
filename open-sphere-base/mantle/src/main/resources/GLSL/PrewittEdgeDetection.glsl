vec4 getFragColor(sampler2D inTexture)
{
    vec4 fragColor = vec4(0.0);
    
    float jumpVal = 1/512.0;

    vec2 offset0 = clamp(gl_TexCoord[0].xy + vec2(-jumpVal,-jumpVal),0.0,1.0);
    vec2 offset1 = clamp(gl_TexCoord[0].xy + vec2(-jumpVal,0),0.0,1.0);
    vec2 offset2 = clamp(gl_TexCoord[0].xy + vec2(-jumpVal,+jumpVal),0.0,1.0);
    vec2 offset3 = clamp(gl_TexCoord[0].xy + vec2(0,-jumpVal),0.0,1.0);
    vec2 offset5 = clamp(gl_TexCoord[0].xy + vec2(0,jumpVal),0.0,1.0);
    vec2 offset6 = clamp(gl_TexCoord[0].xy + vec2(jumpVal,-jumpVal),0.0,1.0);
    vec2 offset7 = clamp(gl_TexCoord[0].xy + vec2(jumpVal,0),0.0,1.0);
    vec2 offset8 = clamp(gl_TexCoord[0].xy + vec2(jumpVal,jumpVal),0.0,1.0);

    vec4 value0 = texture2D(inTexture,  offset0);
    vec4 value1 = texture2D(inTexture,  offset1);
    vec4 value2 = texture2D(inTexture,  offset2);
    vec4 value3 = texture2D(inTexture,  offset3);
    vec4 value5 = texture2D(inTexture,  offset5);
    vec4 value6 = texture2D(inTexture,  offset6);
    vec4 value7 = texture2D(inTexture,  offset7);
    vec4 value8 = texture2D(inTexture,  offset8);

    vec4 horizEdge = value2 + value5 + value8 - (value0 + value3 + value6);
    vec4 vertEdge = value0 + value1 + value2 - (value6 + value7 + value8);

    fragColor.rgb = sqrt((horizEdge.rgb * horizEdge.rgb) + (vertEdge.rgb * vertEdge.rgb));
    fragColor.a = step(0.1, fragColor.r + fragColor.g + fragColor.b);
    
    // TODO blend with gl_Color
    return fragColor;
}