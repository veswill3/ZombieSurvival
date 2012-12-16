package vesper.android.zombiesurvival;

import org.andengine.entity.Entity;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public abstract class Enemy extends Character {
	
	protected final Entity mPlayer;

	public Enemy(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager,
			PhysicsWorld pPhysicsWorld, FixtureDef pFixtureDef, Entity player) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager, pPhysicsWorld,
				pFixtureDef);
		
		mPlayer = player;
	}

}
