uniform sampler2D texture;

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
        vec4 tileColor = texture2D(texture, gl_TexCoord[0].xy);
        tileColor.r = clamp(tileColor.r + gl_Color.r, 0.0, 1.0);
        tileColor.g = clamp(tileColor.g + gl_Color.g, 0.0, 1.0);
        tileColor.b = clamp(tileColor.b + gl_Color.b, 0.0, 1.0);
        tileColor.a *= gl_Color.a;

        float xBorderRange = (textureCoordLimits.y - textureCoordLimits.x) * 0.005;
        float bxMin = textureCoordLimits.x + xBorderRange;
        float bxMax = textureCoordLimits.y - xBorderRange;

        float yBorderRange = (textureCoordLimits.w - textureCoordLimits.z) * 0.005;
        float byMin = textureCoordLimits.z + yBorderRange;
        float byMax = textureCoordLimits.w - yBorderRange;

        float borderRangeX = step(-bxMin, -gl_TexCoord[0].x) + step(bxMax, gl_TexCoord[0].x);
        float borderRangeY = step(-byMin, -gl_TexCoord[0].y) + step(byMax, gl_TexCoord[0].y);
        float inBorder = clamp(borderRangeX + borderRangeY, 0.0, 1.0);

        gl_FragColor = tileColor * (1.0 - inBorder) + vec4(1, 0, 1, 1) * inBorder;
    }
}
