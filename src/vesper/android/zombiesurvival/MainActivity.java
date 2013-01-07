package vesper.android.zombiesurvival;

import java.io.IOException;
import java.util.ArrayList;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
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
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.svg.opengl.texture.atlas.bitmap.SVGBitmapTextureAtlasTextureRegionFactory;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.SAXUtils;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.IEntityLoader;
import org.andengine.util.level.LevelLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.xml.sax.Attributes;
import android.util.Log;
import android.view.KeyEvent;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class MainActivity extends BaseGameActivity implements IOnSceneTouchListener,
															  IOnAreaTouchListener,
															  IScrollDetectorListener,
															  IPinchZoomDetectorListener {

	public static final int CAMERA_WIDTH = 800;
	public static final int CAMERA_HEIGHT = 480;
	private ZoomCamera mZoomCamera;
	private PinchZoomDetector mPinchZoomDetector;
	private SurfaceScrollDetector mScrollDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	private float mDefaultZoomFactor;
	
	public enum SceneType {
		SPLASH,
		MENU,
		OPTIONS,
		LEVELSELECT,
		GAME
	}
	
	public SceneType currentScene = SceneType.SPLASH;
	
	private Scene mSplashScene;
	private Scene mGameScene;
	
	private BitmapTextureAtlas splashTextureAtlas;
	private ITextureRegion splashTextureRegion;
	private Sprite splash;
	
	// collision filter bit categories and masks
	public static final short CATEGORYBIT_WALL = 1;
	public static final short MASKBITS_WALL = PhysicalGameObject.CATEGORYBIT_ENEMY
			+ PhysicalGameObject.CATEGORYBIT_WALL + PhysicalGameObject.CATEGORYBIT_PLAYER
			+ PhysicalGameObject.CATEGORYBIT_BULLET;
	private final static FixtureDef WALL_FIXTUREDEF = PhysicsFactory.createFixtureDef(
			0, 0.5f, 0.5f, false, CATEGORYBIT_WALL, MASKBITS_WALL, (short)0);
	
	// level loading related
	private Boolean mLevelEditMode = false;
	private static ArrayList<ILevelObject> _LevelObjectList = new ArrayList<ILevelObject>();
	
	// texture related
	public static VertexBufferObjectManager _VBOM;
	private BuildableBitmapTextureAtlas mTextureAtlas;
	private ITextureRegion mPlayerTextureRegion;
	public static ITextureRegion _OnScreenControlBaseTextureRegion;
	public static ITextureRegion _OnScreenControlKnobTextureRegion;
	public static ITextureRegion _HealthTextureRegion;
	public static ITextureRegion _BulletTextureRegion;
	public static ITextureRegion _ZombieTextureRegion;
	
	// pools
	public static ZombiePool _ZombiePool;
	public static BulletPool _BulletPool;
	
	private Player mPlayer;

	public static PhysicsWorld _PhysicsWorld;
	private HUD mGameHUD;
	private HUD mLevelEditHUD;

	@Override
	public EngineOptions onCreateEngineOptions() {
		this.mZoomCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		mDefaultZoomFactor = mZoomCamera.getZoomFactor();
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mZoomCamera);
		engineOptions.getTouchOptions().setNeedsMultiTouch(true);
		return engineOptions;
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		SVGBitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		_VBOM = mEngine.getVertexBufferObjectManager();
		splashTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.DEFAULT);
		splashTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlas, this,"splash.png", 0, 0);
		splashTextureAtlas.load();
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
		initSplashScene();
	    pOnCreateSceneCallback.onCreateSceneFinished(this.mSplashScene);
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		mEngine.registerUpdateHandler(new TimerHandler(.1f, new ITimerCallback() {
			public void onTimePassed(final TimerHandler pTimerHandler) {
				mEngine.unregisterUpdateHandler(pTimerHandler);
				loadResources();
				loadScenes();         
				splash.detachSelf();
				setScene(SceneType.GAME);
			}
		}));
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final ITouchArea pTouchArea,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		return pTouchArea.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		if (mLevelEditMode) {
			// only allow pinch and scroll while in level edit mode
			this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
			if(this.mPinchZoomDetector.isZooming()) {
				this.mScrollDetector.setEnabled(false);
			} else {
				if(pSceneTouchEvent.isActionDown()) {
					this.mScrollDetector.setEnabled(true);
				}
				this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
			}
		}

		return true;
	}

	public void loadResources() 
	{
		_PhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		
		final TextureManager textureMgr = this.getTextureManager();
		mTextureAtlas = new BuildableBitmapTextureAtlas(textureMgr, 1024, 1024, TextureOptions.BILINEAR);
		mPlayerTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, this, "player.svg", 128, 128);
		_OnScreenControlBaseTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, this, "onscreen_control_base.svg", 128, 128);
		_OnScreenControlKnobTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, this, "onscreen_control_knob.svg", 64, 64);
		_HealthTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, this, "health.svg", 32, 32);
		_BulletTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, this, "bullet.svg", 8, 8);
		_ZombieTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mTextureAtlas, this, "zombie.svg", 128, 128);
		try {
			mTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
			mTextureAtlas.load();
		} catch (final TextureAtlasBuilderException e) {
			Debug.e(e);
		}
		
		_ZombiePool = new ZombiePool();
		_BulletPool = new BulletPool();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// handle the back key appropriately
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			switch (currentScene) {
			case SPLASH:
				// just ignore it
				break;
			case MENU:
				finish();
				break;
			case OPTIONS:
			case LEVELSELECT:
				setScene(SceneType.MENU);
			case GAME:
				//setScene(SceneType.LEVELSELECT);
				finish();
				break;
			default:
				break;
			}
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			// just some stuff for testing
			generateLevelXML();
			setLevelEditMode(!mLevelEditMode);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void loadScenes()
	{
		// load your game here, you scenes
		this.mEngine.registerUpdateHandler(new FPSLogger());
	
		final Scene scene = new Scene();
		mGameScene = scene;
		scene.setOnAreaTouchTraversalFrontToBack();
		scene.setBackground(new Background(.7f, .7f, .7f));
		scene.setOnSceneTouchListener(this);
		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		scene.registerUpdateHandler(_PhysicsWorld);
		scene.setOnAreaTouchListener(this);
		
		_PhysicsWorld.setContactListener(new ContactListener() {
			
			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
			    Object[] userDataArray = {
			            contact.getFixtureA().getBody().getUserData(),
			            contact.getFixtureB().getBody().getUserData() 
			        };
	
		        if (userDataArray[0] != null && userDataArray[1] != null) {
		            if (userDataArray[0] instanceof Bullet && userDataArray[1] instanceof Zombie) {
		            	_BulletPool.recycle((Bullet) userDataArray[0]);
		            	_ZombiePool.recycle((Zombie) userDataArray[1]);
		            } else if (userDataArray[1] instanceof Bullet && userDataArray[0] instanceof Zombie) {
		            	_BulletPool.recycle((Bullet) userDataArray[1]);
		            	_ZombiePool.recycle((Zombie) userDataArray[0]);
		            } else if (userDataArray[0] instanceof Bullet) {
		            	_BulletPool.recycle((Bullet) userDataArray[0]);
					} else if (userDataArray[1] instanceof Bullet) {
						_BulletPool.recycle((Bullet) userDataArray[1]);
					}
		        }
				
			}
			
			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
			}
			
			@Override
			public void endContact(Contact contact) {
			}
			
			@Override
			public void beginContact(Contact contact) {
			}
		});
		
		scene.registerUpdateHandler(_ZombiePool);
		scene.registerUpdateHandler(_BulletPool);
		
		final LevelLoader levelLoader = new LevelLoader();
		levelLoader.setAssetBasePath("level/");
	
		levelLoader.registerEntityLoader(LevelConstants.TAG_LEVEL, new IEntityLoader() {
			@Override
			public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
				// right
				scene.attachChild(createWall(width - 2, 0, 2, height));
				// left
				scene.attachChild(createWall(0, 0, 2, height));
				// top
				scene.attachChild(createWall(0, 0, width, 2));
				// bottom
				scene.attachChild(createWall(0, height - 2, width, 2));
				
				mZoomCamera.setBounds(0, 0, width, height);
				mZoomCamera.setBoundsEnabled(true);
				return scene;
			}
		});
		
		levelLoader.registerEntityLoader("wall", new IEntityLoader() {
			@Override
			public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, "x");
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, "y");
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, "width");
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, "height");
	
				return createWall(x, y, width, height);
			}
		});
		
		levelLoader.registerEntityLoader("entity", new IEntityLoader() {
			@Override
			public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, "x");
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, "y");
				final String type = SAXUtils.getAttributeOrThrow(pAttributes, "type");
	
				if(type.equals("zombie")) {
					Zombie zombie = _ZombiePool.obtain(x, y);
					scene.registerTouchArea(zombie);
					return zombie;
				} else if(type.equals("player")) {
					Player player = new Player(x, y, mPlayerTextureRegion);
					mPlayer = player;
					mZoomCamera.setChaseEntity(player); // follow player
					_ZombiePool.setPlayer(player);
					scene.registerTouchArea(player);
					addObjectToLevel(player);
					return player;
				} else {
					throw new IllegalArgumentException();
				}
			}
		});
	
		try {
			levelLoader.loadLevelFromAsset(this.getAssets(), "testLevel.xml");
		} catch (final IOException e) {
			Debug.e(e);
		}
		
		mGameHUD = new GameHUD(mZoomCamera, _VBOM, mPlayer);
		mLevelEditHUD = new LevelEditHUD(mZoomCamera, this, _VBOM);
		setLevelEditMode(false); // start in game mode
	}

	private void initSplashScene()
	{
		mSplashScene = new Scene();
	    splash = new Sprite(0, 0, splashTextureRegion, _VBOM);
	    splash.setScale(1.5f);
		splash.setPosition((CAMERA_WIDTH - splash.getWidth()) * 0.5f, (CAMERA_HEIGHT-splash.getHeight()) * 0.5f);
		mSplashScene.attachChild(splash);
	}

	private IEntity createWall(int x, int y, int width, int height) {
		final Rectangle wall = new Rectangle(x, y, width, height, _VBOM);
		wall.setColor(Color.BLACK);
		PhysicsFactory.createBoxBody(_PhysicsWorld, wall, BodyType.StaticBody, WALL_FIXTUREDEF).setUserData(wall);
		return wall;
	}

	/**
	 * Helper to switch to the specified type of scene
	 * @param sceneType
	 */
	private void setScene(SceneType sceneType) {
		switch (sceneType) {
		case SPLASH:
			mEngine.setScene(mSplashScene);
			currentScene = SceneType.SPLASH;
			break;
// will be implemented soon
//		case MENU:
//			mEngine.setScene(mMenuScene);
//			currentScene = SceneType.MENU;
//			break;
//		case OPTIONS:
//			mEngine.setScene(mOptionsScene);
//			currentScene = SceneType.OPTIONS;
//			break;
//		case LEVELSELECT:
//			mEngine.setScene(mLevelSelectScene);
//			currentScene = SceneType.LEVELSELECT;
//			break;
		case GAME:
			mEngine.setScene(mGameScene);
			currentScene = SceneType.GAME;
			break;
		default: // what?
			break;
		}
	}

	private String generateLevelXML() {
		Log.d("generateLevelXML", "--- Start ----");
		for (ILevelObject obj : _LevelObjectList) {
			Log.d("generateLevelXML", obj.getLevelXML());
		}
		Log.d("generateLevelXML", "--- finish ---");
		return null;
	}
	
	/**
	 * Enable or disable level edit mode
	 * @param enable - true to enable level edit mode
	 */
	public void setLevelEditMode(boolean enable) {
		if (enable) {
			enableLevelEditMode();
		} else {
			disableLevelEditMode();
		}
	}
	
	private void enableLevelEditMode() {
		mLevelEditMode = true;
		for (ILevelObject obj : _LevelObjectList) {
			obj.onEnableLevelEditMode();
		}
		mZoomCamera.setChaseEntity(null); // camera should not follow player
		mZoomCamera.setBoundsEnabled(false);
		mZoomCamera.setHUD(mLevelEditHUD);
	}
	
	private void disableLevelEditMode() {
		mLevelEditMode = false;
		for (ILevelObject obj : _LevelObjectList) {
			obj.onDisableLevelEditMode();
		}
		mZoomCamera.setChaseEntity(mPlayer);
		mZoomCamera.setZoomFactor(mDefaultZoomFactor);
		mZoomCamera.setBoundsEnabled(true);
		mZoomCamera.setHUD(mGameHUD);
	}
	
	public static void addObjectToLevel(ILevelObject obj) {
		_LevelObjectList.add(obj);
	}
	
	public static void removedObjectFromLevel(ILevelObject obj) {
		_LevelObjectList.remove(obj);
	}

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
	
}
