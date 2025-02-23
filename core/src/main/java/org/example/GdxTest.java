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
import com.badlogic.gdx.utils.ShortArray;
import space.earlygrey.shapedrawer.*;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms.
 */
public class GdxTest extends ApplicationAdapter {
    private static class TriangledPolygon {
        float[] vertices;
        short[] indices;
    }

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

        cam = new OrthographicCamera(w, h);

        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
    }

    private FloatArray _path = new FloatArray();
    private JoinType _type = JoinType.Round;

    private boolean _open = true;

    private boolean _draw = false;

    private boolean _printDebug = true;

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
        if (_path.size == 0)
            return;

        batch.begin();
        float wall = 100;
        drawPathWithJoin(_path, wall, _type, _open);

        var indices = indices2.toArray();
        var vertices = vertices2.toArray();

        PolygonRegion polyReg = new PolygonRegion(region, vertices, indices);
        if (_draw) {
            batch.draw(polyReg, 0, 0);
        }


        // draw the triangles of sprite
        if (_printDebug) {
            drawer.setColor(Color.CYAN);
            for (int j = 0; j < indices.length; j += 3) {
                float x1 = vertices[2 * indices[j]];
                float y1 = vertices[2 * indices[j] + 1];
                float x2 = vertices[2 * indices[j + 1]];
                float y2 = vertices[2 * indices[j + 1] + 1];
                float x3 = vertices[2 * indices[j + 2]];
                float y3 = vertices[2 * indices[j + 2] + 1];
                drawer.triangle(x1, y1, x2, y2, x3, y3);
            }
            drawer.setColor(Color.RED);
            for (int j = 0; j < vertices.length; j += 2) {
                float x1 = vertices[j];
                float y1 = vertices[j + 1];
                drawer.circle(x1, y1, 4);
            }
        }
        batch.end();
    }

    private final Vector3 mouseInWorld3D = new Vector3();

    private void handleInput() {
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            mouseInWorld3D.x = Gdx.input.getX();
            mouseInWorld3D.y = Gdx.input.getY();
            mouseInWorld3D.z = 0;
            cam.unproject(mouseInWorld3D);
            _path.add(mouseInWorld3D.x, mouseInWorld3D.y);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            _path.clear();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            _type = JoinType.Pointy;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            _type = JoinType.Smooth;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            _type = JoinType.Round;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            _open = !_open;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ALT_LEFT)) {
            _draw = !_draw;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT)) {
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

    private void addArc(FloatArray vertices, ShortArray indices, float centreX, float centreY, float radius, float startAngle, float endAngle, boolean clockwise) {
        var oldSize = vertices.size;
        var oldVertexCount = oldSize / 2;

        if (startAngle < 0) {
            startAngle += MathUtils.PI2;
        }

        if (endAngle < 0) {
            endAngle += MathUtils.PI2;
        }

        var deltaAngle = (endAngle + MathUtils.PI2 - startAngle) % MathUtils.PI2;
        if (clockwise) {
            deltaAngle = MathUtils.PI2 - deltaAngle;
        }
        var sides = estimateSidesRequired(radius, radius);
        sides *= deltaAngle / MathUtils.PI2;

        var dAnglePerSide = deltaAngle / sides;
        var angle = startAngle;
        angle += dAnglePerSide;
        sides -= 1;
        if (clockwise) {
            dAnglePerSide *= -1;
            angle += 2 * dAnglePerSide;

        }

        drawer.setColor(Color.WHITE_FLOAT_BITS);
        for (var i = 1; i <= sides; i++) {
		/*	if(i>1) {
				drawer.setColor(Color.RED);
			}
		*/
            var cos = MathUtils.cos(angle);
            var sin = MathUtils.sin(angle);
            angle += dAnglePerSide;
            var x = centreX + cos * radius;
            var y = centreY + sin * radius;

            vertices.add(x);
            vertices.add(y);
            //	drawer.circle(x,y,2);
        }
        var vertexCount = (vertices.size - oldSize) / 2;

        for (int j = 0; j < vertexCount; j++) {
            indices.add(oldVertexCount - 1);
            indices.add(oldVertexCount + j);
            indices.add(oldVertexCount + j + 1);
        }
    }


    private SideEstimator sideEstimator = new DefaultSideEstimator();

    protected int estimateSidesRequired(float radiusX, float radiusY) {
        //return 12;
        return sideEstimator.estimateSidesRequired(drawer.getPixelSize(), radiusX, radiusY);
    }


    Vector2 vert1 = new Vector2();
    Vector2 vert2 = new Vector2();
    Vector2 vert3 = new Vector2();
    Vector2 vert4 = new Vector2();

    TriangledPolygon result = new TriangledPolygon();

    void pushQuad() {
        var index = vertices2.size / 2;
        vertices2.add(vert1.x);
        vertices2.add(vert1.y);
        vertices2.add(vert2.x);
        vertices2.add(vert2.y);
        vertices2.add(vert3.x);
        vertices2.add(vert3.y);
        vertices2.add(vert4.x);
        vertices2.add(vert4.y);
        indices2.add(index);
        indices2.add(index + 1);
        indices2.add(index + 2);
        indices2.add(index);
        indices2.add(index + 2);
        indices2.add(index + 3);
    }

    void pushTriangle() {
        var index = vertices2.size / 2;
        vertices2.add(vert1.x);
        vertices2.add(vert1.y);
        vertices2.add(vert2.x);
        vertices2.add(vert2.y);
        vertices2.add(vert3.x);
        vertices2.add(vert3.y);
        indices2.add(index);
        indices2.add(index + 1);
        indices2.add(index + 2);
    }

    FloatArray vertices2 = new FloatArray();
    ShortArray indices2 = new ShortArray();

    private void drawPathWithJoin(FloatArray path, float lineWidth, JoinType joinType, boolean open) {
        float halfWidth = lineWidth / 2f;
        boolean pointyJoin = joinType == JoinType.Pointy;

        vertices2.clear();
        indices2.clear();

        if (path.size == 2) {
            var x = path.get(0);
            var y = path.get(1);
            if (joinType == JoinType.Round) {
                vertices2.add(x + halfWidth, y);
                addArc(vertices2, indices2, x, y, halfWidth, 0, MathUtils.PI2 - 0.1f, false);
                vertices2.add(x + halfWidth, y);
            } else {
                vert1.set(x - halfWidth, y - halfWidth);
                vert2.set(x - halfWidth, y + halfWidth);
                vert3.set(x + halfWidth, y + halfWidth);
                vert4.set(x + halfWidth, y - halfWidth);
                pushQuad();
            }
            return;
        }

        if (path.size == 4) {
            A.set(path.get(0), path.get(1));
            B.set(path.get(2), path.get(3));


            if (joinType == JoinType.Round) {
                Joiner.prepareFlatEndpoint(B, A, D, E, halfWidth);

                vertices2.add(D.x);
                vertices2.add(D.y);
                vec1.set(D).add(-A.x, -A.y);
                var angle = vec1.angleRad();
                addArc(vertices2, indices2, A.x, A.y, halfWidth, angle, angle + MathUtils.PI, false);
                vertices2.add(E.x);
                vertices2.add(E.y);

                vert1.set(D);
                vert2.set(E);

                Joiner.prepareFlatEndpoint(A, B, D, E, halfWidth);
                vertices2.add(D.x);
                vertices2.add(D.y);
                vec1.set(D).add(-B.x, -B.y);
                angle = vec1.angleRad();
                addArc(vertices2, indices2, B.x, B.y, halfWidth, angle, angle + MathUtils.PI, false);

                vertices2.add(E.x);
                vertices2.add(E.y);

                vert3.set(D);
                vert4.set(E);
                pushQuad();

            } else {
                Joiner.prepareSquareEndpoint(B, A, D, E, halfWidth);
                E0.set(D);
                vert1.set(D);
                vert2.set(E);

                Joiner.prepareSquareEndpoint(A, B, D, E, halfWidth);
                vert3.set(D);
                vert4.set(E);
                pushQuad();
            }
            return;
        }

        for (int i = 2; i < path.size - 2; i += 2) {
            A.set(path.get(i - 2), path.get(i - 1));
            B.set(path.get(i), path.get(i + 1));
            C.set(path.get(i + 2), path.get(i + 3));

            if (pointyJoin) {
                Joiner.preparePointyJoin(A, B, C, D, E, halfWidth);
            } else {
                Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
            }
            vert3.set(D);
            vert4.set(E);

            if (i == 2) {
                if (open) {
                    Joiner.prepareSquareEndpoint(B, A, D, E, halfWidth);
                    if (joinType == JoinType.Round) {
                        vec1.set(B).sub(A).setLength(halfWidth);
                        D.add(vec1);
                        E.add(vec1);
                        vertices2.add(D.x);
                        vertices2.add(D.y);
                        vec1.set(D).add(-A.x, -A.y);
                        var angle = vec1.angleRad();
                        addArc(vertices2, indices2, A.x, A.y, halfWidth, angle, angle + MathUtils.PI, false);
                        vertices2.add(E.x);
                        vertices2.add(E.y);
                    }

                    vert1.set(E);
                    vert2.set(D);

                } else {
                    vec1.set(path.get(path.size - 2), path.get(path.size - 1));
                    if (pointyJoin) {
                        Joiner.preparePointyJoin(vec1, A, B, D0, E0, halfWidth);
                    } else {
                        Joiner.prepareSmoothJoin(vec1, A, B, D0, E0, halfWidth, true);
                    }
                    vert1.set(E0);
                    vert2.set(D0);
                }
            }

            float x3, y3, x4, y4;
            if (pointyJoin) {
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

            pushQuad();
            if (!pointyJoin) drawSmoothJoinFill(vertices2, indices2, A, B, C, D, E, halfWidth, joinType);
            vert1.set(x4, y4);
            vert2.set(x3, y3);
        }


        if (open) {
            //draw last link on path
            Joiner.prepareFlatEndpoint(B, C, D, E, halfWidth);
            if (joinType == JoinType.Round) {

                vertices2.add(D.x);
                vertices2.add(D.y);
                vec1.set(D).add(-C.x, -C.y);
                var angle = vec1.angleRad();
                addArc(vertices2, indices2, C.x, C.y, halfWidth, angle, angle + MathUtils.PI, false);
                vertices2.add(E.x);
                vertices2.add(E.y);
            } else {
                vec1.set(C).sub(B).setLength(halfWidth);
                D.add(vec1);
                E.add(vec1);
            }

            vert3.set(E);
            vert4.set(D);
            pushQuad();
        } else {
            if (pointyJoin) {
                //draw last link on path
                A.set(path.get(0), path.get(1));
                Joiner.preparePointyJoin(B, C, A, D, E, halfWidth);
                vert3.set(D);
                vert4.set(E);
                pushQuad();

                //draw connection back to first vertex
                vert1.set(D);
                vert2.set(E);
                vert3.set(E0);
                vert4.set(D0);
                pushQuad();
            } else {
                //draw last link on path
                A.set(B);
                B.set(C);
                C.set(path.get(0), path.get(1));
                Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, false);
                vert3.set(D);
                vert4.set(E);
                pushQuad();
                drawSmoothJoinFill(vertices2, indices2,A, B, C, D, E, halfWidth, joinType);

                //draw connection back to first vertex
                Joiner.prepareSmoothJoin(A, B, C, D, E, halfWidth, true);
                vert3.set(E);
                vert4.set(D);
                A.set(path.get(2), path.get(3));
                Joiner.prepareSmoothJoin(B, C, A, D, E, halfWidth, false);
                vert1.set(D);
                vert2.set(E);
                pushQuad();
                drawSmoothJoinFill(vertices2, indices2,B, C, A, D, E, halfWidth, joinType);
            }
        }
    }

    void drawSmoothJoinFill(FloatArray vertices, ShortArray indices, Vector2 A, Vector2 B, Vector2 C, Vector2 D, Vector2 E, float halfLineWidth, JoinType joinType) {
        boolean bendsLeft = Joiner.prepareSmoothJoin(A, B, C, D, E, halfLineWidth, false);
        vert1.set(bendsLeft ? E : D);
        vert2.set(bendsLeft ? D : E);
        if (bendsLeft) {
            vec1.set(E);
        } else {
            vec1.set(D);
        }

        bendsLeft = Joiner.prepareSmoothJoin(A, B, C, D, E, halfLineWidth, true);
        vert3.set(bendsLeft ? E : D);
        pushTriangle();

        if (joinType == JoinType.Round) {
            if(bendsLeft) {
                AB.set(B).sub(A);
                BC.set(C).sub(B);
                vec1.add(-B.x, -B.y);
                var angle = vec1.angleRad();
                var angleDiff = MathUtils.PI2 - ShapeUtils.angleRad(AB, BC);
                vertices.add(vert1.x);
                vertices.add(vert1.y);
                addArc(vertices, indices, B.x, B.y, halfLineWidth, angle, angle + angleDiff, false);
                vertices.add(vert3.x);
                vertices.add(vert3.y);
            } else {
                AB.set(B).sub(A);
                BC.set(C).sub(B);
                vec1.add(-B.x, -B.y);
                var angle = vec1.angleRad();
                var angleDiff = MathUtils.PI2 - ShapeUtils.angleRad(AB, BC);
                vertices.add(vert1.x);
                vertices.add(vert1.y);
                addArc(vertices,indices, B.x, B.y, halfLineWidth, angle, angle + angleDiff, true);
                vertices.add(vert3.x);
                vertices.add(vert3.y);
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
        onePixel.dispose();
    }
}