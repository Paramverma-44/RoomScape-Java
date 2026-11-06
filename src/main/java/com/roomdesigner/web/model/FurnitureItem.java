package com.roomdesigner.web.model;

import java.util.Locale;

public class FurnitureItem {
    private long id;
    private String type;
    private String label;
    private int x;
    private int y;
    private int width;
    private int height;
    private int rotation;
    private String color;

    public FurnitureItem() {}
    public FurnitureItem(long id, String type, String label, int x, int y, int width, int height, int rotation, String color) {
        this.id=id; this.type=type; this.label=label; this.x=x; this.y=y; this.width=width; this.height=height; this.rotation=rotation; this.color=color;
    }
    public long getId(){return id;} public void setId(long v){id=v;}
    public String getType(){return type;} public void setType(String v){type=v;}
    public String getLabel(){return label;} public void setLabel(String v){label=v;}
    public int getX(){return x;} public void setX(int v){x=v;}
    public int getY(){return y;} public void setY(int v){y=v;}
    public int getWidth(){return width;} public void setWidth(int v){width=v;}
    public int getHeight(){return height;} public void setHeight(int v){height=v;}
    public int getRotation(){return rotation;} public void setRotation(int v){rotation=v;}
    public String getColor(){return color;} public void setColor(String v){color=v;}
    public String getSafeColor() { return color != null && color.matches("#[0-9a-fA-F]{6}") ? color : "#d8c5aa"; }
    public String getSvgTransform() { return "translate(" + (x + width/2 + 20) + " " + (y + height/2 + 20) + ") rotate(" + rotation + ")"; }
    public String getSvgLabel() { return label == null ? type : label; }
    public String getDisplayType() { return type == null ? "Furniture" : type.substring(0,1).toUpperCase(Locale.ROOT)+type.substring(1); }
}
