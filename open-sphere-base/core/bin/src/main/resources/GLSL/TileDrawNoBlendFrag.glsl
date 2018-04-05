uniform sampler2D texture[2];

// MinX, MaxX, MinY, MaxY
uniform vec4 textureCoordLimits;

void main(void)
{
    if (gl_TexCoord[0].x < textureCoordLimits.x || gl_TexCoord[0].x > textureCoordLimits.y
        || gl_TexCoord[0].y < textureCoordLimits.z || gl_TexCoord[0].y > textureCoordLimits.w)
    {
        discard;
    }
    else
    {
        vec4 tileColor = texture2D(texture[0], gl_TexCoord[0].xy);
        tileColor.r = clamp(tileColor.r + gl_Color.r, 0.0, 1.0);
        tileColor.g = clamp(tileColor.g + gl_Color.g, 0.0, 1.0);
        tileColor.b = clamp(tileColor.b + gl_Color.b, 0.0, 1.0);
        tileColor.a *= gl_Color.a;

        vec4 background = texture2D(texture[1], gl_TexCoord[0].xy);

        // TODO this could be adjusted to use a modified version of the active blending function.
        // How do we find out what blending is active?
        float hasAlpha = step(background.a, 0.001);
        float topAlphaAjust = clamp(hasAlpha + tileColor.a, 0.0, 1.0);
        gl_FragColor =  tileColor * topAlphaAjust + (background * (1.0 - topAlphaAjust));
    }
}
