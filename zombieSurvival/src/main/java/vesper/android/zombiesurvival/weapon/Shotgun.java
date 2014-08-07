package vesper.android.zombiesurvival.weapon;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import vesper.android.zombiesurvival.Character;

public class Shotgun extends Weapon {
    public Shotgun(Character pParent) { super(pParent); }

    @Override
    public HUD getHUD(Camera pCamera, VertexBufferObjectManager pVertexBufferObjectManager) {
        return null;
    }
}
