package vesper.android.zombiesurvival.weapon;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import vesper.android.zombiesurvival.Character;
import vesper.android.zombiesurvival.MainActivity;
import android.opengl.GLES20;

import com.badlogic.gdx.math.Vector2;

public class SubMachineGun extends Weapon {

//	private static final int MAX_AMMO = 50;
//	private int mAmmo;

	public SubMachineGun(final Character pParent) {
		super(pParent);
//		this.mAmmo = MAX_AMMO;
	}

	@Override
	public HUD getHUD(Camera pCamera, VertexBufferObjectManager pVertexBufferObjectManager) {
		// control is an analog joy stick on the right side of the screen
		final float y2 = (float) (MainActivity.CAMERA_HEIGHT - (MainActivity._OnScreenControlBaseTextureRegion.getHeight() * 1.5));
		final float x2 = (float) (MainActivity.CAMERA_WIDTH - (MainActivity._OnScreenControlBaseTextureRegion.getWidth() * 1.5));
		final AnalogOnScreenControl weaponJoystick = new AnalogOnScreenControl(x2, y2, pCamera, MainActivity._OnScreenControlBaseTextureRegion,
				MainActivity._OnScreenControlKnobTextureRegion, 0.1f, pVertexBufferObjectManager, new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				if(pValueX == 0 && pValueY == 0) {
					// do nothing
				} else {
                    Character parent = mParent;
                    Bullet b = MainActivity._BulletPool.obtain(parent.getCenterX(), parent.getCenterY(), new Vector2(pValueX, pValueY));
                    parent.getParent().attachChild(b);
				}
			}

			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
				/* Nothing */
			}
		});
		weaponJoystick.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		weaponJoystick.getControlBase().setAlpha(0.5f);
		return weaponJoystick;
	}

}
