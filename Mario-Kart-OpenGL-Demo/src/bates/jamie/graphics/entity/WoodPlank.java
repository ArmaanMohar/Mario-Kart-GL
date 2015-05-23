package bates.jamie.graphics.entity;

import javax.media.opengl.GL2;

import bates.jamie.graphics.scene.Material;
import bates.jamie.graphics.scene.Model;
import bates.jamie.graphics.scene.SceneNode;
import bates.jamie.graphics.scene.SceneNode.MatrixOrder;
import bates.jamie.graphics.scene.SceneNode.RenderMode;
import bates.jamie.graphics.util.RGB;
import bates.jamie.graphics.util.TextureLoader;
import bates.jamie.graphics.util.Vec3;
import bates.jamie.graphics.util.shader.Shader;

import com.jogamp.opengl.util.texture.Texture;

public class WoodPlank
{
	static Model[] plank_models =
	{
		new Model("plank_1"),
		new Model("plank_2"),
		new Model("plank_3"),
		new Model("plank_4")
	};
	
	static Texture colourMap, normalMap, heightMap;
	
	SceneNode plankNode;
	
	float rotation = 0;
	Vec3  position;
	
	public WoodPlank(GL2 gl, Vec3 p, int model)
	{
		if(colourMap == null)
		{
			try
			{	
				colourMap = TextureLoader.load(gl, "tex/plank_COLOR.jpg");
				normalMap = TextureLoader.load(gl, "tex/plank_NRM.jpg");
				heightMap = TextureLoader.load(gl, "tex/plank_DISP.jpg");
			}
			catch (Exception e) { e.printStackTrace(); }
		}
		
		position = p;
		
		plankNode = new SceneNode(null, -1, plank_models[model], MatrixOrder.T_RY_RX_RZ_S, new Material(new float[] {1, 1, 1}));
		plankNode.setTranslation(p);
		plankNode.setScale(new Vec3(2.0));
		plankNode.setRenderMode(RenderMode.BUMP_TEXTURE);
		plankNode.setColor(RGB.WHITE);
	}
	
	public void setPosition(Vec3 p)
	{
		position = p;
		
		plankNode.setTranslation(p);
	}
	
	public Vec3 getPosition() { return position; }
	
	public void render(GL2 gl)
	{	
//		Shader shader = Shader.get("wood");
//		
//		shader.enable(gl);
//		
//		shader.setSampler(gl, "noiseSampler", 0);
//		
//		shader.setUniform(gl, "lightColor", new Vec3(0.6, 0.3, 0.10));
//		shader.setUniform(gl,  "darkColor", new Vec3(0.4, 0.2, 0.07));
//		
//		shader.setUniform(gl, "ringFreq", 4.0f);
//		shader.setUniform(gl, "lightGrains", 1.0f);
//		shader.setUniform(gl,  "darkGrains", 0.0f);
//		shader.setUniform(gl, "grainThreshold", 0.5f);
//		
//		shader.setUniform(gl,  "noiseScale", new Vec3(0.5, 0.1, 0.1));
//		
//		shader.setUniform(gl, "noisiness", 3.0f);
//		shader.setUniform(gl, "grainScale", 27.0f);
		
		plankNode.setRotation(new Vec3(0, rotation, 0));
//	    plankNode.render(gl, shader, null);
		
		gl.glActiveTexture(GL2.GL_TEXTURE2); heightMap.bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE1); normalMap.bind(gl);
		gl.glActiveTexture(GL2.GL_TEXTURE0); colourMap.bind(gl);
	    
	    plankNode.render(gl);
		
		Shader.disable(gl);
		
		gl.glColor3f(1, 1, 1);
	}
	
}
