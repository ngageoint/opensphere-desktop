// maybe 0 to 2? The app seems to become unsusable with larger numbers. A value of 1 seems to give an acceptable affect.
uniform float uBloom;
// The rgb components of the highlight color
uniform vec3 uColor;

// TODO this shader assumes a pixel width and height of 512 for the texture. These should be uniforms.
// TODO because this shader uses loops, it is extremely slow. Is there a better way to do this?
vec4 getFragColor(sampler2D inTexture)
{
    vec4 texColor = texture2D(inTexture, gl_TexCoord[0].xy);
    vec4 fragColor = vec4(0.0);

    if (texColor.g > 0.01 || texColor.r > 0.01 || texColor.b > 0.01)
    {
        fragColor = texColor;
        fragColor.a = 1.0;
    }
    else
    {
        float smallsize = 1.0/512.0;
        float halfBloom = (uBloom  / 2.0) * smallsize + 1/1024.0;
        float beginval = -halfBloom;

        float smallsizeb = 1.0/512.0;
        float halfBloomb = (uBloom  / 2.0) * smallsizeb + 1/1024.0;
        float beginvalb = -halfBloomb;
        vec4 saveColor = vec4(0);

        for (float i = beginval; i<= halfBloom; i+=smallsize)
        {
            for (float j = beginvalb; j<= halfBloomb; j+=smallsizeb)
            {
                vec2 offset = gl_TexCoord[0].xy + vec2(i,j);
                vec4 tmp = texture2D(inTexture,  offset);

                if (offset.x >= 0 && offset.x <= 1 && offset.y >= 0 && offset.y <=1)
                {
                    if (tmp.g > saveColor.g)
                    {
                        saveColor.g = tmp.g;
                        fragColor = vec4(uColor.r, uColor.g, uColor.b, 1);
                    }
                    else if (tmp.r > saveColor.r)
                    {
                        saveColor.r = tmp.r;
                        fragColor = vec4(uColor.r, uColor.g, uColor.b, 1);
                    }
                    else if (tmp.b > saveColor.b)
                    {
                        saveColor.b = tmp.b;
                        fragColor = vec4(uColor.r, uColor.g, uColor.b, 1);
                    }
                    else if (tmp.a > saveColor.a)
                    {
                        saveColor.a = tmp.a;
                        fragColor = vec4(uColor.r, uColor.g, uColor.b, 1);
                    }
                }
            }
        }
    }

    // TODO blend with gl_Color
    return fragColor;
}
