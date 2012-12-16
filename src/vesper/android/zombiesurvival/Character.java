package vesper.android.zombiesurvival;

import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public abstract class Character extends PhysicalGameObject implements ILevelObject {

	protected boolean mLevelEditFlag;
	int mMaxSpeed;
	int mHealth;

	public Character(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager,
			PhysicsWorld pPhysicsWorld, FixtureDef pFixtureDef) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager, pPhysicsWorld,
				pFixtureDef);
		// TODO Auto-generated constructor stub
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
	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
			float pTouchAreaLocalX, float pTouchAreaLocalY) {
		if (mLevelEditFlag) {
			float x = pSceneTouchEvent.getX() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
			float y = pSceneTouchEvent.getY() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
			getBody().setTransform(x, y, 0);
			getBody().setLinearVelocity(0f, 0f);
			return true;
		}
		return super.onAreaTouched(pSceneTouchEvent, pTouchAreaLocalX, pTouchAreaLocalY);
	}

	@Override
	public final void onEnableLevelEditMode() {
		getBody().setLinearVelocity(0f, 0f); // stop if currently moving
		mLevelEditFlag = true;
		doOnEnableLevelEditMode();
	}

	@Override
	public final void onDisableLevelEditMode() {
		mLevelEditFlag = false;
		doOnDisableLevelEditMode();
	}
	
	public abstract void doOnEnableLevelEditMode();
	
	public abstract void doOnDisableLevelEditMode();
	
}
