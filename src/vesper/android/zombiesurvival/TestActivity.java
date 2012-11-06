package vesper.android.zombiesurvival;

import java.io.IOException;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.SAXUtils;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.IEntityLoader;
import org.andengine.util.level.LevelLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.xml.sax.Attributes;
import android.opengl.GLES20;
import android.util.Log;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class TestActivity extends SimpleBaseGameActivity implements //IAccelerationListener,
																	IOnSceneTouchListener,
																	IOnAreaTouchListener,
																	IScrollDetectorListener,
																	IPinchZoomDetectorListener {
	// ===========================================================
	// Constants
	// ===========================================================

//	private static final int LEVEL_WIDTH = 2000;
//	private static final int LEVEL_HEIGHT = 1000;
	private static final int DEFAULT_CAMERA_WIDTH = 800;
	private static final int DEFAULT_CAMERA_HEIGHT = 480;
	
	// level loading related
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
	private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
	private static final String TAG_ENTITY_ATTRIBUTE_WIDTH = "width";
	private static final String TAG_ENTITY_ATTRIBUTE_HEIGHT = "height";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";

	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ZOMBIE = "zombie";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
	private static final String TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_WALL = "wall";

	// ===========================================================
	// Fields
	// ===========================================================

	// camera related
	private ZoomCamera mZoomCamera;
	private SurfaceScrollDetector mScrollDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	
	// texture related
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private BitmapTextureAtlas mOnScreenControlTexture;
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;
	
	private ITextureRegion mAndroidTextureRegion;
	private ITextureRegion mZombieTextureRegion;
	
	private PhysicalSprite mAndroid; //handle to "player" - android sprite

	// physics related
	private PhysicsWorld mPhysicsWorld;

//	private Scene mScene;

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
		this.mZoomCamera = new ZoomCamera(0, 0, DEFAULT_CAMERA_WIDTH, DEFAULT_CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
				new RatioResolutionPolicy(DEFAULT_CAMERA_WIDTH, DEFAULT_CAMERA_HEIGHT), mZoomCamera);
	}

	@Override
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),
				32, 64, TextureOptions.BILINEAR);
		this.mAndroidTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				this.mBitmapTextureAtlas, this, "Android.png", 0, 0);
		this.mZombieTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
				this.mBitmapTextureAtlas, this, "Zombie.png", 0, 32, 1, 1);
		this.mBitmapTextureAtlas.load();

		this.mOnScreenControlTexture = new BitmapTextureAtlas(this.getTextureManager(),
				256, 128, TextureOptions.BILINEAR);
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
		this.mOnScreenControlTexture.load();
	}

	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);

		final Scene scene = new Scene();
//		this.mScene = scene;
		scene.setOnAreaTouchTraversalFrontToBack();
		scene.setBackground(new Background(0, 0, 0));
		scene.setOnSceneTouchListener(this);
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		scene.registerUpdateHandler(this.mPhysicsWorld);
		scene.setOnAreaTouchListener(this);
		
		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		
		final LevelLoader levelLoader = new LevelLoader();
		levelLoader.setAssetBasePath("level/");

		levelLoader.registerEntityLoader(LevelConstants.TAG_LEVEL, new IEntityLoader() {
			@Override
			public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
				Log.d("LevelLoader", "about to create borders");
				// right
				scene.attachChild(createWall(width - 2, 0, 2, height));
				// left
				scene.attachChild(createWall(0, 0, 2, height));
				// top
				scene.attachChild(createWall(0, 0, width, 2));
				// bottom
				scene.attachChild(createWall(0, height - 2, width, 2));
				return scene;
			}
		});
		
		levelLoader.registerEntityLoader(TAG_ENTITY, new IEntityLoader() {
			@Override
			public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				Log.d("LevelLoader", "About to load an entity");
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_HEIGHT);
				final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);

				final VertexBufferObjectManager vertexBufferObjectManager = TestActivity.this.getVertexBufferObjectManager();

				IEntity entity;	
				if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_ZOMBIE)) {
					Log.d("LevelLoader", "loading a zombie");
					Zombie zombie = new Zombie(x, y, mZombieTextureRegion, vertexBufferObjectManager, mPhysicsWorld, mAndroid);
					zombie.attach(scene, mPhysicsWorld);
					entity = zombie;
				} else if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_WALL)) {
					Log.d("LevelLoader", "loading a wall");
					entity = createWall(x, y, width, height);
				} else if(type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER)) {
					Log.d("LevelLoader", "loading a player");
					Player player = new Player(x, y, mAndroidTextureRegion, vertexBufferObjectManager, mPhysicsWorld);
					mAndroid = player;
					player.attach(scene, mPhysicsWorld);
					entity = player;
				} else {
					throw new IllegalArgumentException();
				}

				return entity;
			}
		});

		try {
			Log.d("LevelLoader", "about to start loading");
			levelLoader.loadLevelFromAsset(this.getAssets(), "testLevel.xml");
			Log.d("LevelLoader", "finished loading the level, I guess");
		} catch (final IOException e) {
			Debug.e(e);
		}
