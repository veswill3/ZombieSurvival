package vesper.android.zombiesurvival;

import org.andengine.opengl.texture.region.ITextureRegion;
import android.util.Log;
import com.badlogic.gdx.math.Vector2;

public class BulletPool extends EntityPool<Bullet> {

	public BulletPool() {
		super();
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
	public ITextureRegion onCreateTextureRegion() {
		return MainActivity._BulletTextureRegion;
	}

	@Override
	protected Bullet onAllocatePoolItem() {
		return new Bullet(0, 0, mTextureRegion);
	}

}
