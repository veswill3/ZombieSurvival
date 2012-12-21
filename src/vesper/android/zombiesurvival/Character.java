package vesper.android.zombiesurvival;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public abstract class Character extends PhysicalGameObject implements ILevelObject {

	protected boolean mLevelEditFlag;
	private final IUpdateHandler mGameModeUpdateHandler;
	int mMaxSpeed;
	int mHealth;

	public Character(float pX, float pY, ITextureRegion pTextureRegion, FixtureDef pFixtureDef) {
		super(pX, pY, pTextureRegion, pFixtureDef);
		mGameModeUpdateHandler = onCreateGameModeUpdateHanderl();
	}

	@Override
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			float pTouchAreaLocalX, float pTouchAreaLocalY) {
		if (mLevelEditFlag) { // enable touch and drag if in level edit mode
			float x = pSceneTouchEvent.getX() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
			float y = pSceneTouchEvent.getY() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
			getBody().setTransform(x, y, 0);
			getBody().setLinearVelocity(0f, 0f);
			return true;
		}
		return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
	}

	@Override
	public String getLevelType() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getLevelXML() {
		return "<entity x=\"" + getX() + "\" y=\"" + getY() + "\" type=\"" + getLevelType() + "\"/>";
	}

	@Override
	public final void onEnableLevelEditMode() {
		mLevelEditFlag = true;
		if (mGameModeUpdateHandler != null) {
			unregisterUpdateHandler(mGameModeUpdateHandler);
		}
		getBody().setLinearVelocity(0f, 0f); // stop if currently moving
		doOnEnableLevelEditMode();
	}

	@Override
	public final void onDisableLevelEditMode() {
		mLevelEditFlag = false;
		if (mGameModeUpdateHandler != null) {
			registerUpdateHandler(mGameModeUpdateHandler);
		}
		doOnDisableLevelEditMode();
	}
	
	/**
	 * Executes on enabling Level edit mode
	 */
	protected void doOnEnableLevelEditMode() {
		
	}
	
	/**
	 * Executes on disabling level edit mode
	 */
	protected void doOnDisableLevelEditMode() {
		
	}
	
	/**
	 * Called during object creation
	 * @return update handler to run ONLY while in game mode
	 */
	protected abstract IUpdateHandler onCreateGameModeUpdateHanderl();
	
}
