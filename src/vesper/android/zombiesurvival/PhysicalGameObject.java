package vesper.android.zombiesurvival;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class PhysicalGameObject extends Sprite {
	
	public static final short CATEGORYBIT_WALL = 1;
	public static final short CATEGORYBIT_PLAYER = 2;
	public static final short CATEGORYBIT_ENEMY = 4;
	public static final short CATEGORYBIT_BULLET = 8;
	
	private final Body mBody;

	public PhysicalGameObject(float pX, float pY, ITextureRegion pTextureRegion, FixtureDef pFixtureDef) {
		super(pX, pY, pTextureRegion, MainActivity._VBOM);
		
		setCullingEnabled(true); // no need to continue to draw when not onscreen

		// setup the physics
		PhysicsWorld physicsWorld = MainActivity._PhysicsWorld;
		Body body = PhysicsFactory.createCircleBody(physicsWorld, this, BodyType.DynamicBody, pFixtureDef);
		body.setActive(false); // initially start inactive until we add it to the world
		body.setUserData(this);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, false));
		mBody = body;
	}
	
	public void setActive(Boolean activeFlag) {
		setIgnoreUpdate(!activeFlag);
		mBody.setActive(activeFlag);
		this.setVisible(activeFlag);
		if (!activeFlag) {
			this.detachSelf();
		}
	}
	
	public Body getBody() {
		return mBody;
	}

}
