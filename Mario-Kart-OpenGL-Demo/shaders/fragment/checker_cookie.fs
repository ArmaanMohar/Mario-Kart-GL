#extension GL_EXT_gpu_shader4 : enable

uniform sampler2D texture;
uniform sampler2D bumpmap;
uniform sampler2D heightmap;
uniform sampler2D specular_map;

uniform bool enable_spec_map;
uniform bool enableParallax;

uniform sampler2DShadow shadowMap;

varying vec3 lightDir[8];
varying vec3 eyeDir;
varying vec4 shadowCoord;
varying vec3 cameraVec;
varying vec3 vertex_position;

uniform vec3 primary_color;
uniform vec3 secondary_color;

uniform bool enableShadow;
uniform int sampleMode;

const float epsilon = 0.05;
float illumination = 0.5;

uniform vec2 texScale;

float lookup(float x, float y)
{
	vec2 offset = vec2(mod(floor(gl_FragCoord.xy), 2.0));
	x += offset.x;
	y += offset.y;

	float depth = shadow2DProj(shadowMap, shadowCoord + vec4(x, y, 0, 0) * epsilon).x;
	return depth != 1.0 ? illumination : 1.0;
}

float lookup(sampler2DShadow map, vec4 coord, vec2 offset)
{
	float depth = shadow2DProj(shadowMap, vec4(coord.xy + offset * texScale * coord.w, coord.z, coord.w)).x;
	return depth != 1.0 ? illumination : 1.0;
}

float shadowIntensity()
{
	if(sampleMode == 0)
	{	
		return lookup(0.0, 0.0);
	}
	else if(sampleMode == 1)
	{
		float sum = 0.0;
		
		sum += lookup(-1.0, -1.0);
		sum += lookup(+1.0, -1.0);
		sum += lookup(-1.0, +1.0);
		sum += lookup(+1.0, +1.0);
			
		return sum * 0.25;
	}
	else if(sampleMode == 2)
	{
		float sum = 0.0;
		float x, y;

		for (y = -1.5; y <= 1.5; y += 1.0)
  			for (x = -1.5; x <= 1.5; x += 1.0)
    			sum += lookup(shadowMap, shadowCoord, vec2(x, y));
    				
    	return sum / 16.0;
	}	
}

void pointLight(in int i, in vec3 normal, inout vec4 ambient, inout vec4 diffuse, inout vec4 specular)
{
	float distanceToLight, attenuation;
    
    distanceToLight  = length(lightDir[i]);
    vec3 lightVec = normalize(lightDir[i]);
    
    attenuation = 1.0 / (gl_LightSource[i].constantAttenuation  +
             		     gl_LightSource[i].linearAttenuation    * distanceToLight +
             			 gl_LightSource[i].quadraticAttenuation * distanceToLight * distanceToLight);
    
     // Add in ambient light
    ambient += gl_LightSource[i].ambient * attenuation;

    // Diffuse Lighting
    float diffuseCoefficient = max(0.0, dot(normal, lightVec));
    diffuse += diffuseCoefficient * gl_LightSource[i].diffuse * attenuation;

    // Specular Lighting
    if(diffuseCoefficient > 0.0)
	{
		vec3 lightReflection = reflect(-lightVec, normal);
		
    	float specularCoefficient = max(0.0, dot(normalize(-eyeDir), lightReflection));
		specularCoefficient = pow(specularCoefficient, gl_FrontMaterial.shininess);
		
        specular += specularCoefficient * gl_LightSource[i].specular * attenuation;
    }
}

void main(void)
{
	vec2 texCoord = gl_TexCoord[0].st;
	
	if(enableParallax)
	{
		float height, scale = 0.04, bias = 0.02;
		height = scale * (texture2D(heightmap, texCoord).r) - bias;
		texCoord += height * normalize(cameraVec).xy;
	}
	
    vec3 normal = texture2D(bumpmap, texCoord).rgb;
    normal *= 2.0; normal -= 1.0; // map texel from [0,1] to [-1,1]
    normal.rg *= 0.5;
             						        
    float shadowCoefficient = enableShadow ? shadowIntensity() : 1.0;
    
    float mix_factor = 0.0;
    
    float pixWidth = 0.002;
	float boxWidth = 5.0;
	
	float x, z;
	
	for (z = -pixWidth * boxWidth; z <= pixWidth * boxWidth; z += pixWidth)
  			for (x = -pixWidth * boxWidth; x <= pixWidth * boxWidth; x += pixWidth)
    			mix_factor += (int(abs(vertex_position.x + x)) % 2 == int(abs(vertex_position.z + z)) % 2) ? 1.0 : 0.0;
    			
    mix_factor /= pow(2.0 * boxWidth + 1.0, 2.0);
          						         
    vec4 textureColor = vec4(mix(primary_color, secondary_color, mix_factor), 1.0);
    vec4 ambient, diffuse, specular;
    ambient  = vec4(0.0);
    diffuse  = vec4(0.0);
    specular = vec4(0.0);
    
    normal = normalize(normal);
    
    for(int i = 0; i < 8; i++)
    {
    	pointLight(i, normal, ambient, diffuse, specular);
    }
    
    float specular_intensity = enable_spec_map ? texture2D(specular_map, texCoord).r : 1.0;
             						         
	vec4 linearColor = shadowCoefficient * (gl_Color * textureColor * (ambient + diffuse) + specular * specular_intensity);
	
	gl_FragData[0] = vec4(linearColor.rgb, 1.0);
	
	vec3 brightColor = max(linearColor.rgb - vec3(1.0), vec3(0.0));
	
    float bright = dot(brightColor, vec3(1.0));
    bright = smoothstep(0.0, 0.5, bright);
    
    gl_FragData[1] = vec4(mix(vec3(0.0), linearColor.rgb, bright), 1.0);
    gl_FragData[2] = vec4(normal, -eyeDir.z);
}