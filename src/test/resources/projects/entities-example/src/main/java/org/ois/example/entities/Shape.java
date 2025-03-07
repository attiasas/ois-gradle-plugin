package org.ois.example.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import org.ois.core.entities.Entity;
import org.ois.core.utils.io.data.DataNode;

import java.util.Random;

public class Shape extends Entity {

    public static Random random = new Random();
    public static float speed = 2;

    public Vector2 pos = new Vector2();
    public Vector2 v;
    public Color color;

    protected Shape(String type) {
        super(type);

        v = new Vector2(random.nextFloat() * speed, random.nextFloat() * speed);
        color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1);
    }

    @Override
    public void update() {
        super.update();
        pos = pos.add(v);
    }

    @Override
    public Entity loadData(DataNode data) {
        super.loadData(data);

        if (data.contains("x")) {
            pos.x = data.get("x").getFloat();
        }
        if (data.contains("y")) {
            pos.y = data.get("y").getFloat();
        }

        if (data.contains("v")) {
            DataNode vNode = data.get("v");
            if (vNode.contains("x")) {
                v.x = vNode.get("x").getFloat();
            }
            if (vNode.contains("y")) {
                v.y = vNode.get("y").getFloat();
            }
        }

        if (data.contains("color")) {
            DataNode colorNode = data.get("color");
            color.set(
                    colorNode.contains("r") ? colorNode.get("r").getFloat() : 0,
                    colorNode.contains("g") ? colorNode.get("g").getFloat() : 0,
                    colorNode.contains("b") ? colorNode.get("b").getFloat() : 0,
                    colorNode.contains("a") ? colorNode.get("a").getFloat() : 1
            );
        }

        return this;
    }

    @Override
    public DataNode convertToDataNode() {
        DataNode root = super.convertToDataNode();

        root.set("x", pos.x);
        root.set("y", pos.y);

        DataNode vNode = root.getProperty("v");
        vNode.set("x", v.x);
        vNode.set("y", v.y);

        DataNode colorNode = root.getProperty("color");
        colorNode.set("r", color.r);
        colorNode.set("g", color.g);
        colorNode.set("b", color.b);
        colorNode.set("a", color.a);

        return root;
    }
}
