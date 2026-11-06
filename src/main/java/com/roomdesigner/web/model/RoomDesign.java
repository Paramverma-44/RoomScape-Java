package com.roomdesigner.web.model;

import java.util.ArrayList;
import java.util.List;

public class RoomDesign {
    private final List<FurnitureItem> furniture = new ArrayList<>();
    private long nextId = 1;
    private long selectedId = -1;
    private String wallColor = "#f3eee7";
    private String floorColor = "#d9c4a6";
    private String roomName = "My Living Room";

    public RoomDesign() { reset(); }
    public void reset() {
        furniture.clear(); nextId=1; selectedId=-1; roomName="My Living Room"; wallColor="#f3eee7"; floorColor="#d9c4a6";
        add("sofa"); furniture.get(0).setX(95); furniture.get(0).setY(85); furniture.get(0).setWidth(150); furniture.get(0).setHeight(62);
        add("coffee-table"); furniture.get(1).setX(125); furniture.get(1).setY(190); furniture.get(1).setWidth(105); furniture.get(1).setHeight(48);
        add("plant"); furniture.get(2).setX(365); furniture.get(2).setY(65);
        add("lamp"); furniture.get(3).setX(320); furniture.get(3).setY(205);
        selectedId=furniture.get(0).getId();
    }
    public FurnitureItem add(String type) {
        String safeType = type == null ? "chair" : type.toLowerCase();
        int w=70,h=55; String color="#d8c5aa"; String label=safeType.replace('-', ' ');
        switch (safeType) {
            case "sofa" -> {w=150;h=62;color="#9aabb9";label="Sofa";}
            case "chair" -> {w=56;h=56;color="#c9a47e";label="Chair";}
            case "bed" -> {w=130;h=100;color="#b9c7d5";label="Bed";}
            case "dining-table" -> {w=110;h=72;color="#b68b62";label="Dining table";}
            case "coffee-table" -> {w=105;h=48;color="#c69a6d";label="Coffee table";}
            case "wardrobe" -> {w=78;h=95;color="#b18a67";label="Wardrobe";}
            case "bookshelf" -> {w=68;h=90;color="#a87950";label="Bookshelf";}
            case "tv" -> {w=100;h=18;color="#374151";label="TV unit";}
            case "plant" -> {w=44;h=44;color="#79a66a";label="Plant";}
            case "lamp" -> {w=34;h=54;color="#e9c46a";label="Floor lamp";}
            case "rug" -> {w=150;h=95;color="#d9a7a0";label="Rug";}
            case "window" -> {w=100;h=18;color="#8ecae6";label="Window";}
            case "door" -> {w=60;h=16;color="#8b5e3c";label="Door";}
            case "cabinet" -> {w=90;h=48;color="#b68b62";label="Cabinet";}
            default -> {safeType="chair"; label="Chair";}
        }
        int x=55+(int)((nextId*47)%320), y=55+(int)((nextId*31)%230);
        FurnitureItem item = new FurnitureItem(nextId++, safeType, label, x,y,w,h,0,color);
        furniture.add(item); selectedId=item.getId(); return item;
    }
    public List<FurnitureItem> getFurniture(){return furniture;}
    public FurnitureItem getSelected(){return furniture.stream().filter(i->i.getId()==selectedId).findFirst().orElse(null);}
    public long getSelectedId(){return selectedId;}
    public void select(long id){if(furniture.stream().anyMatch(i->i.getId()==id)) selectedId=id;}
    public void deleteSelected(){furniture.removeIf(i->i.getId()==selectedId);selectedId=furniture.isEmpty()?-1:furniture.get(furniture.size()-1).getId();}
    public void moveSelected(int x,int y){FurnitureItem i=getSelected();if(i!=null){i.setX(clamp(x,0,480-i.getWidth()));i.setY(clamp(y,0,320-i.getHeight()));}}
    public void resizeSelected(int w,int h){FurnitureItem i=getSelected();if(i!=null){i.setWidth(clamp(w,24,190));i.setHeight(clamp(h,16,150));moveSelected(i.getX(),i.getY());}}
    public void rotateSelected(){FurnitureItem i=getSelected();if(i!=null)i.setRotation((i.getRotation()+45)%360);}
    public void recolorSelected(String color){FurnitureItem i=getSelected();if(i!=null&&validColor(color))i.setColor(color);}
    public void setWallColor(String v){if(validColor(v))wallColor=v;}
    public void setFloorColor(String v){if(validColor(v))floorColor=v;}
    public String getWallColor(){return wallColor;} public String getFloorColor(){return floorColor;}
    public String getRoomName(){return roomName;} public void setRoomName(String v){if(v!=null&&!v.isBlank())roomName=v.substring(0,Math.min(60,v.length()));}
    public int getFurnitureCount(){return furniture.size();}
    private static boolean validColor(String v){return v!=null&&v.matches("#[0-9a-fA-F]{6}");}
    private static int clamp(int v,int min,int max){return Math.max(min,Math.min(max,v));}
}
