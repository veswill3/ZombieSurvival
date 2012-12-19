package vesper.android.zombiesurvival;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import android.opengl.GLES20;

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
	public HUD getHUD(Camera pCamera, VertexBufferObjectManager pVertexBufferObjectManager) {
		// default control is an analog joy stick on the right side of the screen
		final float y2 = (float) (MainActivity.CAMERA_HEIGHT - (MainActivity._OnScreenControlBaseTextureRegion.getHeight() * 1.5));
		final float x2 = (float) (MainActivity.CAMERA_WIDTH - (MainActivity._OnScreenControlBaseTextureRegion.getWidth() * 1.5));
		final AnalogOnScreenControl weaponJoystick = new AnalogOnScreenControl(x2, y2, pCamera, MainActivity._OnScreenControlBaseTextureRegion,
				MainActivity._OnScreenControlKnobTextureRegion, 0.1f, pVertexBufferObjectManager, new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				if(pValueX == 0 && pValueY == 0) {
					// do nothing
				} else {
					discharge(pValueX, pValueY);
				}
			}
	
			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
				/* Nothing. */
			}
		});
		weaponJoystick.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		weaponJoystick.getControlBase().setAlpha(0.5f);
		return weaponJoystick;
	}

}
