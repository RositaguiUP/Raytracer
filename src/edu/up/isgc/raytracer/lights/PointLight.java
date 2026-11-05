package edu.up.isgc.raytracer.lights;

import edu.up.isgc.raytracer.Intersection;
import edu.up.isgc.raytracer.Vector3D;

import java.awt.*;

/**
 *
 * @author Jafet Rodríguez
 * @author Rosita Aguirre
 * This is a class for lights that have a center
 *
 */
public class PointLight extends Light {
    private int falloff;

    public PointLight(Vector3D position, Color color, double intensity, int falloff){
        super(position, color, intensity);
        setFalloff(falloff);
    }

    /**
     * This method returns the dot product (float) between the light and the intersection
     *
     * @param intersection Object intersection to get its normal
     */
    @Override
    public float getNDotL(Intersection intersection) {
        Vector3D direction = Vector3D.normalize(Vector3D.substract(getPosition(),intersection.getPosition()));
        return (float)Math.max(Vector3D.dotProduct(intersection.getNormal(), direction), 0.0);
    }

    /**
     *
     * Getters and Setters
     *
     */
    public int getFalloff() { return falloff; }

    public void setFalloff(int falloff) { this.falloff = falloff; }

}
