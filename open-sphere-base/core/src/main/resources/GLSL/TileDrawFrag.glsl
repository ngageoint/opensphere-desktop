uniform sampler2D texture;

// MinX, MaxX, MinY, MaxY
uniform vec4 textureCoordLimits;

vec4 getFragColor(sampler2D);

void main(void)
{
    if (gl_TexCoord[0].x < textureCoordLimits.x || gl_TexCoord[0].x > textureCoordLimits.y
        || gl_TexCoord[0].y < textureCoordLimits.z || gl_TexCoord[0].y > textureCoordLimits.w)
    {
        discard;
    }
    else
    {
        gl_FragColor = getFragColor(texture);
    }
}
