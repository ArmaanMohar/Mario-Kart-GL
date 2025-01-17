package bates.jamie.graphics.util.shader;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.Renderer;
import bates.jamie.graphics.util.Vec3;

import com.jogamp.opengl.util.glsl.ShaderUtil;

public class Shader
{
	public static boolean enableSimple = false;
//	public static boolean enabled = true; 
	
	public static Map<String, Shader> shaders = new HashMap<String, Shader>();
	
	public List<Uniform> uniforms = new ArrayList<Uniform>();
	
	public int shaderID;
	 
	public String[] vertSource;
	public String[] fragSource;
	
	public String vertString;
	public String fragString;
	
	public int vertProgram;
	public int fragProgram;
	
	private boolean valid = false;
	
	public Shader(GL2 gl, String vShader, String fShader)
	{	
		valid = attachPrograms(gl, vShader, fShader, null);
	}
	
	public Shader(GL2 gl, String shader)
	{	
		valid = attachPrograms(gl, shader, shader, null);
	}
	
	public Shader(GL2 gl, String vShader, String fShader, HashMap<Integer, String> attributes)
	{	
		valid = attachPrograms(gl, vShader, fShader, attributes);
	}
	
	public static void loadShaders(GL2 gl)
	{
		HashMap<Integer, String> bump_attr = new HashMap<Integer, String>();
		bump_attr.put(1, "tangent");
		
		HashMap<Integer, String> bump_inst_attr = new HashMap<Integer, String>();
		bump_inst_attr.put(1, "tangent");
		bump_inst_attr.put(4, "instance_data");
		
		HashMap<Integer, String> inst_attr = new HashMap<Integer, String>();
		inst_attr.put(4, "instance_data");
		
		HashMap<Integer, String> bump_mat_inst_attr = new HashMap<Integer, String>();
		bump_mat_inst_attr.put( 1, "tangent");
		bump_mat_inst_attr.put( 4, "instance_data");
		bump_mat_inst_attr.put(11, "instance_matrix");
		
		HashMap<Integer, String> mat_inst_attr = new HashMap<Integer, String>();
		mat_inst_attr.put( 4, "instance_data");
		mat_inst_attr.put(11, "instance_matrix");
		
		// load and compile shaders from file
		Shader simple         = new Shader(gl, "simple");
		Shader simpleAlpha    = new Shader(gl, "simple_alpha");
		Shader simpleInstance = new Shader(gl, "simple_instance", "simple", inst_attr);
		Shader simpleMatInst  = new Shader(gl, "simple_mat_inst", "simple", mat_inst_attr);
		
		Shader phong         = new Shader(gl, "phong");
		Shader phongLights   = new Shader(gl, "phong_lights");
		Shader phongInstance = new Shader(gl, "phong_instance", "phong_lights", inst_attr);
		Shader phongMatInst  = new Shader(gl, "phong_mat_inst", "phong_lights", mat_inst_attr);
		Shader phongRim      = new Shader(gl, "phong_lights", "phong_rim");
		Shader phongTexture  = new Shader(gl, "phong_texture");
		Shader phongAlpha    = new Shader(gl, "phong_texture", "phong_alpha");
		
		Shader bloomColor    = new Shader(gl, "bloom_color");
		
		Shader texLights      = new Shader(gl, "texture_lights");
		Shader textureRim     = new Shader(gl, "texture_lights", "texture_rim");
		Shader textureSpec    = new Shader(gl, "shadow_lights", "texture_specular");
		
		Shader checkerDiag    = new Shader(gl, "checker_diagonal");
		Shader checkerSlope   = new Shader(gl, "checker_slope");
		Shader checkerReflect = new Shader(gl, "checker_slope", "checker_reflect");
		Shader checkerShadow  = new Shader(gl, "checker_shadow");
		Shader checkerBlock   = new Shader(gl, "checker_shadow", "checker_block");
		Shader checkerInst    = new Shader(gl, "checker_instance", "checker_block", mat_inst_attr);
		Shader slopeInstance  = new Shader(gl, "checker_instance", "checker_shadow", mat_inst_attr);
		
		Shader checkerCookie = new Shader(gl, "checker_cookie", "checker_cookie", bump_attr);
		Shader cookieMatInst = new Shader(gl, "cookie_instance", "checker_cookie", bump_mat_inst_attr);
		
		Shader bump          = new Shader(gl, "bump", "bump", bump_attr);
		Shader bumpPhong     = new Shader(gl, "parallax_lights", "bump_phong", bump_attr);
		Shader bumpLights    = new Shader(gl, "bump_lights", "bump_lights", bump_attr);
		Shader parallax      = new Shader(gl, "parallax_lights", "parallax_lights", bump_attr);
		Shader parallaxSpec  = new Shader(gl, "parallax_lights", "parallax_specular", bump_attr);
		Shader bumpReflect   = new Shader(gl, "bump_reflect", "bump_reflect", bump_attr);
		Shader bumpCube      = new Shader(gl, "bump_cube", "bump_cube", bump_attr);
		Shader bumpRain      = new Shader(gl, "bump_cube", "bump_rain", bump_attr);
		Shader bumpAlpha     = new Shader(gl, "bump_lights", "bump_alpha", bump_attr);
		Shader bumpInstance  = new Shader(gl, "bump_instance", "parallax_lights", bump_inst_attr);
		Shader bumpMatInst   = new Shader(gl, "bump_mat_inst", "bump_mat_inst", bump_mat_inst_attr);
		
		Shader heightMap     = new Shader(gl, "height_map");
		
		Shader shadow        = new Shader(gl, "shadow");
		Shader phongShadow   = new Shader(gl, "phong_shadow");
		Shader shadowLights  = new Shader(gl, "shadow_lights");
		Shader phongMix      = new Shader(gl, "shadow_lights", "phong_mix"); 
		
		Shader phongCube    = new Shader(gl, "phong_cube");
		Shader cubeLights   = new Shader(gl, "cube_lights");
		Shader cubeRim      = new Shader(gl, "cube_lights", "cube_rim");
		
		Shader aberration   = new Shader(gl, "aberration");
		Shader ghost        = new Shader(gl, "ghost");
		Shader ghostRim     = new Shader(gl, "phong", "ghost_rim");
		Shader invisible    = new Shader(gl, "ghost", "invisible");
		Shader starPower    = new Shader(gl, "phong_cube", "star_cube");
		
		Shader itemBox      = new Shader(gl, "item_box");
		Shader fakeBox      = new Shader(gl, "item_box", "fake_box");
		
		Shader water        = new Shader(gl, "water", "water", bump_attr);
		Shader magma        = new Shader(gl, "water", "magma", bump_attr);
		Shader caustics     = new Shader(gl, "water_caustics", "water_caustics", bump_attr);
		Shader bumpCaustics = new Shader(gl, "bump_caustics", "bump_caustics", bump_attr);
		
		Shader clearSky     = new Shader(gl, "clear_sky");
		Shader cloudySky    = new Shader(gl, "cloudy_sky");
		Shader grass        = new Shader(gl, "grass");
		Shader dissolve     = new Shader(gl, "dissolve");
		Shader energyField  = new Shader(gl, "energy_field");
		
		Shader gaussian     = new Shader(gl, "show_texture", "gaussian");
		Shader depthField   = new Shader(gl, "show_texture", "depth_field");
		Shader mirage       = new Shader(gl, "show_texture", "rain_distort");
		Shader combine      = new Shader(gl, "show_texture", "combine");
		Shader showTexture  = new Shader(gl, "show_texture", "height_normal");
		Shader ambientOcc   = new Shader(gl, "show_texture", "ssao"); // screen-space ambient occlusion
		Shader rainyScene   = new Shader(gl, "show_texture", "rainy_scene");
		Shader crepuscular  = new Shader(gl, "crepuscular");
		Shader radialBlur   = new Shader(gl, "radial_blur");
		Shader smoke        = new Shader(gl, "smoke");
		Shader fire         = new Shader(gl, "fire");
		
		Shader rainDrop     = new Shader(gl, "rain_drop");
		
		Shader pulsate      = new Shader(gl, "pulsate", "phong_lights");
		
		Shader wood         = new Shader(gl, "wood");
		
		// check that shaders have been compiled and linked correctly before hashing 
		if(        simple.isValid()) shaders.put("simple", simple);
		if(   simpleAlpha.isValid()) shaders.put("simple_alpha", simpleAlpha);
		if(simpleInstance.isValid()) shaders.put("simple_instance", simpleInstance);
		if( simpleMatInst.isValid()) shaders.put("simple_mat_inst", simpleMatInst);
		
		if(        phong.isValid()) shaders.put("phong", phong);
		if(  phongLights.isValid()) shaders.put("phong_lights", phongLights);
		if(phongInstance.isValid()) shaders.put("phong_instance", phongInstance);
		if( phongMatInst.isValid()) shaders.put("phong_mat_inst", phongMatInst);
		if(     phongRim.isValid()) shaders.put("phong_rim", phongRim);
		if( phongTexture.isValid()) shaders.put("phong_texture", phongTexture);
		if(   phongAlpha.isValid()) shaders.put("phong_alpha", phongAlpha);
		if(     phongMix.isValid()) shaders.put("phong_mix", phongMix);
		
		if(   bloomColor.isValid()) shaders.put("bloom_color", bloomColor);
		
		if(     texLights.isValid()) shaders.put("texture_lights", texLights);
		if(    textureRim.isValid()) shaders.put("texture_rim", textureRim);
		if(   textureSpec.isValid()) shaders.put("texture_specular", textureSpec);
		
		if(   checkerDiag.isValid()) shaders.put("checker_diagonal", checkerDiag);
		if(  checkerSlope.isValid()) shaders.put("checker_slope", checkerSlope);
		if(checkerReflect.isValid()) shaders.put("checker_reflect", checkerReflect);
		if( checkerShadow.isValid()) shaders.put("checker_shadow", checkerShadow);
		if(  checkerBlock.isValid()) shaders.put("checker_block", checkerBlock);
		if(   checkerInst.isValid()) shaders.put("checker_instance", checkerInst);
		if( slopeInstance.isValid()) shaders.put("slope_instance", slopeInstance);
		
		if( checkerCookie.isValid()) shaders.put("checker_cookie", checkerCookie);
		if( cookieMatInst.isValid()) shaders.put("cookie_instance", cookieMatInst);
		
		if(        bump.isValid()) shaders.put("bump", bump);
		if(   bumpPhong.isValid()) shaders.put("bump_phong", bumpPhong);
		if(  bumpLights.isValid()) shaders.put("bump_lights", bumpLights);
		if(    parallax.isValid()) shaders.put("parallax_lights", parallax);
		if(parallaxSpec.isValid()) shaders.put("parallax_specular", parallaxSpec);
		if( bumpReflect.isValid()) shaders.put("bump_reflect", bumpReflect);
		if(    bumpCube.isValid()) shaders.put("bump_cube", bumpCube);
		if(    bumpRain.isValid()) shaders.put("bump_rain", bumpRain);
		if(   bumpAlpha.isValid()) shaders.put("bump_alpha", bumpAlpha);
		if(bumpInstance.isValid()) shaders.put("bump_instance", bumpInstance);
		if( bumpMatInst.isValid()) shaders.put("bump_mat_inst", bumpMatInst);
		
		if(     itemBox.isValid()) shaders.put("item_box", itemBox);
		if(     fakeBox.isValid()) shaders.put("fake_box", fakeBox);
		
		if(   heightMap.isValid()) shaders.put("height_map", heightMap);
		
		if(      shadow.isValid()) shaders.put("shadow", shadow);
		if( phongShadow.isValid()) shaders.put("phong_shadow", phongShadow);
		if(shadowLights.isValid()) shaders.put("shadow_lights", shadowLights);
		if(   phongCube.isValid()) shaders.put("phong_cube", phongCube);
		if(  cubeLights.isValid()) shaders.put("cube_lights", cubeLights);
		if(     cubeRim.isValid()) shaders.put("cube_rim", cubeRim);
		if(  aberration.isValid()) shaders.put("aberration", aberration);
		if(       ghost.isValid()) shaders.put("ghost", ghost);
		if(   invisible.isValid()) shaders.put("invisible", invisible);
		if(    ghostRim.isValid()) shaders.put("ghost_rim", ghostRim);
		if(   starPower.isValid()) shaders.put("star_power", starPower);
		if(       water.isValid()) shaders.put("water", water);
		if(       magma.isValid()) shaders.put("magma", magma);
		if(    caustics.isValid()) shaders.put("water_caustics", caustics);
		if(bumpCaustics.isValid()) shaders.put("bump_caustics", bumpCaustics);
		if(    clearSky.isValid()) shaders.put("clear_sky", clearSky);
		if(   cloudySky.isValid()) shaders.put("cloudy_sky", cloudySky);
		if(       grass.isValid()) shaders.put("grass", grass);
		if(    dissolve.isValid()) shaders.put("dissolve", dissolve);
		if( energyField.isValid()) shaders.put("energy_field", energyField);
		
		if(    gaussian.isValid()) shaders.put("gaussian", gaussian);
		if(  depthField.isValid()) shaders.put("depth_field", depthField);
		if(      mirage.isValid()) shaders.put("heat_haze", mirage);
		if(     combine.isValid()) shaders.put("combine", combine);
		if( showTexture.isValid()) shaders.put("show_texture", showTexture);
		if(  ambientOcc.isValid()) shaders.put("ssao", ambientOcc);
		if( crepuscular.isValid()) shaders.put("crepuscular", crepuscular);
		if(  rainyScene.isValid()) shaders.put("rainy_scene", rainyScene);
		if(  radialBlur.isValid()) shaders.put("radial_blur", radialBlur);
		if(  	  smoke.isValid()) shaders.put("smoke", smoke);
		if(  	   fire.isValid()) shaders.put("fire", fire);
		
		if(    rainDrop.isValid()) shaders.put("rain_drop", rainDrop);
		if(     pulsate.isValid()) shaders.put("pulsate", pulsate);
		if(        wood.isValid()) shaders.put("wood", wood);
		
		for(Shader shader : shaders.values()) shader.mapUniforms();
	}
	
