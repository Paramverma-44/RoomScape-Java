package com.roomdesigner.web;

import com.roomdesigner.web.model.FurnitureItem;
import com.roomdesigner.web.model.RoomDesign;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@SessionAttributes("room")
public class RoomDesignerController {
    @ModelAttribute("room")
    public RoomDesign room() { return new RoomDesign(); }

    @GetMapping("/")
    public String home(@ModelAttribute("room") RoomDesign room, Model model) {
        model.addAttribute("itemTypes", new String[]{"sofa","chair","bed","dining-table","coffee-table","wardrobe","bookshelf","tv","plant","lamp","rug","window","door","cabinet"});
        return "index";
    }

    @PostMapping("/room/name") public String roomName(@ModelAttribute("room") RoomDesign room,@RequestParam("name") String name){room.setRoomName(name);return redirect();}
    @PostMapping("/furniture/add") public String add(@ModelAttribute("room") RoomDesign room,@RequestParam("type") String type){room.add(type);return redirect();}
    @PostMapping("/furniture/select") public String select(@ModelAttribute("room") RoomDesign room,@RequestParam("id") long id){room.select(id);return redirect();}
    @PostMapping("/furniture/move") public String move(@ModelAttribute("room") RoomDesign room,@RequestParam("x") int x,@RequestParam("y") int y){room.moveSelected(x,y);return redirect();}
    @PostMapping("/furniture/drag") @ResponseBody public org.springframework.http.ResponseEntity<Void> drag(@ModelAttribute("room") RoomDesign room,@RequestParam("id") long id,@RequestParam("x") int x,@RequestParam("y") int y){room.select(id);room.moveSelected(x,y);return org.springframework.http.ResponseEntity.noContent().build();}
    @PostMapping("/furniture/resize") public String resize(@ModelAttribute("room") RoomDesign room,@RequestParam("width") int width,@RequestParam("height") int height){room.resizeSelected(width,height);return redirect();}
    @PostMapping("/furniture/rotate") public String rotate(@ModelAttribute("room") RoomDesign room){room.rotateSelected();return redirect();}
    @PostMapping("/furniture/color") public String color(@ModelAttribute("room") RoomDesign room,@RequestParam("color") String color){room.recolorSelected(color);return redirect();}
    @PostMapping("/furniture/delete") public String delete(@ModelAttribute("room") RoomDesign room){room.deleteSelected();return redirect();}
    @PostMapping("/room/colors") public String colors(@ModelAttribute("room") RoomDesign room,@RequestParam("wallColor") String wallColor,@RequestParam("floorColor") String floorColor){room.setWallColor(wallColor);room.setFloorColor(floorColor);return redirect();}
    @PostMapping("/room/reset") public String reset(@ModelAttribute("room") RoomDesign room){room.reset();return redirect();}

    @GetMapping("/room/export")
    public void export(@ModelAttribute("room") RoomDesign room, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=room-design.room");
        StringBuilder out = new StringBuilder("ROOMDESIGN/1\n");
        out.append("name=").append(clean(room.getRoomName())).append('\n');
        out.append("wall=").append(room.getWallColor()).append('\n');
        out.append("floor=").append(room.getFloorColor()).append('\n');
        for(FurnitureItem i:room.getFurniture()){
            out.append("item=").append(i.getId()).append('|').append(i.getType()).append('|').append(clean(i.getLabel())).append('|')
               .append(i.getX()).append('|').append(i.getY()).append('|').append(i.getWidth()).append('|').append(i.getHeight()).append('|')
               .append(i.getRotation()).append('|').append(i.getSafeColor()).append('\n');
        }
        response.getOutputStream().write(out.toString().getBytes(StandardCharsets.UTF_8));
    }
    private String redirect(){return "redirect:/";}
    private String clean(String s){return s==null?"":s.replace("\n"," ").replace("\r"," ").replace("|","-");}
}
