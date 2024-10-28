package org.example;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import org.ois.core.OIS;
import org.ois.core.project.Assets;
import org.ois.core.state.IState;
import org.ois.core.tools.Timer;

public class GreenState implements IState {
    private final Timer timer = new Timer(5);

    public GreenState(){}

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
        ScreenUtils.clear(0,1,0, 1f);
        batch.begin();
        batch.draw(img, 0, 0);
        batch.end();
    }

    @Override
    public boolean update(float dt) {
        if (timer.tic(dt)) {
            OIS.engine.stateManager.changeState("Blue");
        }
        return true;
    }

    @Override
    public void dispose() {
        batch.dispose();
        img.dispose();
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
