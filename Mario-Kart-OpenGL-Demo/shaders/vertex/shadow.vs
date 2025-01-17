// ADS Point Lighting
// Phong Shading
// Single Texture Map
// Shadow Mapping

uniform mat4 model_matrix;

varying vec4 shadowCoord;
varying vec3 vNormal;
varying vec3 lightDir;
varying vec3 eyeDir;

void main(void) 
{ 
	gl_ClipVertex = gl_ModelViewMatrix * gl_Vertex;
    gl_Position = ftransform();
	
    // Get surface normal in eye coordinates
    vNormal = gl_NormalMatrix * gl_Normal;
	vNormal = normalize(vNormal);

    // Get vertex position in eye coordinates
    vec4 position4 = gl_ModelViewMatrix * gl_Vertex;
    vec3 position3 = vec3(position4) / position4.w;
    
    eyeDir = vec3(gl_ModelViewMatrix * gl_Vertex);

    // Get vector to light source
    lightDir = gl_LightSource[0].position.xyz - position3;
	
	gl_FrontColor = gl_Color;
	
	shadowCoord = gl_TextureMatrix[6] * (model_matrix * gl_Vertex);
	gl_TexCoord[0] = gl_MultiTexCoord0;
}