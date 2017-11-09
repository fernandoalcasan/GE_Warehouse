package mx.itesm.csf.estimoteprueba;

import com.estimote.mgmtsdk.feature.settings.api.Beacon;
import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;

import java.util.HashMap;
import java.util.UUID;

public class Bicon {

    protected BeaconRegion region;
    protected int hall; //pasillo
    int[] beaconsInAdjacency;
    float x, y;
    int counter = 0;
    int minor, major, zone;
    UUID uuid_beacon;
    int vertex_1, vertex_2;
    HashMap<Integer, Integer> floors = new HashMap<Integer, Integer>();

    Bicon(String uuid, int maj, int min, int hal, float posX, float posY, int depZone, int v1, int v2)
    {
        uuid_beacon = UUID.fromString(uuid);
        major = maj;
        minor = min;
        vertex_1 = v1;
        vertex_2 = v2;
        zone = depZone;
        region = new BeaconRegion("region " + min, uuid_beacon, major, minor);
        hall = hal;
        x = posX;
        y = posY;
    }

    public void setFloor(int fl, int id){ floors.put(fl, id); }
    public int getHall()
    {
        return hall;
    }
    public void setHall(int a) { hall = a; }
    public int getRegionID(){ return region.getMinor(); }
    public void setRegion(BeaconRegion zone){ region = zone; }
    public BeaconRegion getRegion(){return region;}
    public int[] getBeaconsInAdjacency() {return beaconsInAdjacency;}
    public void setNoOfBIA(int x) {beaconsInAdjacency = new int[x];}
    public int getMinor() {return minor;}
    public void setMinor(int x) {minor = x;}
    public int getMajor() {return major;}
    public void setMajor(int x) {major = x;}
    public UUID getUUID() {return uuid_beacon;}
    public void setUUID(UUID u) {uuid_beacon = u;}
    public void setValuesByRegion()
    {
        minor = region.getMinor();
        major = region.getMajor();
        uuid_beacon = region.getProximityUUID();
    }
    public void addAdjacencyBeacon(int x) {beaconsInAdjacency[counter++] = x;}
}