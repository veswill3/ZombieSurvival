package vesper.android.zombiesurvival;

import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import android.util.Log;
import com.badlogic.gdx.math.Vector2;

public class BulletPool extends EntityPool<Bullet> {

	public BulletPool(MainActivity activity)
			throws IllegalArgumentException {
		super(activity);
	}
	
	public Bullet obtain(float x, float y, Vector2 direction) {
		return addToWorld().set(x, y, direction);
	}
	
	public void recycle(Bullet pItem) {
		removeFromWorld(pItem);
	}

	@Override
	protected void onAddToWorld(Bullet pItem) {
		Log.d("Bullet", "setting bullet to active");
		pItem.setActive(true);
	}

	@Override
	protected void onRecycle(Bullet pItem) {
		pItem.setActive(false);
	}

	@Override
	public BitmapTextureAtlas onCreateTextureAtlas() {
		return new BitmapTextureAtlas(mActivity.getTextureManager(), 8, 8, TextureOptions.BILINEAR);
	}

	@Override
	public ITextureRegion onCreateTextureRegion() {
		return BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(getTextureAtlas(), mActivity, "bullet.png", 0, 0, 1, 1);
	}

	@Override
	protected Bullet onAllocatePoolItem() {
		return new Bullet(0, 0, mTextureRegion, mVertexBufferObjectManager);
	}

}