	private void mapUniforms()
	{	
		parseUniforms(vertSource[0]);
		parseUniforms(fragSource[0]);
	}

	private void parseUniforms(String source)
	{
		Scanner scanner = new Scanner(source);
		
		while(scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			if(line.startsWith("uniform"))
			{
				String[] tokens = line.trim().split("\\s++");
				
				String identifier = tokens[2].replace(";", "").replaceAll("\\[\\d+\\]", "");
				
				uniforms.add(Uniform.getUniform(tokens[1], identifier));
			}
		}
		scanner.close();
	}
	
	public static Shader get(String name)
	{
		if(enableSimple)
		{
			if(Renderer.instanced_mode)
			{
				if(Renderer.instanced_matrix_mode) return shaders.get("simple_mat_inst");
				else return shaders.get("simple_instance");
			}
			else return shaders.get("simple");
		}
		else return shaders.get(name);
	}
	
	public static Shader getDirect(String name) { return shaders.get(name); }
	
	public static Shader getLightModel(String name)
	{
		Scene scene = Scene.singleton;
		
		if(name.equalsIgnoreCase("phong"))
		{
			if(scene.singleLight) return get("phong");
			else if(scene.rimLighting) return get("phong_rim");
			else return get("phong_lights");
		}
		else if(name.equalsIgnoreCase("texture"))
		{
			if(scene.singleLight) return get("phong_texture");
			else if(scene.rimLighting) return get("texture_rim");
			else return get("texture_lights");
		}
		else if(name.equalsIgnoreCase("cube"))
		{
			if(scene.singleLight) return get("phong_cube");
			else if(scene.rimLighting) return get("cube_rim");
			else return get("cube_lights");
		}
		else if(name.equalsIgnoreCase("shadow"))
		{
			if(scene.singleLight) return get("phong_shadow");
			else return get("shadow_lights");
		}
		
		return get("phong");
	}
	
