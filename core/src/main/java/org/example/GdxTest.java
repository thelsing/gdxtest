package org.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import space.earlygrey.shapedrawer.*;


import java.util.ArrayList;
import java.util.stream.Collectors;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GdxTest extends ApplicationAdapter {
	private PolygonSpriteBatch batch;
	private Texture image;
	private RepeatablePolygonSprite sprite;

	private ShapeRenderer shapeRenderer;

	private ShapeDrawer drawer;

	@Override
	public void create() {
		batch = new PolygonSpriteBatch();
		image = new Texture("libgdx.png");
		sprite = new RepeatablePolygonSprite();
		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);
		drawer = new ShapeDrawer(batch);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();

		float x = 100;
		float y = 100;
		float h = 500;
		float w = 600;
		float wall = 100;

		var path = new float[] {
				x+wall/2,y+wall/2,
				x+wall/2,y+h-wall/2,
				x+w-wall/2,y+h-wall/2,
				x+w-wall/2,y+wall/2,
		};

		float[] vertices = new float[] {
				x,y,
				x,y + h,
				x+w,y+h,
				x+w,y,
				x,y,
				x+wall,y+wall,
				x+w-wall,y+wall,
				x+w-wall,y+h-wall,
				x+wall,y+h-wall,
				x+wall,y+wall
		};

		var region = new TextureRegion(image);
		region.flip(false, true);
		var calcPath = path(FloatArray.with(path), wall, JoinType.Smooth);

//		drawer.startRecording();
//		drawer.path(path, wall, space.earlygrey.shapedrawer.JoinType.POINTY, false);
//		var drawing = drawer.stopRecording();
//		var coordinates = new FloatArray();
//		drawing.getTransformedXYCoordinates(coordinates);

		sprite.setVertices(calcPath);
		//sprite.setTextureRegion(region);
		sprite.draw(batch);
		//batch.draw(image, 140, 210);

		batch.end();
		shapeRenderer.begin();
		//sprite.drawDebug(shapeRenderer, Color.CORAL);
		shapeRenderer.setColor(Color.RED);
		for(var i = 1; i<path.length; i+=2)
			shapeRenderer.circle(path[i-1], path[i], 5);



		shapeRenderer.polyline(path);
		//shapeRenderer.setColor(Color.BLUE);
		//shapeRenderer.polyline(vertices);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.polyline(calcPath);
		shapeRenderer.end();


	}

	enum JoinType {
		Pointy,
		Smooth,
		Round

	}

	private Vector2 A = new Vector2();
	private Vector2 B = new Vector2();
	private Vector2 C = new Vector2();
	private Vector2 D = new Vector2();
	private Vector2 E = new Vector2();
	private Vector2 E0 = new Vector2();
	private Vector2 D0 = new Vector2();
	private Vector2 vec1 = new Vector2();
	private Vector2 vert1 = new Vector2();
	private Vector2 vert2 = new Vector2();
	private Vector2 vert3 = new Vector2();
	private Vector2	vert4 = new Vector2();
	float[] path(FloatArray path, float lineWidth, JoinType joinType) {
		var outer = new ArrayList<Float>();
		var inner = new ArrayList<Float>();

		float halfWidth = lineWidth/2f;
		for (int i = 2; i < path.size - 2; i+=2) {
			A.set(path.get(i-2), path.get(i-1));
			B.set(path.get(i), path.get(i+1));
			C.set(path.get(i+2), path.get(i+3));
			if (i == 2) {
					vec1.set(path.get(path.size  -2), path.get(path.size - 1));
					if (joinType == JoinType.Pointy) {
						Joiner.preparePointyJoin(vec1, A, B, D0, E0, halfWidth);
					} else {
						Joiner.prepareSmoothJoin(vec1, A, B, D0, E0, halfWidth, true);
					}
					vert1.set(E0);
					vert2.set(D0);
			}
			if (joinType == JoinType.Pointy) {
				Joiner.preparePointyJoin(A, B, C, D, E, halfWidth);
			} else {
				Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
			}
			vert3.set(D);
			vert4.set(E);

			float x3, y3, x4, y4;
			if (joinType == JoinType.Pointy) {
				x3 = vert3.x;
				y3 = vert3.y;
				x4 = vert4.x;
				y4 = vert4.y;
			} else {
				Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, true);
				x3 = D.x;
				y3 = D.y;
				x4 = E.x;
				y4 = E.y;
			}
			inner.add(vert1.x);
    		inner.add(vert1.y);
			outer.add(vert2.x);
			outer.add(vert2.y);
			outer.add(vert3.x);
			outer.add(vert3.y);
			inner.add(vert4.x);
			inner.add(vert4.y);
			vert1.x = x4;
			vert1.y = y4;
			vert2.x = x3;
			vert2.y = y3;
		}

		if (joinType == JoinType.Pointy) {
			//draw last link on path
			A.set(path.get(0), path.get(1));
			Joiner.preparePointyJoin(B, C, A, D, E, halfWidth);
			vert3.set(D);
			vert4.set(E);
			inner.add(vert1.x);
			inner.add(vert1.y);
			outer.add(vert2.x);
			outer.add(vert2.y);
			outer.add(vert3.x);
			outer.add(vert3.y);
			inner.add(vert4.x);
			inner.add(vert4.y);

			//draw connection back to first vertex
			vert1.set(D);
			vert2.set(E);
			vert3.set(E0);
			vert4.set(D0);

			outer.add(vert1.x);
			outer.add(vert1.y);
			inner.add(vert2.x);
			inner.add(vert2.y);
			inner.add(vert3.x);
			inner.add(vert3.y);
			outer.add(vert4.x);
			outer.add(vert4.y);

		} else {
			//draw last link on path
			A.set(B);
			B.set(C);
			C.set(path.get(0), path.get(1));
			Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
			vert3.set(D);
			vert4.set(E);
			inner.add(vert1.x);
			inner.add(vert1.y);
			outer.add(vert2.x);
			outer.add(vert2.y);
			outer.add(vert3.x);
			outer.add(vert3.y);
			inner.add(vert4.x);
			inner.add(vert4.y);

			//draw connection back to first vertex
			Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, true);
			vert3.set(E);
			vert4.set(D);
			A.set(path.get(2), path.get(3));
			Joiner.prepareSmoothJoin(B, C, A, D, E, halfWidth, false);
			vert1.set(D);
			vert2.set(E);

			inner.add(vert3.x);
			inner.add(vert3.y);
			outer.add(vert4.x);
			outer.add(vert4.y);
			outer.add(vert1.x);
			outer.add(vert1.y);
			inner.add(vert2.x);
			inner.add(vert2.y);
			outer.add(D0.x);
			outer.add(D0.y);
		}

		float[] floatArray = new float[outer.size()+ inner.size()];
		int i = 0;

		for (Float f : outer) {
			floatArray[i++] =  f ;
		}
		for (int j = 1; j <= inner.size(); j+=2 ) {
			floatArray[floatArray.length - j] =  inner.get(j);
			floatArray[floatArray.length - j - 1] =  inner.get(j - 1);
		}
		return floatArray;
	}



	@Override
	public void dispose() {
		batch.dispose();
		image.dispose();
		shapeRenderer.dispose();
	}
}