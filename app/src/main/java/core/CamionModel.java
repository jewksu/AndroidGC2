package core;

/**
 * Created by kevin on 25/08/2015.
 */
public class CamionModel {

    public CamionModel()
    {}

    public int Id;
    public int PoidsMax;
    public int VolumeMax;
    public int TypeDechetID;
    public boolean Disponible;

    public void SetId(int id) { Id = id; }

    public void SetPoidsMAx(int poidsMax){ PoidsMax = poidsMax; }

    public void SetVolumeMax(int volumeMax) { VolumeMax = volumeMax; }

    public void SetTypeDechetID(int idTypeDechet) { TypeDechetID = idTypeDechet; }

    public void SetDispo(boolean dispo) { Disponible = dispo; }
}