	public boolean isValid() { return valid; }

	public boolean attachPrograms(GL2 gl, String vShader, String fShader, HashMap<Integer, String> attributes)
	{
		vertString = vShader;
		fragString = fShader;
		
		vertProgram = gl.glCreateShader(GL2.GL_VERTEX_SHADER  );
		fragProgram = gl.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		
		vertSource = parseSource("vertex/" + vShader + ".vs");
		if(vertSource == null) return false;
	
		gl.glShaderSource (vertProgram, 1, vertSource, null, 0);
		gl.glCompileShader(vertProgram);
	
		fragSource = parseSource("fragment/" + fShader + ".fs");
		if(fragSource == null) return false;
		
		gl.glShaderSource (fragProgram, 1, fragSource, null, 0);
		gl.glCompileShader(fragProgram);
		
		int[] success = new int[1];
		
		gl.glGetShaderiv(vertProgram, GL2.GL_COMPILE_STATUS, success, 0);
		if(success[0] != 1)
		{
			System.err.println("Vertex Shader: " + vShader + ".vs, cannot be compiled");
			System.err.println(ShaderUtil.getShaderInfoLog(gl, vertProgram));
			System.exit(0);
			return false;
		}
		
		gl.glGetShaderiv(fragProgram, GL2.GL_COMPILE_STATUS, success, 0);
		if(success[0] != 1)
		{
			System.err.println("Fragment Shader: " + fShader + ".fs, cannot be compiled");
			System.err.println(ShaderUtil.getShaderInfoLog(gl, fragProgram));
			System.exit(0);
			return false;
		}
	
	    shaderID = gl.glCreateProgram();
	    
	    if(attributes != null)
	    	for(Entry<Integer, String> attr : attributes.entrySet())
				gl.glBindAttribLocation(shaderID, attr.getKey(), attr.getValue());
	    
	    gl.glAttachShader(shaderID, vertProgram);
	    gl.glAttachShader(shaderID, fragProgram);
	    
	    gl.glLinkProgram(shaderID);
	    
	    System.out.println("Shader Loader: " + vertString + ", " + fragString);
	    
	    String infoLog = ShaderUtil.getProgramInfoLog(gl, shaderID);
	    System.out.print(infoLog.equals("") ? "" : "\n" + infoLog + "\n");

	    return validate(gl);  
	}
	
