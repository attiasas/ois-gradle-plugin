package org.example;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import org.ois.core.OIS;
import org.ois.core.project.Assets;
import org.ois.core.state.IState;
import org.ois.core.tools.Timer;

public class RedState implements IState {
    private final Timer timer = new Timer(5);

    public RedState(){}

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
        ScreenUtils.clear(1,0,0, 1f);
        batch.begin();
        batch.draw(img, OIS.engine.getAppWidth() - img.getWidth(), OIS.engine.getAppHeight() - img.getHeight());
        batch.end();
    }

    @Override
    public boolean update(float dt) {
        if (timer.tic(dt)) {
            OIS.engine.stateManager.changeState("Green");
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
