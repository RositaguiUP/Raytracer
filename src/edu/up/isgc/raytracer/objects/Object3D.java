/**
 * [1968] - [2021] Centros Culturales de Mexico A.C / Universidad Panamericana
 * All Rights Reserved.
 */
package edu.up.isgc.raytracer.objects;

import edu.up.isgc.raytracer.IIntersectable;
import edu.up.isgc.raytracer.Vector3D;
import edu.up.isgc.raytracer.materials.MaterialBP;
import javafx.scene.paint.Material;

import java.awt.*;

/**
 *
 * @author Jafet Rodríguez
 */
public abstract class Object3D implements IIntersectable {

    private Vector3D position;
    private Color color;
    private MaterialBP material;

    public Vector3D getPosition() {
        return position;
    }

    public void setPosition(Vector3D position) {
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public MaterialBP getMaterial() {
        return material;
    }

    public void setMaterial(MaterialBP material) {
        this.material = material;
    }

    public Object3D(Vector3D position, Color color) {
        setPosition(position);
        setColor(color);
    }

    public Object3D(Vector3D position, Color color, MaterialBP material) {
        setPosition(position);
        setColor(color);
        setMaterial(material);
    }

}
