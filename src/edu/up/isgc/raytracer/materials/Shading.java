package edu.up.isgc.raytracer.materials;

import edu.up.isgc.raytracer.Raytracer;
import edu.up.isgc.raytracer.Intersection;
import edu.up.isgc.raytracer.Ray;
import edu.up.isgc.raytracer.Vector3D;
import edu.up.isgc.raytracer.lights.Light;
import edu.up.isgc.raytracer.lights.PointLight;
import edu.up.isgc.raytracer.objects.Object3D;

import java.awt.*;
import java.util.ArrayList;

public class Shading {

    private Shading() {
    }

    public static Color getPixelColor(Ray ray, ArrayList<Light> lights, ArrayList<Object3D> objects, Intersection closestIntersection,
                                      Color pixelColor, float ambientLight, boolean addedAmbient, int totalDepth, int reflectionDepth, int maxDepth){
        for (Light light : lights) {
            Ray intersectionLightRay = new Ray(closestIntersection.getPosition(), light.getPosition());
            Intersection shadowIntersection = Raytracer.raycast(intersectionLightRay, objects, closestIntersection.getObject(), null);

            float intensity = (float) light.getIntensity();
            Color lightColor = light.getColor();
            Color objColor = closestIntersection.getObject().getColor();

            // Add ambient
            if (!addedAmbient) {
                pixelColor = shading(lightColor, objColor, pixelColor, intensity, ambientLight);
                addedAmbient = true;
            }

            if (shadowIntersection == null) {
                Material objMaterial = closestIntersection.getObject().getMaterial();

                // Add diffuse
                float nDotL = light.getNDotL(closestIntersection);
                float diffuseIntensity = intensity * nDotL;
                if (light instanceof PointLight) {
                    PointLight pointLight = (PointLight) light;
                    diffuseIntensity /= Math.pow(Vector3D.magnitude(Vector3D.substract(light.getPosition(),
                            closestIntersection.getPosition())), pointLight.getFalloff());
                }
                pixelColor = shading(lightColor, objColor, pixelColor, diffuseIntensity, (float) objMaterial.getDiffuse());

                // Add specular
                Vector3D vectorH = Vector3D.normalize(Vector3D.add(light.getPosition(), ray.getDirection()));
                Vector3D direction = Vector3D.normalize(Vector3D.substract(vectorH, closestIntersection.getPosition()));
                float specularIntensity = (float) (Math.pow(Math.max(Vector3D.dotProduct(closestIntersection.getNormal(),
                        direction), 0.0), (float) objMaterial.getShininess()));
                pixelColor = shading(lightColor, objColor, pixelColor, specularIntensity, (float) objMaterial.getSpecular());

                if (totalDepth < maxDepth) {
                    // Transparency
                    if (objMaterial.getTransparency() > 0 && reflectionDepth < maxDepth) {
                        Ray intersectionTransparencyRay = new Ray(closestIntersection.getPosition(), ray.getDirection());
                        Intersection transparencyIntersection = Raytracer.raycast(intersectionTransparencyRay, objects, closestIntersection.getObject(),
                                null);
                        if (transparencyIntersection != null) {
                            totalDepth += 1;
                            Color transparencyIntersectionObjColor = getPixelColor(ray, lights, objects, transparencyIntersection, Color.BLACK, ambientLight,
                                    false, totalDepth, reflectionDepth, maxDepth);
                            pixelColor = shading(pixelColor, transparencyIntersectionObjColor, pixelColor, (float) objMaterial.getTransparency(), 1f);
                        }
                    }

                    // Reflection
                    if (objMaterial.getIndexOfReflexion() > 0 && reflectionDepth < maxDepth) {
                        Vector3D reflectionDirection = Vector3D.substract(ray.getDirection(), Vector3D.scalarMultiplication(
                                Vector3D.scalarMultiplication(closestIntersection.getNormal(),
                                        Vector3D.dotProduct(ray.getDirection(), closestIntersection.getNormal())), 2));
                        Ray intersectionReflectionRay = new Ray(closestIntersection.getPosition(), reflectionDirection);
                        Intersection reflectionIntersection = Raytracer.raycast(intersectionReflectionRay, objects, closestIntersection.getObject(),
                                null);
                        if (reflectionIntersection != null) {
                            reflectionDepth += 1;
                            totalDepth += 1;
                            Color reflectedObjColor = getPixelColor(ray, lights, objects, reflectionIntersection, Color.BLACK, ambientLight,
                                    false, totalDepth, reflectionDepth, maxDepth);
                            pixelColor = shading(pixelColor, reflectedObjColor, pixelColor, (float) objMaterial.getIndexOfReflexion(), 1f);
                        }
                    }

                    // Refraction
                    if (objMaterial.getIndexOfRefraction() > 0) {
                        Vector3D refractionDirection = transmitionRay(ray.getDirection(), closestIntersection.getNormal(),
                                (float) objMaterial.getIndexOfRefraction());
                        Ray intersectionRefractionRay = new Ray(closestIntersection.getPosition(), refractionDirection);
                        Intersection refractionIntersection = Raytracer.raycast(intersectionRefractionRay, objects, closestIntersection.getObject(),
                                null);
                        if (refractionIntersection != null) {
                            totalDepth += 1;
                            Color refractedObjColor = getPixelColor(ray, lights, objects, refractionIntersection, Color.BLACK,
                                    ambientLight, false, totalDepth, 0, maxDepth);
                            pixelColor = shading(pixelColor, refractedObjColor, pixelColor, (float) objMaterial.getIndexOfRefraction(), 1f);
                        }
                    }
                }
            }
        }
        return pixelColor;
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }

