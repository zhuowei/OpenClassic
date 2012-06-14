uniform mat4 g_WorldViewProjectionMatrix;
 
attribute vec4 inPosition;
attribute vec2 inTexCoord;
varying vec2 texCoord;
 
uniform float m_FXAA_SUBPIX_SHIFT;
uniform float m_rt_w;
uniform float m_rt_h;
varying vec4 posPos;
 
void main() {
    gl_Position = inPosition * 2.0 - 1.0; //vec4(pos, 0.0, 1.0);
    texCoord = inTexCoord;
 
    vec2 rcpFrame = vec2(1.0/m_rt_w, 1.0/m_rt_h);
    posPos.xy = inTexCoord.xy;
    posPos.zw = inTexCoord.xy -
                  (rcpFrame * (0.5 + m_FXAA_SUBPIX_SHIFT));
}
