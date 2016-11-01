package ssl.bluetruth.database.entities;

import java.io.Serializable;

public class Detector implements Serializable {
    
    private String detectorId;
    private String detectorName;
    private String location;
    private Integer mode;
    private String carriageway;

    public Detector() {
    }

    public Detector(String detectorid) {
        this.detectorId = detectorid;
    }

    public String getDetectorId() {
        return detectorId;
    }

    public void setDetectorId(String detectorId) {
        this.detectorId = detectorId;
    }

    public String getDetectorName() {
        return detectorName;
    }

    public void setDetectorName(String detectorName) {
        this.detectorName = detectorName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public String getCarriageway() {
        return carriageway;
    }

    public void setCarriageway(String carriageway) {
        this.carriageway = carriageway;
    }

    @Override
    public String toString() {
        return "com.ssl.test.Detector[detectorId=" + detectorId + "]";
    }

}
