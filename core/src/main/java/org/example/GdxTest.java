package org.example;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import space.earlygrey.shapedrawer.*;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GdxTest extends ApplicationAdapter {
	private PolygonSpriteBatch batch;
	private Texture image;

	private ShapeDrawer drawer;

	private OrthographicCamera cam;

	private TextureRegion region;
	private Texture onePixel;

	@Override
	public void create() {
		batch = new PolygonSpriteBatch();
		image = new Texture("libgdx.png");
		image.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.drawPixel(0, 0);
		onePixel = new Texture(pixmap);
		pixmap.dispose();
		TextureRegion pixel = new TextureRegion(onePixel, 0, 0, 1, 1);
		drawer = new ShapeDrawer(batch, pixel);
		region = new TextureRegion(image);

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		// Constructs a new OrthographicCamera, using the given viewport width and height
		// Height is multiplied by aspect ratio.
		cam = new OrthographicCamera(w, h);

		cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
		cam.update();
	}

	private FloatArray _path = new FloatArray();
	private JoinType _type = JoinType.Round;

	private boolean _open = true;

	private boolean _draw = false;

	private boolean _printDebug = true;

	private float[] _calcPath;

	@Override
	public void render() {
		handleInput();
		Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		cam.update();
		batch.setProjectionMatrix(cam.combined);

	/*	var _path = FloatArray.with(new float[] {
				150,550,
				550,500,
				550,200,
				150,150,
		});
*/
		if(_path.size == 0)
			return;

		batch.begin();
		float wall = 100;
		_calcPath = path(_path, wall, _type, _open);

		//_calcPath = new float[] {0f, 0f, 0f, 600f, 600f, 600f, 600f, 0f};
		var triag = new EarClippingTriangulator();
		var indices = triag.computeTriangles(_calcPath).toArray();
		PolygonRegion polyReg = new PolygonRegion(region, _calcPath, indices);
		if(_draw) {
			batch.draw(polyReg, 0, 0);
		}
	//	var ps = new PolygonSprite(polyReg);
	//	ps.draw(batch);



		// draw the triangles of sprite
		if(_printDebug) {
			drawer.setColor(Color.CYAN);
			for (int j = 0; j < indices.length; j += 3) {
				float x1 = _calcPath[2*indices[j]];
				float y1 = _calcPath[2*indices[j] + 1];
				float x2 = _calcPath[2*indices[j + 1]];
				float y2 = _calcPath[2*indices[j + 1] + 1];
				float x3 = _calcPath[2*indices[j + 2]];
				float y3 = _calcPath[2*indices[j + 2] + 1];
				drawer.triangle(x1, y1, x2, y2, x3, y3);
			}
			drawer.setColor(Color.GREEN);
			for (int j = 0; j < _calcPath.length; j += 2) {
				float x1 = _calcPath[j];
				float y1 = _calcPath[j + 1];

				if(j+2 >= _calcPath.length)
					break;
				float x2 = _calcPath[j + 2];
				float y2 = _calcPath[j + 3];
				drawer.line(x1, y1, x2, y2);
			}
		}
		batch.end();
	}

	private final Vector3 mouseInWorld3D = new Vector3();

	private void createSprite() {
	//	sprite = new RepeatablePolygonSprite();
	//	sprite.setTextureRegion(region);
	//	sprite.setDensity(100);
	}

	private void handleInput() {
		if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)){
			mouseInWorld3D.x = Gdx.input.getX();
			mouseInWorld3D.y = Gdx.input.getY();
			mouseInWorld3D.z = 0;
			cam.unproject(mouseInWorld3D);
			_path.add(mouseInWorld3D.x, mouseInWorld3D.y);
			createSprite();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			_path.clear();
			createSprite();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
			_type = JoinType.Pointy;
			createSprite();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
			_type = JoinType.Smooth;
			createSprite();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
			_type = JoinType.Round;
			createSprite();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			_open = !_open;
			createSprite();
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT)) {
			_draw = !_draw;
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT)) {
			_printDebug = !_printDebug;
		}
	}

	@Override
	public void resize(int width, int height) {
		cam.viewportWidth = width;
		cam.viewportHeight = height;
		cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
		cam.update();
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

		if(path.size == 2) {
			var x = path.get(0);
			var y = path.get(1);
			if(joinType == JoinType.Round) {
				addArc(outer, x, y,  halfWidth, 0, MathUtils.PI2-0.1f, false);
			} else {
				outer.add(x-halfWidth);
				outer.add(y-halfWidth);
				outer.add(x-halfWidth);
				outer.add(y+halfWidth);
				outer.add(x+halfWidth);
				outer.add(y+halfWidth);
				outer.add(x+halfWidth);
				outer.add(y-halfWidth);
			}

		} else if(path.size == 4) {
			A.set(path.get(0), path.get(1));
			B.set(path.get(2), path.get(3));
			if(joinType == JoinType.Round) {
				Joiner.prepareFlatEndpoint(B, A, D, E, halfWidth);
				E0.set(D);
				outer.add(D.x);
				outer.add(D.y);
				vec1.set(D).add(-A.x, -A.y);
				var angle = vec1.angleRad();
				addArc(outer, A.x, A.y, halfWidth, angle, angle + MathUtils.PI, false);
				outer.add(E.x);
				outer.add(E.y);

				Joiner.prepareFlatEndpoint(A, B, D, E, halfWidth);
				outer.add(D.x);
				outer.add(D.y);
				vec1.set(D).add(-B.x, -B.y);
				angle = vec1.angleRad();
				addArc(outer, B.x, B.y, halfWidth, angle, angle + MathUtils.PI, false);
				outer.add(E.x);
				outer.add(E.y);
				outer.add(E0.x);
				outer.add(E0.y);
			} else {
				Joiner.prepareSquareEndpoint(B, A, D, E, halfWidth);
				E0.set(D);
				outer.add(D.x);
				outer.add(D.y);
				outer.add(E.x);
				outer.add(E.y);
				Joiner.prepareSquareEndpoint(A, B, D, E, halfWidth);
				outer.add(D.x);
				outer.add(D.y);
				outer.add(E.x);
				outer.add(E.y);
				outer.add(E0.x);
				outer.add(E0.y);
			}

		} else {
			for (int i = 2; i < path.size - 2; i += 2) {
				A.set(path.get(i - 2), path.get(i - 1));
				B.set(path.get(i), path.get(i + 1));
				C.set(path.get(i + 2), path.get(i + 3));
				if (i == 2) {
					if (open) {
						if (joinType == JoinType.Round) {
							Joiner.prepareFlatEndpoint(B, A, D, E, halfWidth);
							outer.add(D.x);
							outer.add(D.y);
							vec1.set(D).add(-A.x, -A.y);
							var angle = vec1.angleRad();
							addArc(inner, A.x, A.y, halfWidth, angle, angle + MathUtils.PI, false);
							inner.add(E.x);
							inner.add(E.y);
						} else {
							Joiner.prepareSquareEndpoint(B, A, D, E, halfWidth);
							outer.add(D.x);
							outer.add(D.y);

							// add link at start
						//	inner.add(D.x);
						//	inner.add(D.y);

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
					if (bendsLeft) {
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
					if (bendsLeft) {
						if (joinType == JoinType.Round) {
							AB.set(B).sub(A);
							BC.set(C).sub(B);
							vec1.add(-B.x, -B.y);
							var angle = vec1.angleRad();
							var angleDiff = MathUtils.PI2 - ShapeUtils.angleRad(AB, BC);
							addArc(inner, B.x, B.y, halfWidth, angle, angle + angleDiff, false);
						}
						inner.add(E.x);
						inner.add(E.y);
					} else {
						if (joinType == JoinType.Round) {
							AB.set(B).sub(A);
							BC.set(C).sub(B);
							vec1.add(-B.x, -B.y);
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
				if (joinType == JoinType.Round) {
					Joiner.prepareFlatEndpoint(B, C, D, E, halfWidth);
					outer.add(E.x);
					outer.add(E.y);
					inner.add(D.x);
					inner.add(D.y);
					vec1.set(D).add(-C.x, -C.y);
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
					var bendsLeft = Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
					if (bendsLeft) {
						vec1.set(E);
					} else {
						vec1.set(D);
					}
					outer.add(D.x);
					outer.add(D.y);
					inner.add(E.x);
					inner.add(E.y);

					//draw connection back to first vertex
					Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, true);
					if (bendsLeft) {
						if (joinType == JoinType.Round) {
							AB.set(B).sub(A);
							BC.set(C).sub(B);
							vec1.add(-B.x, -B.y);
							var angle = vec1.angleRad();
							var angleDiff = MathUtils.PI2 - ShapeUtils.angleRad(AB, BC);
							addArc(inner, B.x, B.y, halfWidth, angle, angle + angleDiff, false);
						}
						inner.add(E.x);
						inner.add(E.y);
					} else {
						if (joinType == JoinType.Round) {
							AB.set(B).sub(A);
							BC.set(C).sub(B);
							vec1.add(-B.x, -B.y);
							var angle = vec1.angleRad();
							var angleDiff = MathUtils.PI2 - ShapeUtils.angleRad(AB, BC);
							addArc(outer, B.x, B.y, halfWidth, angle, angle + angleDiff, true);
						}
						outer.add(D.x);
						outer.add(D.y);
					}

					A.set(B);
					B.set(C);
					C.set(path.get(2), path.get(3));
					bendsLeft = Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
					if (bendsLeft) {
						vec1.set(E);
					} else {
						vec1.set(D);
					}
					outer.add(D.x);
					outer.add(D.y);
					inner.add(E.x);
					inner.add(E.y);

					if (joinType == JoinType.Round) {
							AB.set(B).sub(A);
							BC.set(C).sub(B);
							vec1.add(-B.x, -B.y);
							var angle = vec1.angleRad();
							var angleDiff = MathUtils.PI2 - ShapeUtils.angleRad(AB, BC);
							if (bendsLeft) {
								addArc(inner, B.x, B.y, halfWidth, angle, angle + angleDiff, false);
								inner.add(E0.x);
								inner.add(E0.y);
							} else {
								addArc(outer, B.x, B.y, halfWidth, angle, angle + angleDiff, true);
								outer.add(D0.x);
								outer.add(D0.y);
							}
					} else {
						if (bendsLeft) {
							inner.add(E0.x);
							inner.add(E0.y);
						} else {
							outer.add(D0.x);
							outer.add(D0.y);
						}
					}

				}
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

		var deltaAngle = (endAngle + MathUtils.PI2 - startAngle) % MathUtils.PI2;
		if(clockwise) {
			deltaAngle = MathUtils.PI2 - deltaAngle;
		}
		var sides = estimateSidesRequired(radius, radius);
		sides *= deltaAngle/MathUtils.PI2;

		var dAnglePerSide = deltaAngle / sides;
		var angle = startAngle;
		angle += dAnglePerSide;
		sides -= 1;
		if(clockwise) {
			dAnglePerSide *= -1;
			angle += 2*dAnglePerSide;

		}

		drawer.setColor(Color.WHITE_FLOAT_BITS);
		for (var i = 1; i<=sides; i++) {
		/*	if(i>1) {
				drawer.setColor(Color.RED);
			}
		*/	var cos = MathUtils.cos(angle);
			var sin = MathUtils.sin(angle);
			angle += dAnglePerSide;
			var x = centreX + cos * radius;
			var y = centreY + sin * radius;

			list.add(x);
			list.add(y);
		//	drawer.circle(x,y,2);
		}
	}


	private SideEstimator sideEstimator = new DefaultSideEstimator();
	protected int estimateSidesRequired(float radiusX, float radiusY) {
		//return 12;
		return sideEstimator.estimateSidesRequired(drawer.getPixelSize(), radiusX, radiusY);
	}


	@Override
	public void dispose() {
		batch.dispose();
		image.dispose();
		onePixel.dispose();
	}
}