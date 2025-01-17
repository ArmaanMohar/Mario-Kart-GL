package bates.jamie.graphics.scene.process;

import static javax.media.opengl.GL.GL_DEPTH_BUFFER_BIT;
import static javax.media.opengl.GL.GL_TEXTURE0;
import static javax.media.opengl.GL.GL_TEXTURE_2D;

import javax.media.opengl.GL2;

import bates.jamie.graphics.entity.Vehicle;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.util.FrameBuffer;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;

public class FocalBlur
{
	private Scene scene;
	
	private int sceneTexture; // scene to apply blur to
	private int depthTexture;
	private int depthBuffer ; // framebuffer ID
	
	private Texture mirageTexture;
	
	private int fboWidth  = Scene.canvasWidth;
	private int fboHeight = Scene.canvasHeight;
	
	private float[] offsets_3x3 = new float[3 * 3 * 2]; // 3 by 3 Guassian mask
	private float[] offsets_5x5 = new float[5 * 5 * 2];
	private float[] offsets_7x7 = new float[7 * 7 * 2];
	
	private int sampleQuality = 1;
	
	public boolean enableMirage = false;
	public boolean enableRadial = false;
	
	public float decay    = 0.75f;
	public float exposure = 0.40f;
	public float weight   = 0.50f;
	public float density  = 0.01f;
	
	public float blurFactor = 0.0f;
	public Vec3  blurCentre = new Vec3();
	
	public int samples = 10;
	
	
	public FocalBlur(Scene scene)
	{
		this.scene = scene;
	}
	
	public void setup(GL2 gl)
	{
		createTexture(gl);
		createBuffers(gl);
		
		System.out.println("FocalBlur : Texture ID (" + sceneTexture + ")");
	    
	    update(gl);
	    
	    setGuassians();
	}
	
	public int getDepthTexture() { return depthTexture; }
	public int getSceneTexture() { return sceneTexture; }
	
