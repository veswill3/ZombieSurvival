package vesper.android.zombiesurvival;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.pool.GenericPool;

import android.util.Log;

public abstract class EntityPool<T> extends GenericPool<T> implements IUpdateHandler {
    protected final MainActivity mActivity;
    protected final VertexBufferObjectManager mVertexBufferObjectManager;
    protected BitmapTextureAtlas mTextureAtlas;
    protected ITextureRegion mTextureRegion;

    private Set<T> mEntitiesToAddToWorld = Collections .synchronizedSet(new HashSet<T>());
    private Set<T> mEntitiesToRemoveFromWorld = Collections .synchronizedSet(new HashSet<T>());
    
    
    public EntityPool(MainActivity activity) throws IllegalArgumentException  {
        if (activity == null) throw new IllegalArgumentException ("BaseGameActivity is null");
        this.mActivity = activity;
        mVertexBufferObjectManager = activity.getVertexBufferObjectManager();
        this.setTextureAtlas(onCreateTextureAtlas());
        assert getTextureAtlas() instanceof BitmapTextureAtlas;
        
        this.onTextureAtlasCreated();

        this.setTextureRegion(onCreateTextureRegion());
        assert getTextureRegion() instanceof ITextureRegion;
    }
    
    public T addToWorld() {
        T item = this.obtainPoolItem();
        this.mEntitiesToAddToWorld.add(item);
        return item;
    }

    public void addToWorld(T pItem) {
        this.mEntitiesToAddToWorld.add(pItem);
    }

    public void removeFromWorld(T pItem) {
        this.mEntitiesToRemoveFromWorld.add(pItem);
    }
    
    @Override
	public void onUpdate(float pSecondsElapsed) {
	    recycleWorldItems();
	    addItemsToWorld();
	}
    
	@Override
	public void reset() {
	}

	private void addItemsToWorld() {
        synchronized (mEntitiesToAddToWorld) {
            for (T item : mEntitiesToAddToWorld) {
                onAddToWorld(item);
            }
            try {
                mEntitiesToAddToWorld.removeAll(mEntitiesToAddToWorld);
            } catch (UnsupportedOperationException  e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            } catch (NullPointerException  e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

	/**
	 * Defines how the object is added to the world
	 * @param pItem to be added
	 */
    protected abstract void onAddToWorld(T pItem);

	private void recycleWorldItems() {
        synchronized (mEntitiesToRemoveFromWorld) {              
            for (T item : mEntitiesToRemoveFromWorld) {
                recyclePoolItem(item);
            }
            try {
                mEntitiesToRemoveFromWorld.removeAll(mEntitiesToRemoveFromWorld);
            } catch (UnsupportedOperationException  e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            } catch (NullPointerException  e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    private void onTextureAtlasCreated() {
    	mTextureAtlas.load();
	}

	@Override
    protected void onHandleRecycleItem(final T pItem) {
        onRecycle(pItem);
    }

	/**
	 * Defines how the object is removed from the world
	 * @param pItem to be removed
	 */
	protected abstract void onRecycle(T pItem);

	public abstract BitmapTextureAtlas onCreateTextureAtlas();

	public abstract ITextureRegion onCreateTextureRegion();

	public BitmapTextureAtlas getTextureAtlas() {
		return mTextureAtlas;
	}

	public void setTextureAtlas(BitmapTextureAtlas pTextureAtlas) {
		mTextureAtlas = pTextureAtlas;
	}

	public ITextureRegion getTextureRegion() {
		return mTextureRegion;
	}

	public void setTextureRegion(ITextureRegion pTextureRegion) {
		mTextureRegion = pTextureRegion;
	}
    
}
