package vesper.android.zombiesurvival.weapon;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import vesper.android.zombiesurvival.Character;
import vesper.android.zombiesurvival.shared.IObjectWithHUD;

/**
 * Base class that all weapons inherit from
 */
public abstract class Weapon implements IObjectWithHUD {

	protected final Character mParent;
	
	public Weapon(final Character pParent) {
		mParent = pParent; // so we know who is holding the weapon
	}
	
	/**
	 * Discharge the weapon
	 * @param x coordinate of the discharge
	 * @param y coordinate of the discharge
	 */
	protected abstract void discharge(float x, float y);
	
	@Override
	public abstract HUD getHUD(Camera pCamera, VertexBufferObjectManager pVertexBufferObjectManager);

}