	private void createTexture(GL2 gl)
	{
		mirageTexture = TextureLoader.load(gl, "tex/bump_maps/rain.png"); 
		
		// Try to use a texture depth component
		int[] texID = new int[2];
		gl.glGenTextures(2, texID, 0);
		sceneTexture = texID[0];
		depthTexture = texID[1];
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, sceneTexture);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
		
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA8, scene.getWidth(), scene.getHeight(), 0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE, null);
		
		gl.glBindTexture(GL_TEXTURE_2D, depthTexture);

		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);

		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);

		gl.glTexParameteri(GL_TEXTURE_2D, GL2.GL_DEPTH_TEXTURE_MODE, GL2.GL_INTENSITY);

		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT, fboWidth, fboHeight, 0, GL2.GL_DEPTH_COMPONENT, GL2.GL_UNSIGNED_BYTE, null);
	}
	
	private void createBuffers(GL2 gl)
	{
		fboWidth  = scene.getWidth () * sampleQuality;
		fboHeight = scene.getHeight() * sampleQuality;
	
		int bufferStatus = 0;
	
		// create a framebuffer object
		int[] fboID = new int[1];
		gl.glGenFramebuffers(1, fboID, 0);
		depthBuffer = fboID[0];
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, depthBuffer);
	
		// Instruct OpenGL that we won't bind a color texture with the currently bound FBO
		gl.glDrawBuffer(GL2.GL_NONE);
		gl.glReadBuffer(GL2.GL_NONE);
	
		// attach the texture to FBO depth attachment point
		gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0);
	
		// check FBO status
		bufferStatus = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
		if(bufferStatus != GL2.GL_FRAMEBUFFER_COMPLETE)
			System.out.println("FocalBlur : " + FrameBuffer.checkFramebufferError(bufferStatus));
	
		// switch back to window-system-provided framebuffer
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
	}
	
	public void update(GL2 gl)
	{
		enableTextures(gl);
		
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, depthBuffer); // rendering offscreen.
	    gl.glViewport(0, 0, fboWidth, fboHeight); // need larger viewport
 
	    depthMode(gl, true);
	    
	    Scene.beginRenderLog("DEPTH MODE");
	    
	    depthPass(gl);
	    
	    Scene.endRenderLog();

	    depthMode(gl, false);

	    gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
	    gl.glViewport(0, 0, scene.getWidth(), scene.getHeight());
	}
	
	private void depthPass(GL2 gl)
	{
		Vehicle car = scene.getCars().get(0);
		Terrain terrain = scene.getTerrain();
		
//		if(terrain != null && terrain.enableWater) scene.renderWater(gl, car);
		
		scene.render3DModels(gl, car);
		scene.renderWorld(gl);
		
		scene.renderParticles(gl, car);
		Particle.resetTexture();
		
		gl.glColor3f(1, 1, 1);
		
		if(scene.enableTerrain) scene.renderFoliage(gl, car);
		
		if(terrain != null && terrain.enableWater)
			scene.water.render(gl, car.camera.getPosition());
		
		if(scene.displayLight && !scene.moveLight)
			for(Light l : scene.lights) l.render(gl);
	}
	
	private void depthMode(GL2 gl, boolean enable)
	{		
		if(enable) // Only need depth values
		{
			Shader.enableSimple = true; // shaders not required
			Scene.depthMode = true;
			
			// Clear the depth buffer only
			gl.glDepthMask(true);
		    gl.glClear(GL_DEPTH_BUFFER_BIT);
		    
		    gl.glShadeModel(GL2.GL_FLAT);
		    
		    gl.glDisable(GL2.GL_LIGHTING      );
		    gl.glDisable(GL2.GL_COLOR_MATERIAL);
		    gl.glDisable(GL2.GL_NORMALIZE     );
		    
		    gl.glColorMask(false, false, false, false);
		}
		else // Restore normal drawing state
		{ 
			Scene.depthMode = false;
			Shader.enableSimple = false; 
			
		    gl.glShadeModel(GL2.GL_SMOOTH);
		    
		    gl.glEnable(GL2.GL_LIGHTING      );
		    gl.glEnable(GL2.GL_COLOR_MATERIAL);
		    gl.glEnable(GL2.GL_NORMALIZE     );
		    
		    gl.glColorMask(true, true, true, true);
		}
	}
	
	private void copyScene(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE1);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, sceneTexture);
		
		gl.glCopyTexImage2D(GL_TEXTURE_2D, 0, GL2.GL_RGBA8, 0, 0, scene.getWidth(), scene.getHeight(), 0);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
	}
	
	public void guassianPass(GL2 gl)
	{
		copyScene(gl);
		
		gl.glActiveTexture(GL2.GL_TEXTURE2);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, depthTexture);
		
		gl.glActiveTexture(GL2.GL_TEXTURE3);
		mirageTexture.bind(gl);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, Scene.singleton.rainScreen.getHeightMap());
		
		Shader shader = (enableRadial && scene.enableRadial) ? Shader.get("radial_blur") :
						(enableMirage ? Shader.get("heat_haze") : Shader.get("depth_field"));
		if(shader == null) return;
		
		shader.enable(gl);
		
		shader.setSampler(gl, "sceneSampler" , 1);
		shader.setSampler(gl, "depthSampler" , 2);
		shader.setSampler(gl, "normalSampler", 3);
		
		if(enableRadial && scene.enableRadial)
		{
			shader.setUniform(gl, "decay"   , decay   );
			shader.setUniform(gl, "exposure", exposure);
			shader.setUniform(gl, "weight"  , weight  );
			shader.setUniform(gl, "density" , density + blurFactor * 0.09f);
			
			shader.setUniform(gl, "samples", samples);
			
			shader.setUniform(gl, "focalPosition", blurCentre.toArray());
		}
		
		if(enableMirage)
		{
			shader.setUniform(gl, "timer", (float) Scene.sceneTimer);
			
			gl.glActiveTexture(GL2.GL_TEXTURE4);
			scene.bloom.getTexture(1);
			gl.glActiveTexture(GL2.GL_TEXTURE0);
			
			shader.setSampler(gl, "bloomSampler", 4);
			
			shader.setUniform(gl, "decay"   , decay   );
			shader.setUniform(gl, "exposure", exposure);
			shader.setUniform(gl, "weight"  , weight  );
			shader.setUniform(gl, "density" , density + blurFactor * 0.09f);
			
			shader.setUniform(gl, "samples", samples);
		}
		
		int offsetsLoc = -1;
		
		offsetsLoc = gl.glGetUniformLocation(shader.shaderID, "offsets_3x3");
		gl.glUniform2fv(offsetsLoc,  9, offsets_3x3, 0);
		
		offsetsLoc = gl.glGetUniformLocation(shader.shaderID, "offsets_5x5");
		gl.glUniform2fv(offsetsLoc, 25, offsets_5x5, 0);
		
		offsetsLoc = gl.glGetUniformLocation(shader.shaderID, "offsets_7x7");
		gl.glUniform2fv(offsetsLoc, 49, offsets_7x7, 0);
		
		gl.glBegin(GL2.GL_QUADS);
		{
			gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 0.0f); gl.glVertex2f(-1.0f, -1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 0.0f, 1.0f); gl.glVertex2f(-1.0f, +1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 1.0f); gl.glVertex2f(+1.0f, +1.0f);
			gl.glMultiTexCoord2f(GL_TEXTURE0, 1.0f, 0.0f); gl.glVertex2f(+1.0f, -1.0f);
		}
		gl.glEnd();
		
		Shader.disable(gl);
		
		disableTextures(gl);
	}
	
	public void enableTextures(GL2 gl)
	{	
		gl.glActiveTexture(GL2.GL_TEXTURE2);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, depthTexture);
		
		gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
		
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}

	private void disableTextures(GL2 gl)
	{
		gl.glActiveTexture(GL2.GL_TEXTURE2); gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE1); gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
	}
	
	private void setGuassians()
	{
		offsets_3x3 = getGuassian(3);
		offsets_5x5 = getGuassian(5);
		offsets_7x7 = getGuassian(7);
	}
	
	private float[] getGuassian(int size)
	{
		float[] mask = new float[size * size * 2];
		int half = (size / 2);
	 
		float xInc = 1.0f / (float) (scene.getWidth() );
		float yInc = 1.0f / (float) (scene.getHeight());

		for(int i = 0; i < size; i++)
		{
			for(int j = 0; j < size; j++)
			{
				int index = ((i * size) + j) * 2;

				mask[index + 0] = (-1.0f * half * xInc) + ((float) i * xInc);
				mask[index + 1] = (-1.0f * half * yInc) + ((float) j * yInc);
			}
		}
	    
	    return mask;
	}
}
