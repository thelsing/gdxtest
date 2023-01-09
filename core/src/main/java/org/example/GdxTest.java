package org.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import space.earlygrey.shapedrawer.*;


import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GdxTest extends ApplicationAdapter {
	private static final float POINTS_PER_BEZIER = 10;
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


		//sprite.setVertices(calcPath);
		//sprite.setTextureRegion(region);
		//sprite.draw(batch);
		//batch.draw(image, 140, 210);

		batch.end();
		shapeRenderer.begin();
		shapeRenderer.setColor(Color.YELLOW);

//		var v1 = new Vector2();
//		v1.set(-1.0f, -1.0f);
//		var angle = v1.angleRad();
//
//		var v2 = new Vector2();
//		v2.set(0f, -1.0f);
//		angle = v2.angleRad();
//
//		var arc = new ArrayList<Float>();
//		addArc(arc, 200, 200, 100, v1.angleRad(), v2.angleRad(), false);
//		var calcPath = new float[arc.size()];
//		var i = 0;
//		for(var f : arc)
//			calcPath[i++] = f;




		var calcPath = path(FloatArray.with(path), wall, JoinType.Round, true);
		//sprite.drawDebug(shapeRenderer, Color.CORAL);
		shapeRenderer.setColor(Color.RED);
		for(var i = 1; i<path.length; i+=2)
			pointAt(path[i-1], path[i]);

//		shapeRenderer.polyline(path);
		//shapeRenderer.setColor(Color.BLUE);
		//shapeRenderer.polyline(vertices);
		shapeRenderer.setColor(Color.GREEN);
		shapeRenderer.polyline(calcPath);
		shapeRenderer.end();


	}

	void pointAt(float x, float y) {
		shapeRenderer.circle(x, y, 5);
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
	private Vector2 AB = new Vector2();
	private Vector2 BC = new Vector2();
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
					if(joinType == JoinType.Round) {
						Joiner.prepareFlatEndpoint(B, A, D, E, halfWidth);
						outer.add(D.x);
						outer.add(D.y);
						vec1.set(D).add(-A.x, - A.y);
						var angle = vec1.angleRad();
						addArc(inner, A.x, A.y, halfWidth, angle, angle + MathUtils.PI, false);
						inner.add(E.x);
						inner.add(E.y);
					} else {
						Joiner.prepareSquareEndpoint(B, A, D, E, halfWidth);
						outer.add(D.x);
						outer.add(D.y);

						// add link at start
						inner.add(D.x);
						inner.add(D.y);

						inner.add(E.x);
						inner.add(E.y);
					}
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
				outer.add(D.x);
				outer.add(D.y);
				inner.add(E.x);
				inner.add(E.y);
			} else {
				var bendsLeft = Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
				if(bendsLeft) {
					vec1.set(E);
				} else {
					vec1.set(D);
				}
				outer.add(D.x);
				outer.add(D.y);
				inner.add(E.x);
				inner.add(E.y);
				//shapeRenderer.circle(B.x, B.y, halfWidth);

				Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, true);
				if(bendsLeft) {
					if(joinType == JoinType.Round) {
						AB.set(B).sub(A);
						BC.set(C).sub(B);
						vec1.add(-B.x, - B.y);
						var angle = vec1.angleRad();
						var angleDiff = MathUtils.PI2 - ShapeUtils.angleRad(AB, BC);
						addArc(inner, B.x, B.y, halfWidth, angle, angle + angleDiff, false);
					}
					inner.add(E.x);
					inner.add(E.y);
				} else {
					if(joinType == JoinType.Round) {
						AB.set(B).sub(A);
						BC.set(C).sub(B);
						vec1.add(-B.x, - B.y);
						var angle = vec1.angleRad();
						var angleDiff = MathUtils.PI2 - ShapeUtils.angleRad(AB, BC);
						addArc(outer, B.x, B.y, halfWidth, angle, angle + angleDiff, true);
					}
					outer.add(D.x);
					outer.add(D.y);
				}
			}
		}
		if (open) {
			if(joinType == JoinType.Round) {
				Joiner.prepareFlatEndpoint(B, C, D, E, halfWidth);
				outer.add(E.x);
				outer.add(E.y);
				inner.add(D.x);
				inner.add(D.y);
				vec1.set(D).add(-C.x, - C.y);
				var angle = vec1.angleRad();
				addArc(inner, C.x, C.y, halfWidth, angle, angle + MathUtils.PI, false);
			} else {
				Joiner.prepareSquareEndpoint(B, C, D, E, halfWidth);
				outer.add(E.x);
				outer.add(E.y);

				inner.add(D.x);
				inner.add(D.y);
			}
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
				var bendsLeft =  Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, true);
				if(bendsLeft) {
					inner.add(E.x);
					inner.add(E.y);
				} else {
					outer.add(D.x);
					outer.add(D.y);
				}

				A.set(path.get(2), path.get(3));
				bendsLeft =  Joiner.prepareSmoothJoin(B, C, A, D, E, halfWidth, false);
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

	private Vector2 A1 = new Vector2();
	private Vector2 B1 = new Vector2();
	private Vector2 dir = new Vector2();

	private void addArc(List<Float> list, float centreX, float centreY, float radius, float startAngle, float endAngle, boolean clockwise){
		if(startAngle < 0) {
			startAngle += MathUtils.PI2;
		}

		if(endAngle < 0) {
			endAngle += MathUtils.PI2;
		}

		var sides = estimateSidesRequired(radius, radius);
		var deltaAngle = (endAngle + MathUtils.PI2 - startAngle) % MathUtils.PI2;
		if(clockwise) {
			deltaAngle = MathUtils.PI2 - deltaAngle;
		}

		var dAnglePerSide = deltaAngle / sides;
		var angle = startAngle;
		if(clockwise) {
			dAnglePerSide *= -1;
		}

		for (var i = 0; i<=sides; i++) {
			var cos = MathUtils.cos(angle);
			var sin = MathUtils.sin(angle);
			angle += dAnglePerSide;
			var x = centreX + cos * radius;
			var y = centreY + sin * radius;

			list.add(x);
			list.add(y);
			if(i == 0) {
				shapeRenderer.setColor(Color.YELLOW);
				pointAt(x, y);
			}
			if(i == sides) {
				shapeRenderer.setColor(Color.CYAN);
				pointAt(x, y);
			}
		}
	}


	private SideEstimator sideEstimator = new DefaultSideEstimator();
	protected int estimateSidesRequired(float radiusX, float radiusY) {
		return sideEstimator.estimateSidesRequired(drawer.getPixelSize(), radiusX, radiusY);
	}


	@Override
	public void dispose() {
		batch.dispose();
		image.dispose();
		shapeRenderer.dispose();
	}
}