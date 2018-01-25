uniform vec4 activeInterval;
attribute vec2 vertexInterval;

// Fade vertices if they are outside of the input activeInterval.
void main(void)
{
   gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
   gl_FrontColor.rgb = gl_Color.rgb;

   // Smoothstep behavior is undefined if the edges are equal, so branches are necessary here.
   
   float preFade;
   if (activeInterval.y > activeInterval.x)
   {
      preFade = smoothstep(activeInterval.x, activeInterval.y, vertexInterval.y);
   }
   else
   {
      preFade = step(activeInterval.y, vertexInterval.y);
   }
   

   float postFade;
   if (activeInterval.w > activeInterval.z)
   {
      postFade = smoothstep(-activeInterval.w, -activeInterval.z, -vertexInterval.x);
   }
   else
   {
      postFade = step(-activeInterval.z, -vertexInterval.x);
   }

   gl_FrontColor.a = gl_Color.a * preFade * postFade;
}
