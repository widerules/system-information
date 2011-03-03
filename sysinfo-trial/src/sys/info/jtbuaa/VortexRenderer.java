package sys.info.jtbuaa;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView;
import android.util.Log;

public class VortexRenderer implements GLSurfaceView.Renderer {
    public String glVender = "";

    private static final String LOG_TAG = VortexRenderer.class.getSimpleName();
    
    private float _red = 0.9f;
    private float _green = 0.2f;
    private float _blue = 0.2f;
    
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //show vendor of GPU. if vendor is "android", then no real GPU. otherwise it has real GPU
        glVender = gl.glGetString(GL10.GL_VENDOR);
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
        gl.glViewport(0, 0, w, h);
    }

    public void onDrawFrame(GL10 gl) {
        // define the color we want to be displayed as the "clipping wall"
        gl.glClearColor(_red, _green, _blue, 1.0f);
        // clear the color buffer to show the ClearColor we called above...
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
    }

    public void setColor(float r, float g, float b) {
        _red = r;
        _green = g;
        _blue = b;
    }
}
