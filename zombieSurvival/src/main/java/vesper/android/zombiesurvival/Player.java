package vesper.android.zombiesurvival;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import vesper.android.zombiesurvival.shared.IObjectWithHUD;
import vesper.android.zombiesurvival.weapon.Weapon;
import android.opengl.GLES20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;

/**
 * The main character of the game
 */
public class Player extends Character implements IObjectWithHUD {
	
	private Weapon mWeapon;
	private HUD mPlayerHUD;
	private HUD mWeaponHUD;

	public static final short MASKBITS_PLAYER = CATEGORYBIT_ENEMY + CATEGORYBIT_WALL + CATEGORYBIT_PLAYER;
	private final static FixtureDef mFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, false,
			CATEGORYBIT_PLAYER, MASKBITS_PLAYER, (short)0);
	
	private static final float PLAYER_SPEED = 8;
	private static final float PLAYER_WIDTH = 32;
	private static final float PLAYER_HEIGHT = 32;
	
	public Player(float pX, float pY, ITextureRegion pTextureRegion) {
		super(pX, pY, PLAYER_WIDTH, PLAYER_HEIGHT, pTextureRegion, mFixtureDef);
		setActive(true);
	}
	
	public void switchWeapon(Weapon newWeapon) {
		// remove current weapon
		if (mWeaponHUD != null) {
			mPlayerHUD.getChildScene().clearChildScene();
			mWeaponHUD.detachSelf();
		}
		// add new weapon
		if (newWeapon != null) {
			mWeapon = newWeapon;
			Camera camera = mPlayerHUD.getCamera();
			HUD weaponHUD = newWeapon.getHUD(camera, MainActivity._VBOM);
			if (weaponHUD != null) {
				mPlayerHUD.getChildScene().setChildScene(weaponHUD);
				weaponHUD.setCamera(camera);
			}
			mWeaponHUD = weaponHUD;
		}
	}

	@Override
	protected IUpdateHandler onCreateGameModeUpdateHanderl() {
		return null; // just wait for user to interact with player
	}
	
	@Override
	public HUD getHUD(Camera pCamera, VertexBufferObjectManager pVertexBufferObjectManager) {
		HUD playerHUD = new HUD(); // a HUD to attach everything related to the player to
		playerHUD.setCamera(pCamera);
		
		// create health meter
		Sprite health = new Sprite((pCamera.getWidth() - MainActivity._HealthTextureRegion.getWidth()) / 2, 10f, MainActivity._HealthTextureRegion, pVertexBufferObjectManager);
		playerHUD.attachChild(health);

		HUD playerControlHUD = createPlayerControl(pCamera, pVertexBufferObjectManager);

		// get the HUD from the weapon if it has one
		if (mWeapon != null) {
			HUD weaponHUD = mWeapon.getHUD(pCamera, pVertexBufferObjectManager);
			if (weaponHUD != null) {
				weaponHUD.setCamera(pCamera);
				// need to chain the HUDs, so set this as a child scene of player control
				playerControlHUD.setChildScene(weaponHUD);
			}
			mWeaponHUD = weaponHUD; // keep this around so we can manage it later
		}
		
		playerHUD.setChildScene(playerControlHUD);
		mPlayerHUD = playerHUD;
		return playerHUD;
	}
	
	private HUD createPlayerControl(Camera pCamera, VertexBufferObjectManager pVertexBufferObjectManager) {
		final float x = (float) (.5 * MainActivity._OnScreenControlBaseTextureRegion.getWidth());
		final float y = (float) (MainActivity.CAMERA_HEIGHT - (MainActivity._OnScreenControlBaseTextureRegion.getHeight() * 1.5));
		final AnalogOnScreenControl playerJoystick = new AnalogOnScreenControl(x, y, pCamera, MainActivity._OnScreenControlBaseTextureRegion, 
				MainActivity._OnScreenControlKnobTextureRegion, 0.1f, pVertexBufferObjectManager,
				new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				final Body body = getBody();
				final Vector2 velocity = Vector2Pool.obtain(pValueX * PLAYER_SPEED, pValueY * PLAYER_SPEED);
				body.setLinearVelocity(velocity);
				Vector2Pool.recycle(velocity);
			}
	
			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
				/* Nothing. */
			}
		});
		playerJoystick.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		playerJoystick.getControlBase().setAlpha(0.5f);
		return playerJoystick;
	}

}
