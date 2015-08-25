package core;

/**
 * Created by kevin on 25/08/2015.
 */
public class ContainerModel {

    public ContainerModel()
    {}

    public int Id;
    public int Poids;
    public int Volume;
    public int VolumeMax;
    public int FillRatio;
    public boolean ToBeCollected;

    public void SetId(int id) { Id = id; }

    public void SetPoids(int poids){ Poids = poids; }

    public void SetVolume(int volume) { Volume = volume; }

    public void SetVolumeMax(int volumeMax) { VolumeMax = volumeMax; }

    public void SetFillRatio(int fillRatio) { FillRatio = fillRatio; }

    public void SetToBeCollected(boolean toBeCollected) { ToBeCollected = toBeCollected; }
}
