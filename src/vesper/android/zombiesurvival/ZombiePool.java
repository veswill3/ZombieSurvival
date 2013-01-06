package vesper.android.zombiesurvival;

import org.andengine.opengl.texture.region.ITextureRegion;
import android.util.Log;

public class ZombiePool extends EntityPool<Zombie> {
	
	private Player mPlayer;

	public ZombiePool() {
		super();
	}
	
	public Zombie obtain(float x, float y) {
		Log.d("ZombiePool", "Obtaining a Zombie");
		return addToWorld().set(x, y);
	}
	
	public void recycle(Zombie zombie) {
		removeFromWorld(zombie);
	}
	
	public void setPlayer(Player player) {
		mPlayer = player;
	}

	@Override
	protected void onAddToWorld(Zombie pItem) {
		Log.d("ZombiePool", "Adding Zombie to world");
		pItem.setActive(true);
		MainActivity.addObjectToLevel(pItem);
	}

	@Override
	protected void onRecycle(Zombie pItem) {
        pItem.setActive(false);
        MainActivity.removedObjectFromLevel(pItem);
	}

	@Override
	public ITextureRegion onCreateTextureRegion() {
		Log.d("ZombiePool", "oncreateTextureRegion");
		return MainActivity._ZombieTextureRegion;
	}

	@Override
	protected Zombie onAllocatePoolItem() {
		Log.d("ZombiePool", "allocating new Zombie");
		return new Zombie(0, 0, mTextureRegion, mPlayer);
	}

}
