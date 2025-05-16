package org.ois.example.components.state;

import org.ois.core.OIS;
import org.ois.core.components.Component;
import org.ois.core.entities.EntityManager;
import org.ois.core.tools.Timer;
import org.ois.core.utils.log.Logger;

// Reload entities after timer is up
public class ReloadComponent extends Component {
    private static final Logger<ReloadComponent> log = Logger.get(ReloadComponent.class);

    private final Timer timer;

    public ReloadComponent(EntityManager manager, float target, boolean loop) {
        timer = new Timer(target);
        timer.setLoop(loop);
        timer.setOnFinishListener(new TimerDoneAction(manager));
    }

    @Override
    public void update() {
        timer.tic(OIS.deltaTime);
    }

    private static class TimerDoneAction implements Runnable {

        public EntityManager manager;

        public TimerDoneAction(EntityManager manager) {
            this.manager = manager;
        }

        @Override
        public void run() {
            log.info("DONE TIMER");
            manager.loadManifest(true);
        }
    }
}
