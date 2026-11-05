/**
 * [1968] - [2021] Centros Culturales de Mexico A.C / Universidad Panamericana
 * All Rights Reserved.
 */
package edu.up.isgc.raytracer;

/**
 *
 * @author Jafet Rodríguez
 * @author Rosita Aguirre
 * This is an insterface to indicate if some class is intersectable
 *
 */
public interface IIntersectable {
    public abstract Intersection getIntersection(Ray ray);
}
