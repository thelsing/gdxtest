package org.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import space.earlygrey.shapedrawer.*;


import java.util.ArrayList;

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
				150,150,
				150,550,
				350,450,
				550,550,
				700,150,
		};


		var region = new TextureRegion(image);
		region.flip(false, true);
		var calcPath = path(FloatArray.with(path), wall, JoinType.Smooth, false);

		sprite.setVertices(calcPath);
		//sprite.setTextureRegion(region);
		//sprite.draw(batch);
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

	float[] path(FloatArray path, float lineWidth, JoinType joinType, boolean open) {
		var outer = new ArrayList<Float>();
		var inner = new ArrayList<Float>();

		float halfWidth = lineWidth/2f;
		for (int i = 2; i < path.size - 2; i+=2) {
			A.set(path.get(i-2), path.get(i-1));
			B.set(path.get(i), path.get(i+1));
			C.set(path.get(i+2), path.get(i+3));
			if (i == 2) {
				if(open) {
					Joiner.prepareSquareEndpoint(path.get(2), path.get(3), path.get(0), path.get(1), D, E, halfWidth);
					outer.add(D.x);
					outer.add(D.y);

					// add link at start
					inner.add(D.x);
					inner.add(D.y);

					inner.add(E.x);
					inner.add(E.y);
				} else {
					vec1.set(path.get(path.size - 2), path.get(path.size - 1));
					if (joinType == JoinType.Pointy) {
						Joiner.preparePointyJoin(vec1, A, B, D0, E0, halfWidth);
					} else {
						Joiner.prepareSmoothJoin(vec1, A, B, D0, E0, halfWidth, true);
					}
					outer.add(D0.x);
					outer.add(D0.y);
					inner.add(E0.x);
					inner.add(E0.y);
				}
			}
			if (joinType == JoinType.Pointy) {
				Joiner.preparePointyJoin(A, B, C, D, E, halfWidth);
			} else {
				Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
			}
			outer.add(D.x);
			outer.add(D.y);
			inner.add(E.x);
			inner.add(E.y);

			if (joinType == JoinType.Smooth) {
				Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, true);
				outer.add(D.x);
				outer.add(D.y);
				inner.add(E.x);
				inner.add(E.y);
			}
		}
		if (open) {
			//draw last link on path
			Joiner.prepareSquareEndpoint(B, C, D, E, halfWidth);
			outer.add(E.x);
			outer.add(E.y);

			inner.add(D.x);
			inner.add(D.y);
		} else {
			if (joinType == JoinType.Pointy) {
				//draw last link on path
				A.set(path.get(0), path.get(1));
				Joiner.preparePointyJoin(B, C, A, D, E, halfWidth);
				outer.add(D.x);
				outer.add(D.y);
				inner.add(E.x);
				inner.add(E.y);

				//draw connection back to first vertex
				outer.add(D0.x);
				outer.add(D0.y);
				inner.add(E0.x);
				inner.add(E0.y);

			} else {
				//draw last link on path
				A.set(B);
				B.set(C);
				C.set(path.get(0), path.get(1));
				Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
				outer.add(D.x);
				outer.add(D.y);
				inner.add(E.x);
				inner.add(E.y);

				//draw connection back to first vertex
				Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, true);
				outer.add(D.x);
				outer.add(D.y);
				inner.add(E.x);
				inner.add(E.y);
				A.set(path.get(2), path.get(3));
				Joiner.prepareSmoothJoin(B, C, A, D, E, halfWidth, false);
				outer.add(D.x);
				outer.add(D.y);
				inner.add(E.x);
				inner.add(E.y);

				outer.add(D0.x);
				outer.add(D0.y);
			}
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