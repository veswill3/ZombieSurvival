package vesper.android.zombiesurvival;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class GameHUD extends HUD {
	
	private final Sprite mHealth;
	//private final Sprite mAmmo;
	
	private float mHealthValueX;
	private float mHealthValueY;
	
	private float mAmmoValueX;
	private float mAmmoValueY;
	
	public GameHUD(final float pX, final float pY, final Camera pCamera, final ITextureRegion pHealthTextureRegion,
			final VertexBufferObjectManager pVertexBufferObjectManager){
		
		this.setCamera(pCamera);
		
		//Create health meter
		this.mHealth = new Sprite(0, 0, pHealthTextureRegion, pVertexBufferObjectManager);
		
		this.attachChild(this.mHealth);
		
		
	}
	
	public Sprite getHealth() {
		return this.mHealth;
	}

}
