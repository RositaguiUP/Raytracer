/**
 * [1968] - [2021] Centros Culturales de Mexico A.C / Universidad Panamericana
 * All Rights Reserved.
 */
package edu.up.isgc.raytracer;

import edu.up.isgc.raytracer.lights.*;
import edu.up.isgc.raytracer.materials.*;
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
 * @author Rosita Aguirre Plascencia
 * This is the main class to do the ray tracing
 *
 */
public class Raytracer {
    /**
     * This is the Main
     *
     * @param args
     *
     */
    public static void main(String[] args) {
        System.out.println(new Date());
        Scene scene01 = new Scene(0.1f);
        scene01.setCamera(new Camera(new Vector3D(0, 0, -9), 160, 160, 1200,
                1200, 0f, 50f));

        // Lights
        scene01.addLight(new PointLight(new Vector3D(5f, 2f, 2f), Color.WHITE, 4f, 3));
        /*scene01.addLight(new DirectionalLight(new Vector3D(0f, 8f, 0f), new Vector3D(0f, -8f, 0f), Color.WHITE, 2f));
        scene01.addLight(new PointLight(new Vector3D(0f, 3f, -5f), Color.WHITE, 2f, 1));*/

        // Objects
        /*scene01.addObject(OBJReader.GetPolygon("./objs/Floor.obj", new Vector3D(0f, -3f, 0f), Color.WHITE,
                new Material( true,0.8f, 0.3f, 10f, 0f, 2f, 0f)));
        scene01.addObject(OBJReader.GetPolygon("./objs/PianoSpecial.obj", new Vector3D(0f, 0f, 5f), Color.WHITE,
                new Material( true,0.3f, 0.2f, 10f, 0f, 0.3f, 0f)));
        scene01.addObject(OBJReader.GetPolygon("./objs/SmallTeapot.obj", new Vector3D(0f, -1f, 3f), Color.RED,
                new Material( true,0.3f, 0.8f, 50f, 0f, 0.5f, 0f)));
        scene01.addObject(new Sphere(new Vector3D(0f, 0f, -5f), 1f, Color.WHITE,
                new Material(true, 0.2f, 0.6f, 80f,0f, 0.2f,1.5f)));
        scene01.addObject(OBJReader.GetPolygon("./objs/Floor.obj", new Vector3D(0f, -2f, 0f), Color.WHITE,
                new Material( true,0.8f, 1f, 10f, 0f, 2f, 0f)));
        scene01.addObject(new Sphere(new Vector3D(0f, -0.5f, 3f), 0.5f, Color.WHITE,
                new Material(true, 0.2f, 0.6f, 20f,0.2f, 0.8f,0f)));
        scene01.addObject(OBJReader.GetPolygon("./objs/Cube.obj", new Vector3D(0f, -0.5f, 1f), Color.RED,
                new Material(true, 0.2f, 0.6f, 20f, 0f, 0f, 1.5f)));*/

        scene01.addObject(OBJReader.GetPolygon("./objs/Floor.obj", new Vector3D(0f, -2f, 0f), Color.WHITE,
                new Material( true,1f, 1f, 20f, 0f,0.8f, 0f)));
        scene01.addObject(OBJReader.GetPolygon("./objs/SmallTeapot.obj", new Vector3D(0f, -2f, 2f), Color.RED,
                new Material(true,1f,1f, 20f,0f,0f,0f)));
        scene01.addObject(new Sphere(new Vector3D(0.5f, -0.8f, -3f), 0.5f, Color.WHITE,
                new Material(true, 1f, 1f, 20f,0f, 0f,1.8f)));

        BufferedImage image = raytrace(scene01);
        File outputImage = new File("image.png");
        try {
            ImageIO.write(image, "png", outputImage);
        } catch (IOException ioe) {
            System.out.println("Something failed");
        }
        System.out.println(new Date());
    }

    /**
     * This is an static method that creates the png
     *
     * @param scene Scene to ray trace
     *
     */
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
                    pixelColor = Shading.getPixelColor(ray, lights, objects, closestIntersection, pixelColor, (float) scene.getAmbientLight(),
                             false, 0, 0,5);
                }
                image.setRGB(i, j, pixelColor.getRGB());

                Loader(i, j, mainCamera.getResolutionWidth(), mainCamera.getResolutionHeight());
            }
        }
        return image;
    }

    /**
     * This method returns an intersection with the ray casted
     *
     * @param ray Ray casted
     * @param objects All the objectson the scene
     * @param caster Current object for caster
     * @param clippingPlanes Clipping planes
     */
    public static Intersection raycast(Ray ray, ArrayList<Object3D> objects, Object3D caster, float[] clippingPlanes) {
        Intersection closestIntersection = null;

        for (Object3D currentObj : objects) {
            if (!currentObj.equals(caster)) {
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

    /**
     * This method print the percentage advanced
     *
     * @param i Position on i
     * @param j Position on j
     * @param widthResolution Width resolution of the image
     * @param heightResolution Height resolution of the image
     */
    public static void Loader(int i, int j, int widthResolution, int heightResolution){
        int total = widthResolution*heightResolution;
        int actualPixel = (i+1) * widthResolution + (j+1);

        float percentage = (float) actualPixel*100/total;
        if (percentage%10 == 0){
            System.out.println("\tActual percentage: " + percentage + "% --- Pixel " + actualPixel + " of " + total + " --- " + new Date());
        }
    }

}