//
		//createBorders(scene, vertexBufferObjectManager);
		
		// create player in center of the screen
//		Player player = new Player(LEVEL_WIDTH / 2, LEVEL_HEIGHT / 2,
//				mAndroidTextureRegion, vertexBufferObjectManager, mPhysicsWorld);
//		mAndroid = player;
//		player.attach(scene, mPhysicsWorld);
//
//		// add some zombies randomly
//		Random rand = new Random();
//		int x,y;
//		for (int i = 0; i < 50; i++) {
//			x = rand.nextInt(LEVEL_WIDTH - 64) + 32;
//			y = rand.nextInt(LEVEL_HEIGHT - 64) + 32;
//			Zombie zombie = new Zombie(x, y, mZombieTextureRegion,
//					this.getVertexBufferObjectManager(), mPhysicsWorld, mAndroid);
//			zombie.attach(mScene, mPhysicsWorld);
//		}
		
		initOnScreenControls(scene, vertexBufferObjectManager);
		
		return scene;
	}

	protected IEntity createWall(int x, int y, int width, int height) {
		Log.d("LevelLoader", "creating wall. x:" + x + " y:" + y + " w:" + width + " h:" + height);
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		final Rectangle wall = new Rectangle(x, y, width, height, this.getVertexBufferObjectManager());
		PhysicsFactory.createBoxBody(mPhysicsWorld, wall, BodyType.StaticBody, wallFixtureDef);
		return wall;
	}

//	private void createBorders(Scene scene, final VertexBufferObjectManager vertexBufferObjectManager) {
//		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
//		
//		final Rectangle ground = new Rectangle(0, LEVEL_HEIGHT - 2, LEVEL_WIDTH, 2, vertexBufferObjectManager);
//		final Rectangle roof = new Rectangle(0, 0, LEVEL_WIDTH, 2, vertexBufferObjectManager);
//		final Rectangle left = new Rectangle(0, 0, 2 , LEVEL_HEIGHT, vertexBufferObjectManager);
//		final Rectangle right = new Rectangle(LEVEL_WIDTH - 2, 0, 2, LEVEL_HEIGHT, vertexBufferObjectManager);
//		
//		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
//		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
//		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
//		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
//		
//		scene.attachChild(ground);
//		scene.attachChild(roof);
//		scene.attachChild(left);
//		scene.attachChild(right);
//	}

	private void initOnScreenControls(Scene scene, final VertexBufferObjectManager vertexBufferObjectManager) {
		final AnalogOnScreenControl analogOnScreenControl =
				new AnalogOnScreenControl(0, DEFAULT_CAMERA_HEIGHT - mOnScreenControlBaseTextureRegion.getHeight(),
						mZoomCamera, mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion,
						0.1f, vertexBufferObjectManager, new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl,
					final float pValueX, final float pValueY) {
				final Body androidBody = mAndroid.getBody();
				final Vector2 velocity = Vector2Pool.obtain(pValueX * 20, pValueY * 20);
				androidBody.setLinearVelocity(velocity);
				Vector2Pool.recycle(velocity);

				//final float rotationInRad = (float)Math.atan2(-pValueX, pValueY);
				//androidBody.setTransform(androidBody.getWorldCenter(), rotationInRad);
				//TestActivity.this.mAndroid.setRotation(MathUtils.radToDeg(rotationInRad));
			}

			@Override
			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {
				/* Nothing. */
			}
		});
		analogOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		analogOnScreenControl.getControlBase().setAlpha(0.5f);
		analogOnScreenControl.refreshControlKnobPosition();

		scene.setChildScene(analogOnScreenControl);
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		return pTouchArea.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
//		From pinch zoom example
//		-----------------------
		this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

		if(this.mPinchZoomDetector.isZooming()) {
			this.mScrollDetector.setEnabled(false);
		} else {
			if(pSceneTouchEvent.isActionDown()) {
				this.mScrollDetector.setEnabled(true);
			}
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		return true;
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();

		//this.enableAccelerationSensor(this);
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
		this.disableAccelerationSensor();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	@Override
	public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}
	
	@Override
	public void onScrollFinished(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		final float zoomFactor = this.mZoomCamera.getZoomFactor();
		this.mZoomCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
		this.mPinchZoomStartedCameraZoomFactor = this.mZoomCamera.getZoomFactor();
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		this.mZoomCamera.setZoomFactor(this.mPinchZoomStartedCameraZoomFactor * pZoomFactor);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}