    public static Color addColor(Color original, Color otherColor){
        float red = clamp((original.getRed() / 255.0f) + (otherColor.getRed() / 255.0f), 0, 1);
        float green = clamp((original.getGreen() / 255.0f) + (otherColor.getGreen() / 255.0f), 0, 1);
        float blue = clamp((original.getBlue() / 255.0f) + (otherColor.getBlue() / 255.0f), 0, 1);
        return new Color(red, green, blue);
    }

    public static Color shading(Color lightColor, Color objColor, Color pixelColor, float lightIntensity, float multiplier){
        float[] lightRGB = new float[]{lightColor.getRed() / 255.0f, lightColor.getGreen() / 255.0f, lightColor.getBlue() / 255.0f};
        float[] objRGB = new float[]{objColor.getRed() / 255.0f, objColor.getGreen() / 255.0f, objColor.getBlue() / 255.0f};
        for (int colorIndex = 0; colorIndex < objRGB.length; colorIndex++) {
            objRGB[colorIndex] *= multiplier * lightIntensity * lightRGB[colorIndex];
        }
        Color colorToAdd = new Color(clamp(objRGB[0], 0, 1), clamp(objRGB[1], 0, 1), clamp(objRGB[2], 0, 1));
        return addColor(pixelColor, colorToAdd);
    }

    public static Vector3D transmitionRay(Vector3D i, Vector3D n,float ior)
    {
        float nDotL = (float) Vector3D.dotProduct(i, n);
        float cosi = clamp(-1, 1, nDotL);
        float etai = 1, etat = ior;
        Vector3D nN = n;
        if (cosi < 0) { cosi = -cosi; } else {
            float temp = etai;
            etai = etat;
            etat = temp;
            nN= Vector3D.scalarMultiplication(n, -1); }
        float eta = etai / etat;
        float k = 1 - eta * eta * (1 - cosi * cosi);
        return k < 0 ? null : Vector3D.add(
                Vector3D.scalarMultiplication(i, eta),
                Vector3D.scalarMultiplication(nN, eta * cosi - Math.sqrt(k)) );
    }
}
