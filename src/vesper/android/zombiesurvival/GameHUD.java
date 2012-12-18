package vesper.android.zombiesurvival;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

public class GameHUD extends HUD {
	
	private static final float BORDER = 10f;
	private final Character mCharacter;
	
	private final Sprite mHealth;
	//private final Sprite mAmmo;
	
	public GameHUD(final Camera pCamera, final BaseGameActivity pActivity, final VertexBufferObjectManager pVertexBufferObjectManager,
			Character pCharacter){
		super();
		this.setCamera(pCamera);
		
		mCharacter = pCharacter;
		
		// create health meter
		BitmapTextureAtlas healthTextureAtlas = new BitmapTextureAtlas(pActivity.getTextureManager(), 16, 16, TextureOptions.BILINEAR);
		ITextureRegion healthTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(healthTextureAtlas, pActivity, "health.png", 0, 0);
		healthTextureAtlas.load();
		this.mHealth = new Sprite(MainActivity.CAMERA_WIDTH - healthTextureRegion.getWidth() - BORDER
				, BORDER, healthTextureRegion, pVertexBufferObjectManager);
		this.attachChild(this.mHealth);
		
		// create ammo meter
//		BitmapTextureAtlas ammoTextureAtlas = new BitmapTextureAtlas(pActivity.getTextureManager(), 16, 720, TextureOptions.BILINEAR);
//		ITextureRegion ammoTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(ammoTextureAtlas, pActivity, "ammo.png", 0, 0);
//		ammoTextureAtlas.load();
		
	}

}
