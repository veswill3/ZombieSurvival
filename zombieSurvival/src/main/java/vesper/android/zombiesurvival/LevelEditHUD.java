package vesper.android.zombiesurvival;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

public class LevelEditHUD extends HUD {

	public LevelEditHUD(final Camera pCamera, final BaseGameActivity pActivity, final VertexBufferObjectManager pVertexBufferObjectManager) {
		super();
		this.setCamera(pCamera);
		
	}

}
