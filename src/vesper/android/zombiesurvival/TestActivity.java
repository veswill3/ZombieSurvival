package vesper.android.zombiesurvival;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.widget.Toast;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class TestActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener, IOnAreaTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private Camera mCamera;
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	
	private BitmapTextureAtlas mOnScreenControlTexture;
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;

	private ITextureRegion mAndroidTextureRegion;
	private ITextureRegion mZombieTextureRegion;
	
	private Sprite mAndroid; //handle to "player" - android sprite

	private PhysicsWorld mPhysicsWorld;

	private float mGravityX;
	private float mGravityY;

	private Scene mScene;

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {
		Toast.makeText(this, "Touch the screen to add objects. Touch an object to shoot it up into the air.", Toast.LENGTH_LONG).show();

		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 32, 64, TextureOptions.BILINEAR);
		this.mAndroidTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "Android.png", 0, 0);
		this.mZombieTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "Zombie.png", 0, 32, 1, 1);
		this.mBitmapTextureAtlas.load();
		
		this.mOnScreenControlTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
		this.mOnScreenControlTexture.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0, 0, 0));
		this.mScene.setOnSceneTouchListener(this);

		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2 , CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		
		/*
		 *                  _______
		 *                  |     |
		 *    ______________|_lenB|
		 *    |                   |hB
		 *  hA|________lenA_______|
		 * 
		 */
		
		int lenA = CAMERA_WIDTH / 2;
		int lenB = CAMERA_WIDTH / 8;
		int hA = CAMERA_HEIGHT / 5;
		int hB = CAMERA_HEIGHT / 3;
		
		int xA = CAMERA_WIDTH / 6;
		int yA = CAMERA_HEIGHT / 6;
		int xB = xA + lenA - lenB;
		int yB = yA; // so they overlap. Otherwise bodies would get stuck in the crack
		
		final Rectangle midLong = new Rectangle(xA, yA, lenA, hA, vertexBufferObjectManager);
		final Rectangle midTall = new Rectangle(xB, yB, lenB, hB, vertexBufferObjectManager);
		
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
		
		PhysicsFactory.createBoxBody(mPhysicsWorld, midLong, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(mPhysicsWorld, midTall, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		
		this.mScene.attachChild(midLong);
		this.mScene.attachChild(midTall);

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		this.mScene.setOnAreaTouchListener(this);
		
		// create android in middle of screen
		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
		final Sprite androidSprite = new Sprite(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, this.mAndroidTextureRegion, this.getVertexBufferObjectManager());
		mAndroid = androidSprite;
		final Body body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, androidSprite, BodyType.DynamicBody, objectFixtureDef);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(androidSprite, body, true, true));
		androidSprite.setUserData(body);
		this.mScene.registerTouchArea(androidSprite);
		this.mScene.attachChild(androidSprite);

		this.initOnScreenControls();
		
		return this.mScene;
	}

	private void initOnScreenControls() {
		final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(0, CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight(), this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				final Body carBody = (Body)TestActivity.this.mAndroid.getUserData();
				final Vector2 velocity = Vector2Pool.obtain(pValueX * 200, pValueY * 200);
				if (velocity.len2() > 1) {
					carBody.setLinearVelocity(velocity);
				}
				Vector2Pool.recycle(velocity);

//				final float rotationInRad = (float)Math.atan2(-pValueX, pValueY);
//				carBody.setTransform(carBody.getWorldCenter(), rotationInRad);

//				TestActivity.this.mAndroid.setRotation(MathUtils.radToDeg(rotationInRad));
			}

			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
				/* Nothing. */
			}
		});
		analogOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		analogOnScreenControl.getControlBase().setAlpha(0.5f);
		//		analogOnScreenControl.getControlBase().setScaleCenter(0, 128);
		//		analogOnScreenControl.getControlBase().setScale(0.75f);
		//		analogOnScreenControl.getControlKnob().setScale(0.75f);
		analogOnScreenControl.refreshControlKnobPosition();

		this.mScene.setChildScene(analogOnScreenControl);
	}

	@Override
	public boolean onAreaTouched( final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		if(pSceneTouchEvent.isActionDown()) {
			final Sprite face = (Sprite) pTouchArea;
			if (face.equals(mAndroid)) {
			} else {
				this.jumpZombie(face);
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		if(this.mPhysicsWorld != null) {
			if(pSceneTouchEvent.isActionDown()) {
				this.addZombie(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				return true;
			}
		}
		return false;
	}

	@Override
	public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {

	}

	@Override
	public void onAccelerationChanged(final AccelerationData pAccelerationData) {
		this.mGravityX = pAccelerationData.getX();
		this.mGravityY = pAccelerationData.getY();

		final Vector2 gravity = Vector2Pool.obtain(this.mGravityX, this.mGravityY);
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		this.disableAccelerationSensor();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	private void addZombie(final float pX, final float pY) {
		final Sprite zombieSprite;
		final Body zombieBody;

		final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

		zombieSprite = new Sprite(pX, pY, this.mZombieTextureRegion, this.getVertexBufferObjectManager());
		zombieBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, zombieSprite, BodyType.DynamicBody, objectFixtureDef);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(zombieSprite, zombieBody, true, true));

		zombieSprite.setUserData(zombieBody);
		this.mScene.registerTouchArea(zombieSprite);
		this.mScene.attachChild(zombieSprite);
	}

	private void jumpZombie(final Sprite zombie) {
		final Body zombieBody = (Body)zombie.getUserData();

		final Vector2 velocity = Vector2Pool.obtain(this.mGravityX * -50, this.mGravityY * -50);
		zombieBody.setLinearVelocity(velocity);
		Vector2Pool.recycle(velocity);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}