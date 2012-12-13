package vesper.android.zombiesurvival;

import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Player extends Character<Player> {
	
	Weapon mWeapon;

	public static final short MASKBITS_PLAYER = CATEGORYBIT_ENEMY + CATEGORYBIT_WALL + CATEGORYBIT_PLAYER;
	private final static FixtureDef mFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, false,
			CATEGORYBIT_PLAYER, MASKBITS_PLAYER, (short)0);
	
	public Player(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager,
			PhysicsWorld pPhysicsWorld) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager, pPhysicsWorld, mFixtureDef);
		
		// start with a pistol
		mWeapon = new Pistol();
		
		setActive(true);
	}

	@Override
	public Player loadFromXML(String xml) {
		// TODO Auto-generated method stub
		return this;
	}


}
