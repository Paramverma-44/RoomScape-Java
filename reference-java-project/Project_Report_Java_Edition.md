# Project Report - Virtual Interior Room Designer using 2D Java Graphics

## 1. Introduction
Planning furniture placement before buying or moving it saves time and money. This project is a
2D virtual interior designer in which a user can build a room layout on screen using drag-and-drop
furniture drawn entirely with Java 2D graphics.

## 2. Objectives
- Provide a top-view (floor plan) room canvas with realistic-looking furniture drawn in code
- Let the user move, resize, rotate and recolour furniture interactively
- Allow customising wall colour, floor colour and floor style
- Allow saving, loading and exporting designs

## 3. Technologies
Java (JDK 8+), Swing (GUI), Java 2D API (Graphics2D) for all rendering. No external libraries.

## 4. System design
- **Model:** `Furniture` (type, centre x/y, width, height, angle, colour), `FurnitureType` (defaults)
- **View:** `RoomPanel.paintComponent` draws floor -> grid -> walls -> furniture (sorted by layer)
- **Controller:** mouse listeners in `RoomPanel`; buttons/menus/spinners in `DesignerFrame`

### Key algorithms
1. **Rendering with transforms:** each item is drawn in local coordinates around its centre; an
   `AffineTransform` (translate, then rotate) places it in the room.
2. **Hit-testing:** the mouse point is converted into the item's local space using the inverse
   transform, then checked against its width/height - this works for rotated items too.
3. **Resizing:** the same local-space conversion gives the new half-width/half-height.
4. **Snap to grid:** `Math.round(v / 10) * 10`.
5. **Undo:** before every change the whole design is stored as a list of text lines on a stack.
6. **Save/Load:** plain text file, one line per item (`TYPE,x,y,w,h,angle,rgb`).

## 5. Features
See README.md.

## 6. Testing
| Test | Expected | Result |
|------|----------|--------|
| Add each of the 15 items | Item appears at room centre | Pass |
| Drag item beyond wall | Item stays inside room | Pass |
| Resize / rotate selected item | Shape updates, panel values update | Pass |
| Save then Open | Identical design restored | Pass |
| Undo after add/move/delete | Previous state restored | Pass |
| Export PNG | Image of room without selection marks | Pass |

## 7. Limitations and future scope
- Top-view 2D only; a 3D walk-through view could be added
- Fixed room size; a custom room size/shape dialog could be added
- More furniture items, multi-select, and print support

## 8. Conclusion
The project meets its objectives and demonstrates core Java 2D graphics concepts: shapes, colour,
strokes, transforms, clipping, images and event-driven GUI programming.
