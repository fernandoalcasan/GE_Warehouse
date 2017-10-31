package mx.itesm.csf.estimoteprueba;

import com.estimote.mgmtsdk.feature.settings.api.Beacon;

public class Bicon {
    protected int pasillo;
    protected int zonaSup;
    protected int zonaInf;
    protected boolean af_wall;
    protected Beacon hardware;

    public int getPasillo()
    {
        return pasillo;
    }

    public void setPasillo(int a)
    {
        pasillo = a;
    }

    public int getZona(int a)
    {
        if(a == 1)
            return zonaSup;
        else
            return zonaInf;
    }

    public void setZona(int a, int z)
    {
        if(a == 1)
            zonaSup = z;
        else
            zonaInf = z;
    }

    public boolean estaPorPared()
    {
        return af_wall;
    }

    public void setPorPared(boolean a)
    {
        af_wall = a;
    }

    public Beacon getBeacon()
    {
        return hardware;
    }

    public void setBeacon(Beacon a)
    {
        hardware = a;
    }

}