package org.ois.example.entities;

import org.ois.core.entities.Entity;
import org.ois.core.utils.io.data.DataNode;

public class Circle extends Shape {

    public float radius;

    public Circle() {
        super("circle");
    }

    @Override
    public Entity loadData(DataNode data) {
        super.loadData(data);

        radius = data.get("radius").getFloat();

        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = super.convertToDataNode();

        root.set("radius", radius);

        return root;
    }
}
