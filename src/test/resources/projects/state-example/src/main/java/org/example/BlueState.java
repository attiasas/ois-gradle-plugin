package org.example;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import org.ois.core.OIS;
import org.ois.core.project.Assets;
import org.ois.core.state.IState;
import org.ois.core.tools.Timer;

public class BlueState implements IState {
    private final Timer timer = new Timer(5);

    SpriteBatch batch;
    Texture img;

    @Override
    public void enter(Object... parameters) {
        batch = new SpriteBatch();
        img = new Texture(Assets.get("test.png"));

        timer.reset();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0,0,1, 1f);
        batch.begin();
        batch.draw(img, (OIS.engine.getAppWidth() / 2f) - (img.getWidth() / 2f), (OIS.engine.getAppHeight() / 2f) - (img.getHeight() / 2f));
        batch.end();
    }

    @Override
    public boolean update(float dt) {
        if (timer.tic(dt)) {
            OIS.engine.stateManager.changeState("Red");
        }
        return true;
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
        if (img != null) {
            img.dispose();
        }
    }

    @Override
    public void exit() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void resize(int width, int height) {

    }

}
