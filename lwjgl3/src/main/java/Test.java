import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;

public class Test extends ApplicationAdapter {
    public void create () {
        // your code here
    }
    private boolean written = false;
    public void render () {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        var clip = new Polygon(new float[] {94.5f, 0.0f, 94.5f, 63.0f, 189.0f, 63.0f, 189.0f, 0.0f});
        var toClip = new Polygon(new float[] {0.0f, 0.0f, 0.0f, 200.0f, 310.0f, 200.0f, 310.0f, 0.0f, 210.0f, 0.0f, 210.0f, 100.0f, 100.0f, 100.0f, 100.0f, 0.0f});
        var intersect = new Polygon();
        Intersector.intersectPolygons(toClip, clip, intersect);
        // intersect is [94.5, 0.0, 94.5, 63.0, 189.0, 63.0, 100.0, 63.0, 100.0, 0.0] instead of
        // [94.5, 0.0, 94.5, 63.0, 100.0, 63.0, 100.0, 0.0]
        if(written)
            return;

        var verts = intersect.getVertices();
        for(int i = 0; i<verts.length; i+=2)
        {
            written = true;
            System.out.println("(" + verts[i] + ", " + verts[i+1] + ")" );
        }
    }

    public static void main (String[] args) throws Exception {
        new Lwjgl3Application(new Test());
    }
}