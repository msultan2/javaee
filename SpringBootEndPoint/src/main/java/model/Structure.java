package model;

public class Structure {

    private final long id;
    private final String smile;

    public Structure(long id, String smileString) {
        this.id = id;
        this.smile = smileString;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return smile;
    }

}
