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
        gl_FragColor = gl_Color;
        gl_FragColor.a = 1.0;
    }
}
