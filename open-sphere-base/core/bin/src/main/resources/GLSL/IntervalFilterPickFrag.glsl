uniform sampler2D texture;

// Discard fragments that are transparent.
void main(void)
{
   // Branching can be slow in shaders, but this case seems to perform okay.
   if (gl_Color.a == 0)
   {
      discard;
   }
   else
   {
      vec4 tileColor = texture2D(texture, gl_TexCoord[0].xy);

      vec4 fragColor = gl_Color;
      fragColor.a = sign(tileColor.a);
      gl_FragColor = fragColor;
   }
}
