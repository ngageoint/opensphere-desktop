#if __VERSION__ < 130
#extension GL_EXT_gpu_shader4 : enable
#endif

uniform vec4 uOutlineColor;

// Surround the colored pixels with a border. This assumes that the colored
// pixels are 95 percent or more opaque.
vec4 getFragColor(sampler2D inSampler)
{
    vec4 fragColor = vec4(0.0);
    
    // Get the texture size so that we can get sample
    // the pixels next to the one being drawn.
#if __VERSION__ >= 130
    ivec2 size = textureSize(inSampler, 0);
#else
    ivec2 size = textureSize2D(inSampler, 0);
#endif
    vec2 pixelOffset = 1.0 / vec2(size);

    vec2 offset0 = clamp(gl_TexCoord[0].xy + vec2(-pixelOffset.x,-pixelOffset.y), 0.0, 1.0);
    vec2 offset1 = clamp(gl_TexCoord[0].xy + vec2(-pixelOffset.x,0), 0.0, 1.0);
    vec2 offset2 = clamp(gl_TexCoord[0].xy + vec2(-pixelOffset.x,pixelOffset.y), 0.0, 1.0);
    vec2 offset3 = clamp(gl_TexCoord[0].xy + vec2(0,pixelOffset.y), 0.0, 1.0);
    vec2 offset4 = clamp(gl_TexCoord[0].xy + vec2(pixelOffset.x,pixelOffset.y), 0.0, 1.0);
    vec2 offset5 = clamp(gl_TexCoord[0].xy + vec2(pixelOffset.x,0), 0.0, 1.0);
    vec2 offset6 = clamp(gl_TexCoord[0].xy + vec2(pixelOffset.x,-pixelOffset.y), 0.0, 1.0);
    vec2 offset7 = clamp(gl_TexCoord[0].xy + vec2(0,pixelOffset.y), 0.0, 1.0);

    vec4 value0 = texture2D(inSampler,  offset0);
    vec4 value1 = texture2D(inSampler,  offset1);
    vec4 value2 = texture2D(inSampler,  offset2);
    vec4 value3 = texture2D(inSampler,  offset3);
    vec4 value4 = texture2D(inSampler,  offset4);
    vec4 value5 = texture2D(inSampler,  offset5);
    vec4 value6 = texture2D(inSampler,  offset6);
    vec4 value7 = texture2D(inSampler,  offset7);
    
    // If the texel is mostly opaque, then it is considered to be part of the text.
    float step0 = step(0.95, value0.a);
    float step1 = step(0.95, value1.a);
    float step2 = step(0.95, value2.a);
    float step3 = step(0.95, value3.a);
    float step4 = step(0.95, value4.a);
    float step5 = step(0.95, value5.a);
    float step6 = step(0.95, value6.a);
    float step7 = step(0.95, value7.a);
    
    float sum = step0 + step1 + step2 + step3 + step4 + step5 + step6 + step7;
    float adjacent = clamp(sum, 0.0, 1.0);
    
    vec4 valCenter = texture2D(inSampler,  gl_TexCoord[0].xy);
    
    // If the current texel is mostly opaque, then render the regular text color.
    vec4 foreColor = gl_Color * step(0.95, valCenter.a);
    
    // If the texel is not opaque, then render in the
    // outline color if it is next to text.
    vec4 backColor = (1.0 - step(0.95, valCenter.a)) * uOutlineColor * adjacent;
    
    return foreColor + backColor;
}