	public int num_of_uses = 0;
	public int recorded_usage = 0;
	
	public static void recordUsage()
	{
		for(Shader shader : shaders.values())
		{
			shader.recorded_usage = shader.num_of_uses;
			shader.num_of_uses = 0;
		}
	}
	
	public static void printUsage()
	{
		for(Map.Entry<String, Shader> shader : shaders.entrySet())
			System.out.println(shader.getKey() + ": " + shader.getValue().recorded_usage);
		
		System.out.println();
	}
	
	/** 
	 * This function is called when you want to activate the shader.
     * Once activated, it will be used to render anything that is drawn until the
     * <code>disable</code> function is called or the <code>enable</code> function
     * is called by another Shader object.
     */
    public int enable(GL2 gl)
    {
    	num_of_uses++;
    	
        gl.glUseProgram(shaderID);
        return shaderID;
    }

    public static void disable(GL2 gl) { gl.glUseProgram(0); }

	private boolean validate(GL2 gl)
	{
		gl.glValidateProgram(shaderID);
        
        IntBuffer intBuffer = IntBuffer.allocate(1);
        gl.glGetProgramiv(shaderID, GL2.GL_LINK_STATUS, intBuffer);
        
        if(intBuffer.get(0) != 1)
        {
            gl.glGetProgramiv(shaderID, GL2.GL_INFO_LOG_LENGTH, intBuffer);
            int size = intBuffer.get(0);
            
            System.err.println("Shader linking error:");
            
            if(size > 0)
            {  	
                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                gl.glGetProgramInfoLog(shaderID, size, intBuffer, byteBuffer);
                
                for (byte b : byteBuffer.array()) System.err.print((char) b);
            }
            else System.err.println("Unknown");
            
            return false;
        }
        
        return true;
	}
	
