package mx.itesm.csf.estimoteprueba;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;

import java.util.HashMap;
import java.util.UUID;

public class Bicon {

    protected BeaconRegion region;
    int[] beaconsInAdjacency = null;
    int minor, major, zone;
    UUID uuid_beacon;
    String region_id;
    HashMap<Integer, Integer> floors = new HashMap<Integer, Integer>();

    Bicon(String uuid, int maj, int min, int depZone)
    {
        uuid_beacon = UUID.fromString(uuid);
        major = maj;
        minor = min;
        zone = depZone;
        region_id = "ranged region ";
        region = new BeaconRegion(region_id + zone, uuid_beacon, major, minor);
        //Message.message(cont, "Se creo una region con minor de " + minor);
    }

    public int getZone(){ return zone; }
    public void setFloor(int fl, int id){ floors.put(fl, id); }
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
    public int getBiASize()
    {
        if(beaconsInAdjacency == null)
            return 0;
        else
            return beaconsInAdjacency.length;
    }

    public int getFloorsSize()
    {
        return floors.size();
    }

    public boolean isAdjacent(int x)
    {
        if(beaconsInAdjacency != null)
        {
            for(int i = 0; i < beaconsInAdjacency.length; i++)
            {
                if(x == beaconsInAdjacency[i])
                    return true;
            }
        }
        return false;
    }

}