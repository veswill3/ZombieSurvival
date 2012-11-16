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
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.SAXUtils;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.IEntityLoader;
import org.andengine.util.level.LevelLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.xml.sax.Attributes;
import android.opengl.GLES20;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class GameActivity extends SimpleBaseGameActivity implements IOnSceneTouchListener,
																	IOnAreaTouchListener {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;
	
	// collision filter bit categories and masks
	public static final short CATEGORYBIT_WALL = 1;
	public static final short MASKBITS_WALL = PhysicalSprite.CATEGORYBIT_ENEMY
			+ PhysicalSprite.CATEGORYBIT_WALL + PhysicalSprite.CATEGORYBIT_PLAYER
			+ PhysicalSprite.CATEGORYBIT_BULLET;
	private final static FixtureDef WALL_FIXTUREDEF = PhysicsFactory.createFixtureDef(
			0, 0.5f, 0.5f, false, CATEGORYBIT_WALL, MASKBITS_WALL, (short)0);
	
	// level loading related
	private static final String TAG_ENTITY = "entity";
	private static final String TAG_WALL = "wall";
	private static final String TAG_ATTRIBUTE_X = "x";
	private static final String TAG_ATTRIBUTE_Y = "y";
	private static final String TAG_ATTRIBUTE_WIDTH = "width";
	private static final String TAG_ATTRIBUTE_HEIGHT = "height";
	private static final String TAG_ATTRIBUTE_TYPE = "type";

	private static final String TAG_ATTRIBUTE_TYPE_VALUE_ZOMBIE = "zombie";
	private static final String TAG_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";

	// ===========================================================
	// Fields
	// ===========================================================

	private ZoomCamera mZoomCamera;
	
	// texture related
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private BitmapTextureAtlas mOnScreenControlTexture;
	private ITextureRegion mOnScreenControlBaseTextureRegion;
	private ITextureRegion mOnScreenControlKnobTextureRegion;
	private ITextureRegion mAndroidTextureRegion;
	
	private ZombiePool mZombiePool;
	private BulletPool mBulletPool;
	
	private Player mPlayer; //handle to "player" - android sprite

	private PhysicsWorld mPhysicsWorld;

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
		this.mZoomCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mZoomCamera);
	}

	@Override
	public void onCreateResources() {
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		mZombiePool = new ZombiePool(this, mPhysicsWorld);
		mBulletPool = new BulletPool(this, mPhysicsWorld);
		
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),
				32, 32, TextureOptions.BILINEAR);
		this.mAndroidTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				this.mBitmapTextureAtlas, this, "Android.png", 0, 0);
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

		final Scene scene = new Scene();
		scene.setOnAreaTouchTraversalFrontToBack();
		scene.setBackground(new Background(.7f, .7f, .7f));
		scene.setOnSceneTouchListener(this);
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		scene.registerUpdateHandler(this.mPhysicsWorld);
		scene.setOnAreaTouchListener(this);
		
		mPhysicsWorld.setContactListener(new ContactListener() {
			
			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
			    Object[] userDataArray = {
			            contact.getFixtureA().getBody().getUserData(),
			            contact.getFixtureB().getBody().getUserData() 
			        };

		        if (userDataArray[0] != null && userDataArray[1] != null) {
		            if (userDataArray[0] instanceof Bullet && userDataArray[1] instanceof Zombie) {
		            	mBulletPool.recycle((Bullet) userDataArray[0]);
		            	mZombiePool.recycle((Zombie) userDataArray[1]);
		            } else if (userDataArray[1] instanceof Bullet && userDataArray[0] instanceof Zombie) {
		            	mBulletPool.recycle((Bullet) userDataArray[1]);
		            	mZombiePool.recycle((Zombie) userDataArray[0]);
		            } else if (userDataArray[0] instanceof Bullet) {
		            	mBulletPool.recycle((Bullet) userDataArray[0]);
					} else if (userDataArray[1] instanceof Bullet) {
						mBulletPool.recycle((Bullet) userDataArray[1]);
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
		
		scene.registerUpdateHandler(mZombiePool);
		scene.registerUpdateHandler(mBulletPool);
		
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		
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
		
		levelLoader.registerEntityLoader(TAG_WALL, new IEntityLoader() {
			@Override
			public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ATTRIBUTE_X);
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ATTRIBUTE_Y);
				final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ATTRIBUTE_WIDTH);
				final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ATTRIBUTE_HEIGHT);

				return createWall(x, y, width, height);
			}
		});
		
		levelLoader.registerEntityLoader(TAG_ENTITY, new IEntityLoader() {
			@Override
			public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
				final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ATTRIBUTE_X);
				final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ATTRIBUTE_Y);
				final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ATTRIBUTE_TYPE);

				final VertexBufferObjectManager vertexBufferObjectManager = GameActivity.this.getVertexBufferObjectManager();

				if(type.equals(TAG_ATTRIBUTE_TYPE_VALUE_ZOMBIE)) {
					Zombie zombie = mZombiePool.obtain(x, y);
					scene.registerTouchArea(zombie);
					return zombie;
				} else if(type.equals(TAG_ATTRIBUTE_TYPE_VALUE_PLAYER)) {
					Player player = new Player(x, y, mAndroidTextureRegion, vertexBufferObjectManager, mPhysicsWorld);
					mPlayer = player;
					mZoomCamera.setChaseEntity(player); // follow player
					mZombiePool.setPlayer(player);
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
		
		initOnScreenControls(scene, vertexBufferObjectManager);
		
		return scene;
	}

	protected IEntity createWall(int x, int y, int width, int height) {
		final Rectangle wall = new Rectangle(x, y, width, height, this.getVertexBufferObjectManager());
		wall.setColor(Color.BLACK);
		PhysicsFactory.createBoxBody(mPhysicsWorld, wall, BodyType.StaticBody, WALL_FIXTUREDEF).setUserData(wall);
		return wall;
	}

	private void initOnScreenControls(Scene scene, final VertexBufferObjectManager vertexBufferObjectManager) {
		final AnalogOnScreenControl analogOnScreenControl =
				new AnalogOnScreenControl(0, CAMERA_HEIGHT - mOnScreenControlBaseTextureRegion.getHeight(),
						mZoomCamera, mOnScreenControlBaseTextureRegion, mOnScreenControlKnobTextureRegion,
						0.1f, vertexBufferObjectManager, new IAnalogOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl,
					final float pValueX, final float pValueY) {
				final Body androidBody = mPlayer.getBody();
				final Vector2 velocity = Vector2Pool.obtain(pValueX * 20, pValueY * 20);
				androidBody.setLinearVelocity(velocity);
				Vector2Pool.recycle(velocity);
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
		
		if (pSceneTouchEvent.isActionDown()) {
			float x = mPlayer.getX() + 16; // adjusting to center of sprite
			float y = mPlayer.getY() + 16; // TODO - fix this
			Bullet b = mBulletPool.obtain(x, y, new Vector2(
					pSceneTouchEvent.getX(), pSceneTouchEvent.getY()).sub(x, y));
			pScene.attachChild(b);
		}
		
		return true;
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();
	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();
	}

	// ===========================================================
	// Methods
	// ===========================================================

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}