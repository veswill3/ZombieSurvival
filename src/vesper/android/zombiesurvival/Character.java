package vesper.android.zombiesurvival;

import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public abstract class Character extends PhysicalGameObject {
	
	int mMaxSpeed;
	int mHealth;

	public Character(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager,
			PhysicsWorld pPhysicsWorld, FixtureDef pFixtureDef) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager, pPhysicsWorld,
				pFixtureDef);
		// TODO Auto-generated constructor stub
	}
	
	// TODO need to add abstract handler for if health is gone

}
