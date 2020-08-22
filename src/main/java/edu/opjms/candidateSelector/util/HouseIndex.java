package edu.opjms.candidateSelector.util;

public enum HouseIndex {
    TILAK("#1565C0"),
    KABIR("#3D933D"),
    RAMAN("#aa32c3"),
    TAGORE("#D13438"),
    VASHISHTH("#a08d1c"),
    VIVEKANAND("#E18627");

    public String color;

    HouseIndex(String color) {
        this.color = color;
    }

    public static HouseIndex getFromIndex(int houseIndex) {
        switch (houseIndex) {
            case 0:
                return HouseIndex.TILAK;
            case 1:
                return HouseIndex.KABIR;
            case 2:
                return HouseIndex.RAMAN;
            case 3:
                return HouseIndex.TAGORE;
            case 4:
                return HouseIndex.VASHISHTH;
            case 5:
                return HouseIndex.VIVEKANAND;
        }
        throw new IndexOutOfBoundsException("Index out of range");
    }
}
