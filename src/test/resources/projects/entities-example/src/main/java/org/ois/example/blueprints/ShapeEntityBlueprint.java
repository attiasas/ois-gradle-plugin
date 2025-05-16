package org.ois.example.blueprints;

import com.badlogic.gdx.graphics.Color;
import org.ois.core.components.IComponent;
import org.ois.core.entities.Entity;
import org.ois.core.project.blueprints.ComponentBlueprint;
import org.ois.core.project.blueprints.EntityBlueprint;
import org.ois.core.utils.io.data.properties.FloatProperty;
import org.ois.core.utils.io.data.properties.Properties;
import org.ois.core.utils.io.data.properties.Property;
import org.ois.example.components.entity.ShapeComponent;

import java.util.Random;

public class ShapeEntityBlueprint extends EntityBlueprint {

    public static Random random = new Random();

    @Override
    public void registerCustomComponentsBlueprints() {
        registerBlueprint("render", new ShapeRenderComponentBlueprint());
    }

    private static class ShapeRenderComponentBlueprint extends ComponentBlueprint<Entity> {

        private final Property<Color> color = Properties.color("color");
        private final Property<Float> width = new FloatProperty("width");
        private final Property<Float> height = new FloatProperty("height");
        private final Property<Float> radius = new FloatProperty("radius");

        public ShapeRenderComponentBlueprint() {
            registerProperty(color.setOptional(true).setDefaultValue(new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1)));
            registerProperty(width.setOptional(true).setDefaultValue(random.nextFloat() * 100));
            registerProperty(height.setOptional(true).setDefaultValue(random.nextFloat() * 100));
            registerProperty(radius.setOptional(true).setDefaultValue(random.nextFloat() * 100));
        }

        @Override
        public <C extends IComponent> C create() {
            return (C) new ShapeComponent(context, color.get(), radius.get(), width.get(), height.get());
        }
    }
}
