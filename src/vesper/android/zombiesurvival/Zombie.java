package vesper.android.zombiesurvival;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.Entity;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Zombie extends Enemy<Zombie> {
	
	public static final short MASKBITS_ZOMBIE = CATEGORYBIT_ENEMY + CATEGORYBIT_WALL + CATEGORYBIT_BULLET + CATEGORYBIT_PLAYER;
	private final static FixtureDef mFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, false,
			CATEGORYBIT_ENEMY, MASKBITS_ZOMBIE, (short)0);
	
	private static final int SMELL_RADIUS = 100;
	
	
	public Zombie(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager,
			PhysicsWorld pPhysicsWorld, Entity player) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager,
				pPhysicsWorld, mFixtureDef, player);
		
		registerUpdateHandler(new IUpdateHandler() {
			
			@Override
			public void reset() {}
			
			@Override
			public void onUpdate(float pSecondsElapsed) {
				followPlayer(mPlayer);
			}
		});
		
	}
	
	/**
	 * If within smelling distance, follow the player
	 * @param player object to follow
	 */
	private void followPlayer(Entity player) {
		final Vector2 toTarget = Vector2Pool.obtain(player.getX(), player.getY());
		toTarget.sub(this.getX(), this.getY());
		// only if close enough to smell him
		if (toTarget.len() < SMELL_RADIUS) {
			getBody().setLinearVelocity(toTarget.nor().mul(10));
		}
		Vector2Pool.recycle(toTarget);
	}

	public Zombie set(float x, float y) {
		getBody().setTransform(x / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
				y / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		return this;
	}

	@Override
	public Zombie loadFromXML(String xml) {
		// TODO Auto-generated method stub
		return null;
	}

}
