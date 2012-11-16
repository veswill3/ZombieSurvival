package vesper.android.zombiesurvival;

import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class PhysicalSprite extends Sprite {
	
	public static final short CATEGORYBIT_WALL = 1;
	public static final short CATEGORYBIT_PLAYER = 2;
	public static final short CATEGORYBIT_ENEMY = 4;
	public static final short CATEGORYBIT_BULLET = 8;
	
	private final Body mBody;

	public PhysicalSprite(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager,
			PhysicsWorld pPhysicsWorld, FixtureDef pFixtureDef) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		
		setCullingEnabled(true); // no need to continue to draw when not onscreen

		this.setVisible(false); // initially start invisible until added to world
		
		// setup the physics
		Body body = PhysicsFactory.createCircleBody(pPhysicsWorld, this, BodyType.DynamicBody, pFixtureDef);
		body.setActive(false); // initially start inactive until we add it to the world
		body.setUserData(this);
		pPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, false));
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
