package org.ois.example.entities;

import org.ois.core.entities.Entity;
import org.ois.core.utils.io.data.DataNode;

public class Rect extends Shape {

    public float width;
    public float height;

    public Rect() {
        super("rect");
    }

    @Override
    public Entity loadData(DataNode data) {
        super.loadData(data);

        width = data.get("width").getFloat();
        height = data.get("height").getFloat();

        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = super.convertToDataNode();

        root.set("width", width);
        root.set("height", height);

        return root;
    }
}
