package ibm.gse.eda.domain;

import java.time.Instant;

public class Position {
    public String vesselID;
    public String status;
    public double speed;
    public double bearing;
    public double longitude;
    public double lattitude;
    public Instant timestamp;
}