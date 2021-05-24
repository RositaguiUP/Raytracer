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
 */
public abstract class Light extends Object3D {
    private double intensity;
    private int falloff;

    public Light(Vector3D position, Color color, double intensity, int falloff){
        super(position, color);
        setIntensity(intensity);
        setFalloff(falloff);
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }


    public int getFalloff() { return falloff; }

    public void setFalloff(int falloff) { this.falloff = falloff; }

    public abstract float getNDotL(Intersection intersection);

    public Intersection getIntersection(Ray ray){
        return new Intersection(Vector3D.ZERO(), -1, Vector3D.ZERO(), null);
    }
}
