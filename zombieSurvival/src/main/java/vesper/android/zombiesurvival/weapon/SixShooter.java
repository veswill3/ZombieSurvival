package vesper.android.zombiesurvival.weapon;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import vesper.android.zombiesurvival.Character;
import vesper.android.zombiesurvival.MainActivity;

import com.badlogic.gdx.math.Vector2;

public class SixShooter extends Weapon {

	public SixShooter(Character pParent) {
		super(pParent);
	}

	@Override
	protected void discharge(float pX, float pY) {
		final Character parent = mParent;
		// point where the user pressed - player
		Vector2 direction = new Vector2(pX, pY).sub(parent.getCenterX(),
				parent.getCenterY());
		Bullet b = MainActivity._BulletPool.obtain(parent.getCenterX(),
				parent.getCenterY(), direction);
		mParent.getParent().attachChild(b);
	}

	@Override
	public HUD getHUD(final Camera pCamera,
			VertexBufferObjectManager pVertexBufferObjectManager) {
		// TODO add a visual representation of the six shooters cylinder

		final Rectangle tapToShootArea = new Rectangle(0, 0,
				pCamera.getWidth(), pCamera.getHeight(), MainActivity._VBOM) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionUp()) {
					// convert from local to world coordinates
					float x = pSceneTouchEvent.getX() + pCamera.getXMin();
					float y = pSceneTouchEvent.getY() + pCamera.getYMin();
					discharge(x, y);

					return true;
				}
				return false;
			}
		};
		final HUD sixShooterHUD = new HUD();
		sixShooterHUD.setCamera(pCamera);
		sixShooterHUD.registerTouchArea(tapToShootArea);
		sixShooterHUD.attachChild(tapToShootArea);
		tapToShootArea.setAlpha(0);
		return sixShooterHUD;
	}

}
