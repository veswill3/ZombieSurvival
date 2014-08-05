package vesper.android.zombiesurvival.enemy;

import org.andengine.entity.Entity;
import org.andengine.opengl.texture.region.ITextureRegion;

import vesper.android.zombiesurvival.Character;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public abstract class Enemy extends Character {
	
	protected final Entity mPlayer;

	public Enemy(float pX, float pY, float pWidth, float pHeight, ITextureRegion pTextureRegion, FixtureDef pFixtureDef, Entity player) {
		super(pX, pY, pWidth, pHeight, pTextureRegion, pFixtureDef);
		mPlayer = player;
	}

}
