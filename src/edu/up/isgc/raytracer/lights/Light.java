/**
 * [1968] - [2021] Centros Culturales de Mexico A.C / Universidad Panamericana
 * All Rights Reserved.
 */
package edu.up.isgc.raytracer.lights;

import edu.up.isgc.raytracer.Intersection;
import edu.up.isgc.raytracer.Ray;
import edu.up.isgc.raytracer.Vector3D;
import edu.up.isgc.raytracer.objects.Object3D;

import java.awt.*;

/**
 *
 * @author Jafet Rodríguez
 * @author Rosita Aguirre
 * This is a parent class for lights
 *
 */
public abstract class Light extends Object3D {
    private double intensity;

    public Light(Vector3D position, Color color, double intensity){
        super(position, color);
        setIntensity(intensity);
    }

    /**
     * This is an abstract that returns the dot product (float)
     *
     * @param intersection Object intersections
     */
    public abstract float getNDotL(Intersection intersection);

    /**
     * This method returns a new intersection
     *
     * @param ray
     */
    public Intersection getIntersection(Ray ray){
        return new Intersection(Vector3D.ZERO(), -1, Vector3D.ZERO(), null);
    }

    /**
     *
     * Getters and Setters
     *
     */
    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }

}
