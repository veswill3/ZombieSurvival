package vesper.android.zombiesurvival;

import com.badlogic.gdx.math.Vector2;

public class Pistol extends Weapon {
	
//	private static final int MAX_AMMO = 50;
//	private int mAmmo;
	final BulletPool mBulletPool;
	
	public Pistol(final Character pParent, final BulletPool pBulletPool) {
		super(pParent);
		mBulletPool = pBulletPool;
//		this.mAmmo = MAX_AMMO;
	}

	@Override
	protected void discharge(float pX, float pY) {
		Character parent = mParent;
		float x = parent.getX() + parent.getWidth() / 2; // adjusting to center of sprite
		float y = parent.getY() + parent.getHeight() / 2;
		Bullet b = mBulletPool.obtain(x, y, new Vector2(pX, pY));
		mParent.getParent().attachChild(b);
	}
	
}
