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
      gl_FragColor = texture2D(texture, gl_TexCoord[0].xy);
      gl_FragColor.a = gl_FragColor.a * gl_Color.a;
   }
}
