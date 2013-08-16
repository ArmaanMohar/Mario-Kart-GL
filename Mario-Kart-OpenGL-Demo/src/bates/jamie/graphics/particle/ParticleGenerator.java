package bates.jamie.graphics.particle;


import static bates.jamie.graphics.util.Vector.multiply;
import static bates.jamie.graphics.util.Vector.normalize;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import bates.jamie.graphics.entity.Car;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.Vector;

import com.jogamp.opengl.util.texture.Texture;

public class ParticleGenerator
{
	private Random generator;
	
	private int pulse    = 0;
	private int counter  = 0;
	private int quantity = 0;
	
	private float[] source = {0, 0, 0};
	
	public enum GeneratorType
	{
		BLAST,
		SPARK;
	}
	
	private GeneratorType type;
	
	public ParticleGenerator() { generator = new Random(); }
	
	public ParticleGenerator(int pulse, int quantity, GeneratorType type, float[] source)
	{
		generator = new Random();
		
		this.pulse = pulse;
		this.quantity = quantity;
		this.type = type;
		
		this.source = source;
	}
	
	public void setPulse(int pulse) { this.pulse = pulse; }
	
	public void setQuantity(int quantity) { this.quantity = quantity; }
	
	public boolean update()
	{
		counter++;
		counter %= pulse;
		
		return counter == 0;
	}
	
	public List<Particle> generate()
	{
		switch(type)
		{
			case BLAST: return generateBlastParticles(source, quantity);
			case SPARK: return generateSparkParticles(source, getRandomVector(), quantity, 1, false, null);
			
			default: return null;
		}
	}
	
	public List<Particle> generateTerrainParticles(float[] source, int n, Texture texture)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{
			float[] texCoords = {generator.nextFloat(), generator.nextFloat()};
			
			float[] t = getRandomVector();
			t[1] = Math.abs(t[1] * 0.75f);
			
			particles.add(new TerrainParticle(source, t, 0, 12, texCoords, texture));
		}
		
		return particles;
	}
	
	public List<Particle> generateItemBoxParticles(float[] source, int n)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		float[][] colors = {RGB.RED, RGB.ORANGE, RGB.YELLOW, RGB.GREEN, RGB.BLUE, RGB.INDIGO, RGB.VIOLET};
		
		for(int i = 0; i < n; i++)
		{
			float[]  color = colors[generator.nextInt(colors.length)];
			float[] _color = {color[0]/255, color[1]/255, color[2]/255}; 
			
			float[] t = getRandomVector();
			
			particles.add(new ItemBoxParticle(source, t, 0, _color, generator.nextBoolean(), false));
		}
		
		return particles;
	}
	
	public List<Particle> generateSparkParticles(float[] source, float[] t, int n, int type, boolean miniature, Car car)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		float[][][] colors =
		{
			{RGB.YELLOW, RGB.ORANGE},
			{RGB.ORANGE, RGB.RED   },
			{RGB.INDIGO, RGB.BLUE  },
		};
		
		for(int i = 0; i < n; i++)
		{
			float[]  color = Vector.mix(colors[type][0], colors[type][1], generator.nextFloat());
			float[] _color = {color[0]/255, color[1]/255, color[2]/255};
			
			float[] _t = Vector.add(Vector.normalize(t), getRandomVector());
			_t[1] = Math.abs(_t[1] * (generator.nextBoolean() ? 1 : 2));
			
			int length = 3 + generator.nextInt(4);
			
			particles.add(new SparkParticle(source, _t, 8, _color, length, miniature, car));
		}
		
		return particles;
	}
	
	public List<Particle> generateDriftParticles(float[] source, int n, int color, boolean miniature)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{	
			float rotation = -45 + generator.nextInt(90);
			
			particles.add(new DriftParticle(source, rotation, color, generator.nextBoolean(), miniature));
		}
		
		return particles;
	}
	
	public List<Particle> generateBlastParticles(float[] source, int n)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{
			float[] t = getRandomVector();
			t = multiply(normalize(t), 2.5f);
			
			int duration = 30 + generator.nextInt(30);
			
			particles.add(new BlastParticle(source, t, 0, duration));
		}
		
		return particles;
	}
	
	public List<Particle> generateStarParticles(float[] source, int n, boolean miniature)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		for(int i = 0; i < n; i++)
		{
			float[] t = getRandomVector();
			if(miniature) t = multiply(t, 0.5f);
			
			float scale = generator.nextFloat() * ((miniature) ? 1.25f : 2.5f);
			
			particles.add(new StarParticle(source, t, 5, scale));
		}
		
		return particles;
	}
	
	public List<Particle> generateFakeItemBoxParticles(float[] source, int n, boolean miniature)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		Random generator = new Random();
		
		for(int i = 0; i < n; i++)
		{
			float[]  color = RGB.RED;
			float[] _color = {color[0]/255, color[1]/255, color[2]/255}; 
			
			float[] t = getRandomVector();
			if(miniature) t = multiply(t, 0.5f);
			
			particles.add(new ItemBoxParticle(source, t, 45, _color, generator.nextBoolean(), miniature));
		}
		
		return particles;
	}
	
	public List<Particle> generateBoostParticles(float[] source, int n, boolean special, boolean miniature)
	{
		List<Particle> particles = new ArrayList<Particle>();
		
		Random generator = new Random();
		
		for(int i = 0; i < n; i++)
		{	
			float[] t = getRandomVector();
			
			float k = (special) ? 0.55f : 0.5f;
			t = multiply(t, k);
			if(miniature) t = multiply(t, 0.5f);
			
			float scale = generator.nextFloat() * ((miniature) ? 1.25f : 2.5f);
			
			particles.add(new BoostParticle(source, t, 0, 1, scale, special, miniature));
		}
		
		return particles;
	}
	
	private float[] getRandomVector()
	{
		float xVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		float yVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		float zVel = (generator.nextBoolean()) ? generator.nextFloat() : -generator.nextFloat();
		
		return new float[] {xVel, yVel, zVel};
	}
}
