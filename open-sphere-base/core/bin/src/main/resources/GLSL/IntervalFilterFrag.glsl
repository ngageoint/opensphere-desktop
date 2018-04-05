// Discard fragments that are transparent.
void main(void)
{
   // Branching can be slow in shaders, but this case seems to perform okay.
   if (gl_Color.a == 0.0)
   {
      discard;
   }
   else
   {
      gl_FragColor = gl_Color;
   }
}
