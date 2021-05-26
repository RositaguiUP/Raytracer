/**
 * [1968] - [2021] Centros Culturales de Mexico A.C / Universidad Panamericana
 * All Rights Reserved.
 */
package edu.up.isgc.raytracer;

import edu.up.isgc.raytracer.lights.*;
import edu.up.isgc.raytracer.materials.MaterialBP;
import edu.up.isgc.raytracer.objects.*;
import edu.up.isgc.raytracer.tools.OBJReader;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

/**
 * @author Jafet Rodríguez
 */
public class Raytracer {

    public static void main(String[] args) {
        System.out.println(new Date());
        Scene scene01 = new Scene();
        scene01.setCamera(new Camera(new Vector3D(0, 0, -8), 160, 160, 800,
                800, 0f, 50f));
        /*scene01.addLight(new DirectionalLight(Vector3D.ZERO(), new Vector3D(0.0, 0.0, 1.0), Color.WHITE, 0.8));
        scene01.addLight(new DirectionalLight(Vector3D.ZERO(), new Vector3D(0.0, -0.1, 0.1), Color.WHITE, 0.2));
        scene01.addLight(new DirectionalLight(Vector3D.ZERO(), new Vector3D(-0.2, -0.1, 0.0), Color.WHITE, 0.2));*/

        //scene01.addLight(new DirectionalLight(Vector3D.ZERO(), new Vector3D(0.0, 0, 0), Color.WHITE, 0.8, 1));
        scene01.addLight(new PointLight(new Vector3D(0f, 4f, 0.5f), Color.WHITE, 4, 1));
        //scene01.addLight(new PointLight(new Vector3D(1.5f, 1f, 1f), Color.WHITE, 2, 1));
        scene01.addObject(OBJReader.GetPolygon("./objs/Floor.obj", new Vector3D(0f, -1.5f, 1f), Color.GRAY,
                new MaterialBP(0.001f, 0.4f, 0.5f, 10f, 0.9f, 0f)));
        /*scene01.addObject(OBJReader.GetPolygon("./objs/SmallTeapot.obj", new Vector3D(2f, -0.5f, 3f),
                Color.RED, new MaterialBP(0.1f, 1f, 1f, 20f, 0.8f, 0f)));*/
        scene01.addObject(new Sphere(new Vector3D(0f, -0.5f,3f), 0.5f, Color.WHITE,
                new MaterialBP(0.1f, 0.2f, 0.6f, 20f, 0.8f, 1f)));
        scene01.addObject(OBJReader.GetPolygon("./objs/Cube.obj", new Vector3D(0f, -0.5f, 1f), Color.RED,
                new MaterialBP(0.1f, 0.2f, 0.6f, 20f, 0f, 1.5f)));

        BufferedImage image = raytrace(scene01);
        File outputImage = new File("image.png");
        try {
            ImageIO.write(image, "png", outputImage);
        } catch (IOException ioe) {
            System.out.println("Something failed");
        }
        System.out.println(new Date());
    }

    public static BufferedImage raytrace(Scene scene) {
        Camera mainCamera = scene.getCamera();
        ArrayList<Light> lights = scene.getLights();
        float[] nearFarPlanes = mainCamera.getNearFarPlanes();
        BufferedImage image = new BufferedImage(mainCamera.getResolutionWidth(), mainCamera.getResolutionHeight(), BufferedImage.TYPE_INT_RGB);
        ArrayList<Object3D> objects = scene.getObjects();

        Vector3D[][] positionsToRaytrace = mainCamera.calculatePositionsToRay();
        for (int i = 0; i < positionsToRaytrace.length; i++) {
            for (int j = 0; j < positionsToRaytrace[i].length; j++) {
                double x = positionsToRaytrace[i][j].getX() + mainCamera.getPosition().getX();
                double y = positionsToRaytrace[i][j].getY() + mainCamera.getPosition().getY();
                double z = positionsToRaytrace[i][j].getZ() + mainCamera.getPosition().getZ();

                Ray ray = new Ray(mainCamera.getPosition(), new Vector3D(x, y, z));
                float cameraZ = (float) mainCamera.getPosition().getZ();
                Intersection closestIntersection = raycast(ray, objects, null, new float[]{cameraZ + nearFarPlanes[0],
                        cameraZ + nearFarPlanes[1]});

                //Background color
                Color pixelColor = Color.BLACK;
                if (closestIntersection != null) {
                    pixelColor = getPixelColor(ray, lights, objects, closestIntersection, pixelColor, 0, 5);
                }
                image.setRGB(i, j, pixelColor.getRGB());
            }
        }
        return image;
    }

