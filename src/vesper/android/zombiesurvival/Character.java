package vesper.android.zombiesurvival;

import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.badlogic.gdx.physics.box2d.FixtureDef;

public abstract class Character extends PhysicalGameObject implements ILevelObject {

	int mMaxSpeed;
	int mHealth;

	public Character(float pX, float pY, ITextureRegion pTextureRegion,
			VertexBufferObjectManager pVertexBufferObjectManager,
			PhysicsWorld pPhysicsWorld, FixtureDef pFixtureDef) {
		super(pX, pY, pTextureRegion, pVertexBufferObjectManager, pPhysicsWorld,
				pFixtureDef);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getLevelType() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getLevelXML() {
		return "<entity x=\"" + getX() + "\" y=\"" + getY() + "\" type=\"" + getLevelType() + "\"/>";
	}
	
	@Override
	public void setLevelEditMode(boolean enable) {
		if (enable) {
			onEnableLevelEditMode();
		} else {
			onDisableLevelEditMode();
		}
	}
	
}
