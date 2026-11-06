# Virtual Interior Room Designer using 2D Java Graphics

A desktop application built with **Java Swing** and **Java 2D (Graphics2D)** that lets a user
design the layout of a room by placing, moving, resizing, rotating and colouring furniture.

## How to run
**Option 1 (easiest):** double-click `RoomDesigner.jar`, or run `java -jar RoomDesigner.jar`
**Option 2 (from source):** Windows: double-click `run.bat`  |  Linux/Mac: `./run.sh`
Requires JDK/JRE 8 or newer.

## Features
- 15 furniture items drawn with Java 2D shapes (bed, sofa, armchair, tables, chair, desk, wardrobe,
  bookshelf, TV unit, rug, lamp, plant, door, window)
- Drag and drop to move; drag the corner handle to resize; mouse wheel / R key to rotate
- Change colour, width, height and rotation of any item from the Properties panel
- Room options: wall colour, floor colour, 3 floor styles (wooden planks, tiles, carpet), grid and snap-to-grid
- Undo (Ctrl+Z), Duplicate (Ctrl+D), Delete, Bring to front / Send to back, arrow-key nudging
- Save / Open designs (`.room` files), Export design as PNG, built-in sample room
- Scale: 1 pixel = 1 cm (room is 840 x 560 cm)

## Project structure
| File | Purpose |
|------|---------|
| `Main.java` | Program entry point |
| `DesignerFrame.java` | Window, menus, furniture palette, properties panel |
| `RoomPanel.java` | Drawing canvas, mouse/keyboard handling, undo, save/load, PNG export |
| `Furniture.java` | Furniture model + Java 2D drawing of every item (Graphics2D, AffineTransform) |
| `FurnitureType.java` | Enum of furniture kinds with default size, colour, layer |

## Java 2D concepts used
Graphics2D, Shape classes (Rectangle2D, RoundRectangle2D, Ellipse2D, Arc2D, Line2D, Area),
AffineTransform (translate + rotate), BasicStroke, antialiasing (RenderingHints), clipping,
BufferedImage (palette icons and PNG export), hit-testing with inverse transforms.
Also: Swing event handling (MouseAdapter, key bindings), MVC-style separation, file I/O.