	public String[] parseSource(String filename)
	{
		try
		{
			Scanner scanner = new Scanner(new File("shaders/" + filename));
			
			StringBuffer str = new StringBuffer();
			String line;
			
			while(scanner.hasNextLine())
			{
				line = scanner.nextLine() + "\n";
				str.append(line);
			}
			
			scanner.close();
			
			return new String[] {str.toString()};
		}
		catch(FileNotFoundException e)
		{
			System.err.println("Shader: " + filename + ", cannot be parsed");
			return null;
		}
	}

	public void setSampler(GL2 gl, String sampler, int unit)
	{
		int samplerID = gl.glGetUniformLocation(shaderID, sampler);
		gl.glUniform1i(samplerID, unit);
	}
	
	public void setUniform(GL2 gl, String uniform, float value)
	{
		int uniformID = gl.glGetUniformLocation(shaderID, uniform);
		gl.glUniform1f(uniformID, value);
	}
	
	public void setUniform(GL2 gl, String uniform, int value)
	{
		int uniformID = gl.glGetUniformLocation(shaderID, uniform);
		gl.glUniform1i(uniformID, value);
	}
	
	public void setUniform(GL2 gl, String uniform, boolean value)
	{	
		int uniformID = gl.glGetUniformLocation(shaderID, uniform);
		gl.glUniform1i(uniformID, value ? 1 : 0);
	}
	
