package dev.nazhida.kivra.essentials;

public final class LocationData {
    public String dimension;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public LocationData() {}

    public LocationData(String dimension, double x, double y, double z, float yaw, float pitch) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
