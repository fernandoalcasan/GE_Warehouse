package mx.itesm.csf.estimoteprueba;

import com.estimote.mgmtsdk.feature.settings.api.Beacon;
import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;

import java.util.HashMap;
import java.util.UUID;

public class Bicon {

    protected BeaconRegion region;
    protected int hall; //pasillo
    int[] beaconsInAdjacency = null;
    float x, y;
    int minor, major, zone;
    UUID uuid_beacon;
    int vertex_1, vertex_2;
    String region_id;
    HashMap<Integer, Integer> floors = new HashMap<Integer, Integer>();

    Bicon(String uuid, int maj, int min, int hal, float posX, float posY, int depZone, int v1, int v2)
    {
        uuid_beacon = UUID.fromString(uuid);
        major = maj;
        minor = min;
        vertex_1 = v1;
        vertex_2 = v2;
        zone = depZone;
        region_id = "ranged region ";
        region = new BeaconRegion(region_id + zone, uuid_beacon, major, minor);
        hall = hal;
        x = posX;
        y = posY;
    }

    public int getZone(){ return zone; }
    public void setFloor(int fl, int id){ floors.put(fl, id); }
    public int getHall()
    {
        return hall;
    }
    public void setHall(int a) { hall = a; }
    public String getRegionID(){ return region_id; }
    public void setRegion(BeaconRegion zone){ region = zone; }
    public BeaconRegion getRegion(){return region;}
    public int[] getBeaconsInAdjacency() {return beaconsInAdjacency;}
    public void setBiA(int[] adj) {beaconsInAdjacency = adj;}
    public int getMinor() {return minor;}
    public void setMinor(int x) {minor = x;}
    public int getMajor() {return major;}
    public void setMajor(int x) {major = x;}
    public UUID getUUID() {return uuid_beacon;}
    public void setUUID(UUID u) {uuid_beacon = u;}
    public void initializeBiA(int x) {beaconsInAdjacency = new int[x];}
    public void insertBiA(int x, int z) {beaconsInAdjacency[x] = z;}
    public int[] getBiA() {return beaconsInAdjacency;}

    public boolean isAdjacent(int x)
    {
        for(int i = 0; i < beaconsInAdjacency.length; i++)
        {
            if(x == beaconsInAdjacency[i])
                return true;
        }
        return false;
    }
}