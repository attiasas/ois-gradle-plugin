package org.ois.example.components.entity;

import com.badlogic.gdx.graphics.Color;
import org.ois.core.components.Component;
import org.ois.core.entities.Entity;
import org.ois.core.utils.io.data.properties.FloatProperty;
import org.ois.core.utils.io.data.properties.Properties;
import org.ois.core.utils.io.data.properties.Property;

public class ShapeComponent extends Component {

    public Entity entity;

    private final Property<Color> color = Properties.color("color");

    private final Property<Float> width = new FloatProperty("width");
    private final Property<Float> height = new FloatProperty("height");
    private final Property<Float> radius = new FloatProperty("radius");

    public ShapeComponent(Entity entity, Color colorValue, float radiusValue, float widthValue, float heightValue) {
        this.entity = entity;

        registerProperty(color.set(colorValue).setOptional(true));
        registerProperty(width.setOptional(true).setDefaultValue(widthValue));
        registerProperty(height.setOptional(true).setDefaultValue(heightValue));
        registerProperty(radius.setOptional(true).setDefaultValue(radiusValue));
    }

    @Override
    public void update() {
        switch (this.entity.getType()) {
            case "circle":

                break;
            case "rect":

                break;
        }
    }
}
