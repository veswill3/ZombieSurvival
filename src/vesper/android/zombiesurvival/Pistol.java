package vesper.android.zombiesurvival;

import com.badlogic.gdx.math.Vector2;

public class Pistol extends Weapon {
	
//	private static final int MAX_AMMO = 50;
//	private int mAmmo;
	
	public Pistol(final Character pParent) {
		super(pParent);
//		this.mAmmo = MAX_AMMO;
	}

	@Override
	protected void discharge(float pX, float pY) {
		Character parent = mParent;
		float x = parent.getX() + parent.getWidth() / 2; // adjusting to center of sprite
		float y = parent.getY() + parent.getHeight() / 2;
		Bullet b = MainActivity._BulletPool.obtain(x, y, new Vector2(pX, pY));
		mParent.getParent().attachChild(b);
	}
	
}
