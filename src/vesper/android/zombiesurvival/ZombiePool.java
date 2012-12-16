package vesper.android.zombiesurvival;

import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import android.util.Log;

public class ZombiePool extends EntityPool<Zombie> {
	
	private Player mPlayer;

	public ZombiePool(MainActivity activity, PhysicsWorld physicsWorld)
			throws IllegalArgumentException {
		super(activity, physicsWorld);
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
		mActivity.addObjectToLevel(pItem);
	}

	@Override
	protected void onRecycle(Zombie pItem) {
        pItem.setActive(false);
        mActivity.removedObjectFromLevel(pItem);
	}

	@Override
	public BitmapTextureAtlas onCreateTextureAtlas() {
		Log.d("ZombiePool", "onCreateTextureAtlas");
		return new BitmapTextureAtlas(mActivity.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
	}

	@Override
	public ITextureRegion onCreateTextureRegion() {
		Log.d("ZombiePool", "oncreateTextureRegion");
		return BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(getTextureAtlas(), mActivity, "Zombie.png", 0, 0, 1, 1);
	}

	@Override
	protected Zombie onAllocatePoolItem() {
		Log.d("ZombiePool", "allocating new Zombie");
		return new Zombie(0, 0, mTextureRegion, mVertexBufferObjectManager, mPhysicsWorld, mPlayer);
	}

}