    public static Color getPixelColor(Ray ray, ArrayList<Light> lights, ArrayList<Object3D> objects, Intersection closestIntersection,
                                      Color pixelColor, int depth, int maxDepth){
        for (Light light : lights) {
            Ray intersectionLightRay = new Ray(closestIntersection.getPosition(), light.getPosition());
            Intersection shadowIntersection = raycast(intersectionLightRay, objects, closestIntersection.getObject(), null);
            if (shadowIntersection == null) {
                float intensity = (float) light.getIntensity();
                Color lightColor = light.getColor();
                Color objColor = closestIntersection.getObject().getColor();
                MaterialBP objMaterial = closestIntersection.getObject().getMaterial();

                // Add ambient
                pixelColor = shading(lightColor, objColor, pixelColor, intensity, (float) objMaterial.getAmbient());

                // Add diffuse
                float nDotL = light.getNDotL(closestIntersection);
                float diffuseIntensity = intensity * nDotL;
                float lightFalloff = (float) (diffuseIntensity / Math.pow(Vector3D.magnitude(Vector3D.substract(light.getPosition(),
                        closestIntersection.getPosition())), light.getFalloff()));
                pixelColor = shading(lightColor, objColor, pixelColor, lightFalloff, (float) objMaterial.getDiffuse());

                // Add specular
                Vector3D vectorH = Vector3D.normalize(Vector3D.add(light.getPosition(), ray.getDirection()));
                Vector3D direction = Vector3D.normalize(Vector3D.substract(vectorH, closestIntersection.getPosition()));
                float specularIntensity = (float) (Math.pow(Math.max(Vector3D.dotProduct(closestIntersection.getNormal(),
                        direction), 0.0), (float) objMaterial.getShininess()));
                pixelColor = shading(lightColor, objColor, pixelColor, specularIntensity, (float) objMaterial.getSpecular());

                /* Refraction
                float nDotL = light.getNDotL(closestIntersection);
                MaterialBP objMaterial = closestIntersection.getObject().getMaterial();

                Vector3D transmitionVector = transmitionRay(light.getPosition(), nDotL, closestIntersection.getNormal(), (float) objMaterial.getIndexOfRefraction());
                if (transmitionVector == null) {
                */

                // Reflection
                if (objMaterial.getIndexOfReflexion() > 0 && depth < maxDepth) {
                    Vector3D reflectionDirection = Vector3D.substract(ray.getDirection(), Vector3D.scalarMultiplication(
                            Vector3D.scalarMultiplication(closestIntersection.getNormal(),
                            Vector3D.dotProduct(ray.getDirection(), closestIntersection.getNormal())), 2));
                    Ray intersectionReflectionRay = new Ray(closestIntersection.getPosition(), reflectionDirection);
                    Intersection reflectionIntersection = raycast(intersectionReflectionRay, objects, closestIntersection.getObject(), null);
                    if (reflectionIntersection != null) {
                        depth += 1;
                        Color reflectedObjColor = getPixelColor(ray, lights, objects, reflectionIntersection, Color.BLACK, depth, maxDepth);
                        pixelColor = shading(pixelColor, reflectedObjColor, pixelColor, (float) objMaterial.getIndexOfReflexion(), 1f);
                    }
                }

                // Refraction
                if (objMaterial.getIndexOfRefraction() > 0){
                    Vector3D refractionDirection = transmitionRay(ray.getDirection(), closestIntersection.getNormal(), (float) objMaterial.getIndexOfRefraction());
                    Ray intersectionRefractionRay = new Ray(closestIntersection.getPosition(), refractionDirection);
                    Intersection refractionIntersection = raycast(intersectionRefractionRay, objects, closestIntersection.getObject(), null);
                    if (refractionIntersection != null) {
                        Color refractedObjColor = getPixelColor(ray, lights, objects, refractionIntersection, Color.BLACK, 0, maxDepth);
                        pixelColor = shading(pixelColor, refractedObjColor, pixelColor, (float) objMaterial.getIndexOfRefraction(), 1f);
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
        if (value > max) {
            return max;
        }
        return value;
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

    public static Intersection raycast(Ray ray, ArrayList<Object3D> objects, Object3D caster, float[] clippingPlanes) {
        Intersection closestIntersection = null;

        for (int k = 0; k < objects.size(); k++) {
            Object3D currentObj = objects.get(k);
            if (caster == null || !currentObj.equals(caster)) {
                Intersection intersection = currentObj.getIntersection(ray);
                if (intersection != null) {
                    double distance = intersection.getDistance();
                    if (distance >= 0 &&
                            (closestIntersection == null || distance < closestIntersection.getDistance()) &&
                            (clippingPlanes == null || (intersection.getPosition().getZ() >= clippingPlanes[0] &&
                                    intersection.getPosition().getZ() <= clippingPlanes[1]))) {
                        closestIntersection = intersection;
                    }
                }
            }
        }

        return closestIntersection;
    }

}
