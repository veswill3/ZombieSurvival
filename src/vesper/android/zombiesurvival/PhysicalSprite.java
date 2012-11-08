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
	
	private final Body mBody;

	public PhysicalSprite(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager,
			PhysicsWorld pPhysicsWorld, FixtureDef pFixtureDef) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager);
		
		// setup the physics
		Body body = PhysicsFactory.createCircleBody(pPhysicsWorld, this,
				BodyType.DynamicBody, pFixtureDef);
		pPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true));
		mBody = body;
	}
	
	public Body getBody() {
		return mBody;
	}

}
