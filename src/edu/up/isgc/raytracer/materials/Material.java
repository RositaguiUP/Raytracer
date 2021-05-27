package edu.up.isgc.raytracer.materials;

public class Material {
    private boolean phong;
    private double diffuse;
    private double specular;
    private double shininess;
    private double indexOfReflexion;
    private double indexOfRefraction;

    public Material(boolean phong, double diffuse, double specular, double shininess, double indexOfReflexion,
                    double indexOfRefraction) {
        setPhong(phong);
        setDiffuse(diffuse);
        setSpecular(specular);
        setShininess(shininess);
        setIndexOfReflexion(indexOfReflexion);
        setIndexOfRefraction(indexOfRefraction);
    }

    public boolean isPhong() { return phong; }

    public void setPhong(boolean phong) { this.phong = phong; }

    public double getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(double diffuse) {
        this.diffuse = diffuse;
    }

    public double getSpecular() {
        return specular;
    }

    public void setSpecular(double specular) {
        this.specular = specular;
    }

    public double getShininess() {
        return shininess;
    }

    public void setShininess(double shininess) {
        this.shininess = shininess;
    }

    public double getIndexOfReflexion() {
        return indexOfReflexion;
    }

    public void setIndexOfReflexion(double indexOfReflexion) {
        this.indexOfReflexion = indexOfReflexion;
    }

    public double getIndexOfRefraction() {
        return indexOfRefraction;
    }

    public void setIndexOfRefraction(double indexOfRefraction) {
        this.indexOfRefraction = indexOfRefraction;
    }
}
