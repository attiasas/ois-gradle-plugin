package org.ois.example.blueprints;

import org.ois.core.components.IComponent;
import org.ois.core.project.blueprints.ComponentBlueprint;
import org.ois.core.project.blueprints.StateBlueprint;
import org.ois.core.state.managed.IManagedState;
import org.ois.core.utils.io.data.properties.BooleanProperty;
import org.ois.core.utils.io.data.properties.FloatProperty;
import org.ois.core.utils.io.data.properties.Property;
import org.ois.example.components.state.ReloadComponent;

public class EntitiesStateBlueprint extends StateBlueprint {

    @Override
    public void registerCustomComponentsBlueprints() {
        registerBlueprint("reloadState", new ReloadComponentBlueprint());
    }

    private static class ReloadComponentBlueprint extends ComponentBlueprint<IManagedState> {

        Property<Float> target = new FloatProperty("target");
        Property<Boolean> loop = new BooleanProperty("loop");

        public ReloadComponentBlueprint() {
            registerProperty(target.setOptional(true).setDefaultValue(5f));
            registerProperty(loop.setOptional(true).setDefaultValue(true));
        }

        @Override
        public <C extends IComponent> C create() {
            return (C) new ReloadComponent(context.getEntityManager(), target.get(), loop.get());
        }
    }
}
