package roomdesigner;

import java.awt.Color;

/** All furniture / fixture kinds available in the palette. */
public enum FurnitureType {
    BED("Bed", 140, 190, 0x8D6E63, 1),
    SOFA("Sofa", 190, 80, 0x5C6BC0, 1),
    ARMCHAIR("Armchair", 80, 80, 0x26A69A, 1),
    DINING_TABLE("Dining Table", 160, 90, 0xA1887F, 1),
    ROUND_TABLE("Round Table", 100, 100, 0xBCAAA4, 1),
    CHAIR("Chair", 45, 45, 0x6D4C41, 1),
    DESK("Study Desk", 140, 70, 0x795548, 1),
    WARDROBE("Wardrobe", 120, 55, 0x8D6E63, 1),
    BOOKSHELF("Bookshelf", 100, 35, 0x6D4C41, 1),
    TV_UNIT("TV Unit", 150, 45, 0x424242, 1),
    RUG("Rug", 200, 140, 0xC62828, 0),
    LAMP("Floor Lamp", 35, 35, 0xFFD54F, 2),
    PLANT("Plant", 50, 50, 0x43A047, 2),
    DOOR("Door", 90, 90, 0x8D6E63, 1),
    WINDOW("Window", 120, 16, 0x81D4FA, 1);

    public final String label;
    public final int defW, defH, layer;
    public final Color defColor;

    FurnitureType(String label, int w, int h, int rgb, int layer) {
        this.label = label;
        this.defW = w;
        this.defH = h;
        this.defColor = new Color(rgb);
        this.layer = layer;   // 0 = floor level (rug), 1 = furniture, 2 = on top
    }
}
