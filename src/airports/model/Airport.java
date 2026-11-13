package airports.model;

public class Airport {

    private final String name;
    private final String code;
    private final int x;
    private final int y;

    private boolean visible = true;

    public Airport(String name, String code, int x, int y) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Naziv aerodroma ne sme da bude prazan");
        }

        if (code == null || !code.trim().matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Kod aerodroma mora da sadrzi tacno tri velika slova");
        }

        if (x < -180 || x > 180) {
            throw new IllegalArgumentException("X koordinata mora da bude izmedju -180 i 180");
        }

        if (y < -90 || y > 90) {
            throw new IllegalArgumentException("Y koordinata mora da bude izmedju -90 i 90");
        }

        this.name = name.trim();
        this.code = code.trim();
        this.x = x;
        this.y = y;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }

    @Override
    public boolean equals(Object object) {
        if (this==object)
            return true;

        if (!(object instanceof Airport airport))
            return false;

        return code.equals(airport.code);
    }

}
