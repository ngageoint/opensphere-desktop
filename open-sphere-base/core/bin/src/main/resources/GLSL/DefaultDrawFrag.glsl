vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    vec4 fragColor = vec4(0.0);

    fragColor.r = clamp(texColor.r + gl_Color.r, 0.0, 1.0);
    fragColor.g = clamp(texColor.g + gl_Color.g, 0.0, 1.0);
    fragColor.b = clamp(texColor.b + gl_Color.b, 0.0, 1.0);
    fragColor.a = texColor.a * gl_Color.a;

    return fragColor;
}
