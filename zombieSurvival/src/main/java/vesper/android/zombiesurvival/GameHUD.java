package vesper.android.zombiesurvival;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import vesper.android.zombiesurvival.shared.IObjectWithHUD;

/**
 * HUD to be displayed during game play
 */
public class GameHUD extends HUD {
	
	public GameHUD(final Camera pCamera, final VertexBufferObjectManager pVertexBufferObjectManager, IObjectWithHUD pObjWithHUD){
		super();
		this.setCamera(pCamera);
		
		// load controlling object specific HUD (for onscreen controls)
		HUD objControlsHUD = pObjWithHUD.getHUD(pCamera, pVertexBufferObjectManager);
		if (objControlsHUD != null) {
			this.setChildScene(objControlsHUD);
		}
		
	}
	
}
