package vesper.android.zombiesurvival;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Add to an object that has something to display in a HUD
 * e.g. joystick, button, or health
 */
public interface IObjectWithHUD {
	
	/**
	 * Get HUD from an object that has something to display
	 * This could be information or for user interaction
	 * @param pCamera
	 * @param pVertexBufferObjectManager
	 * @return HUD for the object
	 */
	public HUD getHUD(final Camera pCamera, final VertexBufferObjectManager pVertexBufferObjectManager);

}
