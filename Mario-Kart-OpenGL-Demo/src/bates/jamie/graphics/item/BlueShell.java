package bates.jamie.graphics.item;

import static bates.jamie.graphics.util.Renderer.displayWildcardObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import bates.jamie.graphics.collision.Bound;
import bates.jamie.graphics.collision.Sphere;
import bates.jamie.graphics.entity.Terrain;
import bates.jamie.graphics.entity.Vehicle;
import bates.jamie.graphics.particle.BlastParticle;
import bates.jamie.graphics.particle.Particle;
import bates.jamie.graphics.particle.ParticleGenerator;
import bates.jamie.graphics.scene.Light;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.Scene;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.scene.process.BloomStrobe;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class BlueShell extends Shell
{
	public static final int ID = 13;
	
	private static int shellList = -1;
	private static Model spike_model = new Model("spikes");
	private static SceneNode spikeNode;
	
	private static Texture[] textures;
	private static Texture shellTop;
	public  static Texture noiseSampler;
	
	public static Light blastLight;
	
	static
	{
		try
		{
			shellTop = TextureIO.newTexture(new File(TEXTURE_DIRECTORY + "blueShellTop.jpg"), true);
			
			textures = new Texture[] {shellTop};
		}
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public ParticleGenerator generator;
	
	private int   blastDuration = 0;
	private float blastRadius   = 0;
	private float blastSpeed    = 2.5f;
	
	private List<Particle> blast = new ArrayList<Particle>();
	
	public BlueShell(GL2 gl, Scene scene, Vehicle car, float trajectory)
	{
		super(gl, scene, car, trajectory);
		
		if(shellList == -1)
		{
			shellList = gl.glGenLists(1);
			gl.glNewList(shellList, GL2.GL_COMPILE);
			displayWildcardObject(gl, SHELL_FACES, textures);
			gl.glEndList();
			
			spikeNode = new SceneNode(spike_model);
			spikeNode.setRenderMode(RenderMode.COLOR);
			
			noiseSampler = TextureLoader.load(gl, "tex/blast_noise.png");
			
			float[] blastColor = RGB.INDIGO;
			
			blastLight = new Light(gl, new Vec3(), blastColor, blastColor, blastColor);
		    
			blastLight.setConstantAttenuation(0.5f);
			blastLight.setLinearAttenuation(0.001f);
			blastLight.setQuadraticAttenuation(0.001f);
			blastLight.enableAttenuation = true;
			blastLight.disable(gl);
		}
		
		generator = new ParticleGenerator();
		
		boundColor = RGB.toRGBA(RGB.INDIGO, BOUND_ALPHA);
	}
	
	public BlueShell(Scene scene, Vec3 c)
	{
		super(null, scene, null, 0);
		
		bound = new Sphere(c, RADIUS);
		boundColor = RGB.toRGBA(RGB.INDIGO, BOUND_ALPHA);
		
		generator = new ParticleGenerator();
	}
	
	@Override
	public void render(GL2 gl, float trajectory)
	{
		if(!dead)
		{
			gl.glPushMatrix();
			{
				gl.glTranslatef(bound.c.x, bound.c.y, bound.c.z);
				gl.glRotatef(rotation, 0, -1, 0);
				gl.glScalef(1.5f, 1.5f, 1.5f);
				
				Shader shader = Shader.getLightModel("texture");
				if(shader != null) shader.enable(gl);
				
				gl.glCallList(shellList);
				
				spikeNode.render(gl);
				rimNode.render(gl);
				
			}
			gl.glPopMatrix();
			
			Shader.disable(gl);
		}	
		else if(!blast.isEmpty() && !Scene.shadowMode)
		{
			boolean useHDR = BloomStrobe.isEnabled();
			
			if(Scene.reflectMode) BloomStrobe.end(gl);

			if(Scene.singleton.enableBloom) renderBlast(gl);
			else BlastParticle.renderList(gl, blast);
			
			if(useHDR) BloomStrobe.begin(gl);
			
			Shader.disable(gl);
		}
		
		if(blastDuration <= 1) blastLight.disable(gl); 
		else blastLight.enable(gl);
	}

	private void renderBlast(GL2 gl)
	{
		GLU glu = new GLU();
		
		Shader shader = Shader.get("dissolve");
		if(shader != null)
		{
			shader.enable(gl);
			
			shader.setSampler(gl, "cloudSampler", 0);
			shader.setUniform(gl, "dissolveFactor", 1.0f - ((float) blastDuration / 60.0f));
		}
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		
		GLUquadric sphere = glu.gluNewQuadric();
		
		glu.gluQuadricDrawStyle(sphere, GLU.GLU_FILL);
		glu.gluQuadricTexture  (sphere, true);
		
		gl.glPushMatrix();
		{
			noiseSampler.bind(gl);
			noiseSampler.setTexParameterf(gl, GL2.GL_TEXTURE_MAX_ANISOTROPY_EXT, 16);
			
			gl.glTranslatef(bound.c.x, bound.c.y, bound.c.z);
			
			glu.gluSphere(sphere, blastRadius, 24, 24);
		}
		gl.glPopMatrix();
	}
	
	@Override
	public void update()
	{
		if(!dead)
		{
			setPosition(getPositionVector());
			if(falling) fall();
	
			for(Bound bound : scene.getBounds())
				if(bound.testBound(this.bound))
					{ destroy(); break; }
			
			if(scene.enableTerrain)
			{
				Terrain terrain = scene.getTerrain();
				float h = terrain.getHeight(terrain.trees.values(), getPosition().toArray());
				
				if(bound.c.y - bound.getMaximumExtent() <= h) destroy();
			}
	
			rotation += 10 * velocity;
		}
		else if(blastDuration > 0)
		{
			bound = new Sphere(getPosition(), blastRadius);
			blastRadius += blastSpeed;
			blastSpeed *= 0.9f;
			blastDuration--;
			
			for(Particle particle : blast) particle.update();
			Particle.removeParticles(blast);
			
			blastLight.setPosition(getPosition());
			scene.focalBlur.blurCentre = blastLight.getPosition();
			scene.focalBlur.enableRadial = true;
			scene.focalBlur.blurFactor = (float) blastDuration / 60.0f;
		}
	}
	
	@Override
	public void destroy()
	{
		if(!dead)
		{
			blastDuration = 60;
			blast = generator.generateBlastParticles(getPosition(), 1800);
		}
		
		dead = true;
	}
	
	@Override
	public boolean isDead() { return dead && blastDuration < 1; }
	
	@Override
	public void collide(Vehicle car)
	{
		if(!dead) destroy();
		else if(blastDuration > 50)
		{
			car.velocity = 0;
			car.spin();
		}
		else if(blastDuration >  0) car.spin();
	}
}
