package edu.up.isgc.raytracer.lights;

import edu.up.isgc.raytracer.Intersection;
import edu.up.isgc.raytracer.Vector3D;

import java.awt.*;

public class PointLight extends Light {

    public PointLight(Vector3D position, Color color, double intensity){
        super(position, color, intensity);
    }

    @Override
    public float getNDotL(Intersection intersection) {
        Vector3D direction = Vector3D.normalize(Vector3D.substract(getPosition(),intersection.getPosition()));
        return (float)Math.max(Vector3D.dotProduct(intersection.getNormal(), direction), 0.0);
    }
}
