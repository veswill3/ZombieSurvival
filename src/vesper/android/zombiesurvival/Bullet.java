package vesper.android.zombiesurvival;

import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.opengl.texture.region.ITextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Bullet extends PhysicalGameObject {
	
	public static final short MASKBITS_BULLET = CATEGORYBIT_ENEMY + CATEGORYBIT_WALL + CATEGORYBIT_BULLET;
	private final static FixtureDef mFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f, false,
			CATEGORYBIT_BULLET, MASKBITS_BULLET, (short)0);
	
	private static final float BULLET_SPEED = 50;

	public Bullet(float pX, float pY, ITextureRegion pTextureRegion) {
		super(pX, pY, pTextureRegion, mFixtureDef);
		getBody().setBullet(true);
	}

	public Bullet set(float x, float y, Vector2 direction) {
		Body body = getBody();
		body.setTransform(x / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
				y/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		body.setLinearVelocity(direction.nor().mul(BULLET_SPEED));
		return this;
	}

}
