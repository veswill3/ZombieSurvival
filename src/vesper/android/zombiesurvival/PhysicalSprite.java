package vesper.android.zombiesurvival;

import org.andengine.entity.scene.Scene;
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
	
	/**
	 * Attach this object to the scene and physics world
	 * @param pScene
	 * @param pPhysicsWorld
	 */
	public void attach(Scene pScene, PhysicsWorld pPhysicsWorld) {
		pScene.registerTouchArea(this);
		pScene.attachChild(this);
		pPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(this, mBody, true, true));
	}
	
	/**
	 * Detach this object from the scene and physics world
	 * @param pScene
	 * @param pPhysicsWorld
	 */
	public void detach(Scene pScene, PhysicsWorld pPhysicsWorld) {
		final PhysicsConnector facePhysicsConnector = pPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(this);

		pPhysicsWorld.unregisterPhysicsConnector(facePhysicsConnector);
		pPhysicsWorld.destroyBody(mBody);

		pScene.unregisterTouchArea(this);
		pScene.detachChild(this);
		
		System.gc();
	}	
	
	public Body getBody() {
		return mBody;
	}

}
