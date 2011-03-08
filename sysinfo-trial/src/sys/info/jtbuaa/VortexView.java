package sys.info.jtbuaa;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class VortexView extends GLSurfaceView {
    private VortexRenderer _renderer;
    
    public VortexView(Context context) {
        super(context);
        _renderer = new VortexRenderer();
        setRenderer(_renderer);
    }
    
	public String getGlVender() {
        return _renderer.glVender.trim();
	}
}
