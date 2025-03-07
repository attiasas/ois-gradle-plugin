package org.ois.example;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import org.ois.core.OIS;
import org.ois.core.entities.Entity;
import org.ois.core.entities.EntityManager;
import org.ois.core.state.managed.ManagedState;
import org.ois.core.tools.Timer;
import org.ois.example.entities.Circle;
import org.ois.example.entities.Rect;

public class EntitiesState extends ManagedState {

    FitViewport viewport;
    OrthographicCamera camera;
    ShapeRenderer shape;

    Timer timer;

    @Override
    public void enter(Object... parameters) {
        super.enter(parameters);
        shape = new ShapeRenderer();
        camera = new OrthographicCamera();
        viewport = new FitViewport(OIS.engine.getAppWidth(), OIS.engine.getAppHeight(), camera);
        viewport.update(OIS.engine.getAppWidth(), OIS.engine.getAppHeight());

        timer = new Timer(5);
        timer.setLoop(true);
        timer.setOnFinishListener(new TimerDoneAction(entityManager));
    }

    private static class TimerDoneAction implements Runnable {

        public EntityManager manager;

        public TimerDoneAction(EntityManager manager) {
            this.manager = manager;
        }

        @Override
        public void run() {
            manager.loadManifest(true);
        }
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        viewport.update(width, height);
    }

    @Override
    public boolean update(float dt) {
        if (!super.update(dt)) {
            return false;
        }
        timer.tic(dt);
        return true;
    }

    @Override
    public void render() {
        super.render();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        shape.setProjectionMatrix(camera.combined);

        for (Entity entity : entityManager.get("circle")) {
            Circle circle = (Circle) entity;
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(circle.color);
            shape.circle(circle.pos.x, circle.pos.y, circle.radius);
            shape.end();
        }

        for (Entity entity : entityManager.get("rect")) {
            Rect rect = (Rect) entity;
            shape.begin(ShapeRenderer.ShapeType.Filled);
            shape.setColor(rect.color);
            shape.rect(rect.pos.x, rect.pos.y, rect.width, rect.height);
            shape.end();
        }
    }
}
