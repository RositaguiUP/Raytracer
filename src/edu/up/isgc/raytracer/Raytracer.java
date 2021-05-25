/**
 * [1968] - [2021] Centros Culturales de Mexico A.C / Universidad Panamericana
 * All Rights Reserved.
 */
package edu.up.isgc.raytracer;

import edu.up.isgc.raytracer.lights.Light;
import edu.up.isgc.raytracer.lights.PointLight;
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

        scene01.addLight(new PointLight(new Vector3D(0f, 5f, 1f), Color.WHITE, 4, 1));
        //scene01.addLight(new PointLight(new Vector3D(1.5f, 1f, 1f), Color.WHITE, 2, 1));
        scene01.addObject(OBJReader.GetPolygon("./objs/Floor.obj", new Vector3D(0f, -1.5f, 1f), Color.GRAY,
                new MaterialBP(0.05, 1f, 1f, 10f, 0f, 0f)));
        scene01.addObject(OBJReader.GetPolygon("./objs/SmallTeapot.obj", new Vector3D(0f, -1.5f, 1f),
                Color.RED, new MaterialBP(0.1f, 1f, 1f, 20f, 0f, 0f)));
        //scene01.addObject(new Sphere(new Vector3D(0f, -0.5f,3f), 0.5f, Color.YELLOW));

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
                    pixelColor = Color.BLACK;
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
                            Vector3D direction = Vector3D.normalize(Vector3D.substract(vectorH,closestIntersection.getPosition()));
                            float specularIntensity = (float) (Math.pow(Math.max(Vector3D.dotProduct(closestIntersection.getNormal(),
                                    direction),0.0), (float) objMaterial.getShininess()));
                            pixelColor = shading(lightColor, objColor, pixelColor, specularIntensity, (float) objMaterial.getSpecular());

                            // ambien 0.1 shan 20 reflec 0.8 dif 1
                        }
                    }
                }
                image.setRGB(i, j, pixelColor.getRGB());
            }
        }

        return image;
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

    public static Vector3D reflectionRay(Vector3D i, Vector3D n)
    {
        return Vector3D.substract(i, Vector3D.scalarMultiplication(Vector3D.scalarMultiplication(n, Vector3D.dotProduct(i, n)), 2));
    }

    /*public static Color castRay( Vector3D origin, Vector3D direction, ArrayList<Object3D> objects, ArrayList<Light> lights, int maxDepth, Color backgroundColor, int depth) {
        if (depth > maxDepth) return backgroundColor;
        if (trace(origin, direction, objects, isect)) {
        ...
            switch (isect.hitObject->type) {
                case kDiffuse:
            ...
                case kReflection:
                {
                    Vector3D R = reflectionRay(direction, hitNormal);
                    hitColor += 0.8 * castRay(hitPoint + hitNormal * options.bias, R, objects, lights, maxDepth, backgroundColor, depth + 1);
                    break;
                }
            ...
            }
        }
        return hitColor;
    }*/

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