	public void setUniform(GL2 gl, String uniform, float[] vec)
	{
		int uniformID = gl.glGetUniformLocation(shaderID, uniform);
		
		switch(vec.length)
		{
			case 2: gl.glUniform2f(uniformID, vec[0], vec[1]); break;
			case 3: gl.glUniform3f(uniformID, vec[0], vec[1], vec[2]); break;
			case 4: gl.glUniform4f(uniformID, vec[0], vec[1], vec[2], vec[3]); break;
			
			default: return;
		}
	}
	
	public void setUniform(GL2 gl, String uniform, Vec3 vec)
	{
		setUniform(gl, uniform, vec.toArray());
	}
	
	public void setUniform(GL2 gl, Uniform uniform)
	{
//		int uniformID = gl.glGetUniformLocation(shaderID, uniform.getIdentifier());
		
		if(uniform instanceof UniformFloat  ) setUniform(gl, uniform.getIdentifier(), ((UniformFloat  ) uniform).getValue());
		if(uniform instanceof UniformInt    ) setUniform(gl, uniform.getIdentifier(), ((UniformInt    ) uniform).getValue());
		if(uniform instanceof UniformBool   ) setUniform(gl, uniform.getIdentifier(), ((UniformBool   ) uniform).getValue());
		if(uniform instanceof UniformVec3   ) setUniform(gl, uniform.getIdentifier(), ((UniformVec3   ) uniform).getValue());
		if(uniform instanceof UniformSampler) setSampler(gl, uniform.getIdentifier(), ((UniformSampler) uniform).getValue());
	}
	
	public void setModelMatrix(GL2 gl, float[] matrix)
	{
		int modelMatrix = gl.glGetUniformLocation(shaderID, "model_matrix");
		gl.glUniformMatrix4fv(modelMatrix, 1, false, matrix, 0);
	}
	
	public void loadMatrix(GL2 gl, String uniform, float[] matrix)
	{
		int matrixID = gl.glGetUniformLocation(shaderID, uniform);
		gl.glUniformMatrix4fv(matrixID, 1, false, matrix, 0);
	}
}
