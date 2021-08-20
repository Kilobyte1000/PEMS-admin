package net.kilobyte1000;

public enum Houses {
    TILAK("#1565C0", "Tilak"),
    KABIR("#3D933D", "Kabir"),
    RAMAN("#aa32c3", "Raman"),
    TAGORE("#D13438", "Tagore"),
    VASHISHTH("#a08d1c", "Vashishth"),
    VIVEKANAND("#E18627", "Vivekanand");

    public String color;
    public String uiText;

    Houses(String color, String uiText) {
        this.color = color;
        this.uiText = uiText;
    }

    public static Houses getFromIndex(int houseIndex) {
        switch (houseIndex) {
            case 0:
                return Houses.TILAK;
            case 1:
                return Houses.KABIR;
            case 2:
                return Houses.RAMAN;
            case 3:
                return Houses.TAGORE;
            case 4:
                return Houses.VASHISHTH;
            case 5:
                return Houses.VIVEKANAND;
        }
        throw new IndexOutOfBoundsException("Index out of range");
    }


}
