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
        vec4 fragColor = gl_Color;
        fragColor.a = 1.0;
        gl_FragColor = fragColor;
    }
}
