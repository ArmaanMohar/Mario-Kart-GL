#version 120

// ADS Point Lighting Shader (Phong)

uniform mat4 model_matrix;

varying vec4 shadowCoord;
varying vec3 vertexNormal;
varying vec3 lightDir[8];
varying vec3 eyeDir;

void main(void) 
{ 
	vec4 vertex = gl_ModelViewMatrix * gl_Vertex;
	
	gl_ClipVertex = vertex;
    gl_Position   = ftransform();
	
    // Get surface normal in eye coordinates
    vertexNormal = normalize(gl_NormalMatrix * gl_Normal);

    // Get vertex position in eye coordinates
    vec3 position = (vertex / vertex.w).xyz;
    
    eyeDir = -vertex.xyz;

    // Get vector to light source
    for(int i = 0; i < 8; i++) lightDir[i] = gl_LightSource[i].position.xyz - position;
	
	gl_FrontColor = gl_Color;
	shadowCoord = gl_TextureMatrix[6] * (model_matrix * gl_Vertex);
}
