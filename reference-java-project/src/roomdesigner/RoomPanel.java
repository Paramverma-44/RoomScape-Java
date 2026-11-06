package roomdesigner;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/** The drawing canvas: draws the room and handles all mouse / keyboard editing. */
public class RoomPanel extends JPanel {
    public static final Rectangle2D ROOM = new Rectangle2D.Double(40, 40, 840, 560); // 8.4m x 5.6m
    private static final int GRID = 10;
    private static final int WALL = 16;

    public static final String[] FLOOR_STYLES = {"Wooden planks", "Tiles", "Plain carpet"};

    private final List<Furniture> items = new ArrayList<>();
    private Furniture selected;
    private Color wallColor = new Color(0xE8DCC8);
    private Color floorColor = new Color(0xC9A27A);
    private int floorStyle = 0;
    private boolean showGrid = true, snap = true;

    private final Deque<List<String>> undoStack = new ArrayDeque<>();
    private Consumer<Furniture> selectionListener = f -> { };
    private Consumer<String> statusListener = s -> { };

    // drag state
    private boolean dragging, resizing;
    private double dragDx, dragDy;

    public RoomPanel() {
        setPreferredSize(new Dimension(920, 640));
        setBackground(new Color(0x37474F));
        setFocusable(true);

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                Point2D p = e.getPoint();
                if (selected != null && selected.onHandle(p)) {
                    pushUndo();
                    resizing = true;
                    return;
                }
                Furniture hit = null;
                List<Furniture> ordered = ordered();
                for (int i = ordered.size() - 1; i >= 0; i--) {
                    if (ordered.get(i).contains(p)) { hit = ordered.get(i); break; }
                }
                select(hit);
                if (hit != null) {
                    pushUndo();
                    dragging = true;
                    dragDx = hit.x - p.getX();
                    dragDy = hit.y - p.getY();
                }
                repaint();
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (selected == null) return;
                Point2D p = e.getPoint();
                if (resizing) {
                    Point2D l = selected.toLocal(p);
                    selected.w = Math.max(20, snapV(2 * Math.abs(l.getX())));
                    selected.h = Math.max(10, snapV(2 * Math.abs(l.getY())));
                } else if (dragging) {
                    selected.x = clamp(snapV(p.getX() + dragDx), ROOM.getMinX(), ROOM.getMaxX());
                    selected.y = clamp(snapV(p.getY() + dragDy), ROOM.getMinY(), ROOM.getMaxY());
                }
                selectionListener.accept(selected);
                repaint();
            }

            @Override public void mouseReleased(MouseEvent e) { dragging = resizing = false; }

            @Override public void mouseMoved(MouseEvent e) {
                if (selected != null && selected.onHandle(e.getPoint())) setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                else {
                    boolean over = false;
                    for (Furniture f : items) if (f.contains(e.getPoint())) { over = true; break; }
                    setCursor(Cursor.getPredefinedCursor(over ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR));
                }
                Point2D p = e.getPoint();
                if (ROOM.contains(p))
                    statusListener.accept(String.format("Cursor: %d cm, %d cm   |   Items: %d",
                            (int) (p.getX() - ROOM.getX()), (int) (p.getY() - ROOM.getY()), items.size()));
            }

            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                if (selected == null) return;
                pushUndo();
                rotateSelected(e.getWheelRotation() * 15);
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);

        bind("DELETE", "del", () -> deleteSelected());
        bind("BACK_SPACE", "del2", () -> deleteSelected());
        bind("R", "rot", () -> { if (selected != null) { pushUndo(); rotateSelected(90); } });
        bind("ctrl Z", "undo", this::undo);
        bind("ctrl D", "dup", this::duplicateSelected);
        bind("UP", "up", () -> nudge(0, -GRID));
        bind("DOWN", "down", () -> nudge(0, GRID));
        bind("LEFT", "left", () -> nudge(-GRID, 0));
        bind("RIGHT", "right", () -> nudge(GRID, 0));
    }

    private void bind(String key, String name, Runnable r) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { r.run(); }
        });
    }

    // ---------- helpers ----------
    private double snapV(double v) { return snap ? Math.round(v / GRID) * GRID : v; }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }

    private List<Furniture> ordered() {
        List<Furniture> l = new ArrayList<>(items);
        l.sort(Comparator.comparingInt(f -> f.type.layer));   // stable sort keeps insertion order within layer
        return l;
    }

    // ---------- listeners ----------
    public void setSelectionListener(Consumer<Furniture> l) { selectionListener = l; }
    public void setStatusListener(Consumer<String> l) { statusListener = l; }
    public Furniture getSelected() { return selected; }

    private void select(Furniture f) {
        selected = f;
        selectionListener.accept(f);
    }

    // ---------- editing operations ----------
    public void addItem(FurnitureType t) {
        pushUndo();
        double off = (items.size() % 6) * 20;
        Furniture f = new Furniture(t, ROOM.getCenterX() + off, ROOM.getCenterY() + off);
        items.add(f);
        select(f);
        repaint();
    }

    public void deleteSelected() {
        if (selected == null) return;
        pushUndo();
        items.remove(selected);
        select(null);
        repaint();
    }

    public void duplicateSelected() {
        if (selected == null) return;
        pushUndo();
        Furniture c = selected.copy();
        c.x += 30; c.y += 30;
        items.add(c);
        select(c);
        repaint();
    }

    public void rotateSelected(double deg) {
        if (selected == null) return;
        selected.angle = ((selected.angle + deg) % 360 + 360) % 360;
        selectionListener.accept(selected);
        repaint();
    }

    private void nudge(int dx, int dy) {
        if (selected == null) return;
        pushUndo();
        selected.x = clamp(selected.x + dx, ROOM.getMinX(), ROOM.getMaxX());
        selected.y = clamp(selected.y + dy, ROOM.getMinY(), ROOM.getMaxY());
        selectionListener.accept(selected);
        repaint();
    }

    public void setSelectedColor(Color c) { if (selected != null) { pushUndo(); selected.color = c; repaint(); } }
    public void setSelectedSize(double w, double h) { if (selected != null) { selected.w = w; selected.h = h; repaint(); } }
    public void setSelectedAngle(double a) { if (selected != null) { selected.angle = a; repaint(); } }

    public void bringToFront() { if (selected != null) { pushUndo(); items.remove(selected); items.add(selected); repaint(); } }
    public void sendToBack() { if (selected != null) { pushUndo(); items.remove(selected); items.add(0, selected); repaint(); } }

    public void clearAll() { pushUndo(); items.clear(); select(null); repaint(); }

    public void setWallColor(Color c) { pushUndo(); wallColor = c; repaint(); }
    public void setFloorColor(Color c) { pushUndo(); floorColor = c; repaint(); }
    public void setFloorStyle(int s) { pushUndo(); floorStyle = s; repaint(); }
    public Color getWallColor() { return wallColor; }
    public Color getFloorColor() { return floorColor; }
    public int getFloorStyle() { return floorStyle; }
    public void setShowGrid(boolean b) { showGrid = b; repaint(); }
    public void setSnap(boolean b) { snap = b; }

    // ---------- undo ----------
    private List<String> snapshot() {
        List<String> l = new ArrayList<>();
        l.add(wallColor.getRGB() + "," + floorColor.getRGB() + "," + floorStyle);
        for (Furniture f : items) l.add(f.serialize());
        return l;
    }

    private void restore(List<String> l) {
        items.clear();
        String[] h = l.get(0).split(",");
        wallColor = new Color(Integer.parseInt(h[0]), true);
        floorColor = new Color(Integer.parseInt(h[1]), true);
        floorStyle = Integer.parseInt(h[2]);
        for (int i = 1; i < l.size(); i++) items.add(Furniture.parse(l.get(i)));
    }

    private void pushUndo() {
        undoStack.push(snapshot());
        if (undoStack.size() > 100) undoStack.removeLast();
    }

    public void undo() {
        if (undoStack.isEmpty()) { statusListener.accept("Nothing to undo"); return; }
        restore(undoStack.pop());
        select(null);
        repaint();
    }

    // ---------- file I/O ----------
    public void save(File f) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("ROOMDESIGNER_V1");
            for (String s : snapshot()) pw.println(s);
        }
    }

    public void load(File f) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String first = br.readLine();
            if (first == null || !first.equals("ROOMDESIGNER_V1")) throw new IOException("Not a Room Designer file");
            String s;
            while ((s = br.readLine()) != null) if (!s.isBlank()) lines.add(s);
        }
        if (lines.isEmpty()) throw new IOException("Empty design file");
        pushUndo();
        try {
            restore(lines);
        } catch (RuntimeException ex) {
            undo();
            throw new IOException("Corrupted design file");
        }
        select(null);
        repaint();
    }

    public void exportPng(File f) throws IOException {
        BufferedImage img = new BufferedImage(920, 640, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        Furniture keep = selected;
        selected = null;
        paintDesign(g, false);
        selected = keep;
        g.dispose();
        ImageIO.write(img, "png", f);
    }

    /** Loads a ready-made bedroom + living area layout (useful for demo). */
    public void loadSample() {
        pushUndo();
        items.clear();
        wallColor = new Color(0xE8DCC8);
        floorColor = new Color(0xC9A27A);
        floorStyle = 0;
        add(FurnitureType.RUG, 300, 380, 240, 160, 0, 0xB71C1C);
        add(FurnitureType.BED, 130, 140, 150, 190, 0, 0x7E57C2);
        add(FurnitureType.WARDROBE, 420, 78, 140, 55, 0, 0x8D6E63);
        add(FurnitureType.DESK, 740, 100, 140, 70, 0, 0x795548);
        add(FurnitureType.CHAIR, 740, 160, 45, 45, 180, 0x455A64);
        add(FurnitureType.SOFA, 300, 320, 200, 80, 0, 0x5C6BC0);
        add(FurnitureType.TV_UNIT, 300, 575, 150, 45, 0, 0x424242);
        add(FurnitureType.ROUND_TABLE, 300, 440, 90, 90, 0, 0xBCAAA4);
        add(FurnitureType.BOOKSHELF, 860, 330, 120, 30, 90, 0x6D4C41);
        add(FurnitureType.LAMP, 80, 270, 35, 35, 0, 0xFFD54F);
        add(FurnitureType.PLANT, 520, 550, 55, 55, 0, 0x43A047);
        add(FurnitureType.WINDOW, 130, 40, 120, 16, 0, 0x81D4FA);
        add(FurnitureType.WINDOW, 740, 40, 120, 16, 0, 0x81D4FA);
        add(FurnitureType.DOOR, 700, 555, 90, 90, 0, 0x8D6E63);
        select(null);
        repaint();
    }

    private void add(FurnitureType t, double x, double y, double w, double h, double a, int rgb) {
        Furniture f = new Furniture(t, x, y);
        f.w = w; f.h = h; f.angle = a; f.color = new Color(rgb);
        items.add(f);
    }

    // ---------- painting ----------
    @Override protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        paintDesign((Graphics2D) g0, true);
    }

    private void paintDesign(Graphics2D g, boolean editing) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (!editing) { g.setColor(getBackground()); g.fillRect(0, 0, 920, 640); }

        drawFloor(g);
        if (editing && showGrid) drawGrid(g);
        drawWalls(g);
        drawDimensions(g);
        for (Furniture f : ordered()) f.draw(g, f == selected);
    }

    private void drawFloor(Graphics2D g) {
        Shape oldClip = g.getClip();
        g.clip(ROOM);
        int rx = (int) ROOM.getX(), ry = (int) ROOM.getY(), rw = (int) ROOM.getWidth(), rh = (int) ROOM.getHeight();
        g.setColor(floorColor);
        g.fill(ROOM);
        if (floorStyle == 0) {                       // wooden planks
            int ph = 28, pl = 140;
            for (int row = 0, y = ry; y < ry + rh; row++, y += ph) {
                int off = (row * 53) % pl;
                for (int x = rx - off; x < rx + rw; x += pl) {
                    float k = 0.94f + ((x * 31 + row * 17) % 13) / 100f;
                    g.setColor(Furniture.shade(floorColor, k));
                    g.fillRect(x, y, pl, ph);
                    g.setColor(Furniture.shade(floorColor, 0.75));
                    g.drawRect(x, y, pl, ph);
                }
            }
        } else if (floorStyle == 1) {                // tiles
            int t = 60;
            for (int y = ry, r = 0; y < ry + rh; y += t, r++)
                for (int x = rx, c = 0; x < rx + rw; x += t, c++) {
                    g.setColor((r + c) % 2 == 0 ? floorColor : Furniture.shade(floorColor, 0.88));
                    g.fillRect(x, y, t, t);
                    g.setColor(Furniture.shade(floorColor, 0.7));
                    g.drawRect(x, y, t, t);
                }
        }
        g.setClip(oldClip);
    }

    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 22));
        g.setStroke(new BasicStroke(1f));
        for (int x = (int) ROOM.getX(); x <= ROOM.getMaxX(); x += 20)
            g.drawLine(x, (int) ROOM.getY(), x, (int) ROOM.getMaxY());
        for (int y = (int) ROOM.getY(); y <= ROOM.getMaxY(); y += 20)
            g.drawLine((int) ROOM.getX(), y, (int) ROOM.getMaxX(), y);
    }

    private void drawWalls(Graphics2D g) {
        Area walls = new Area(new Rectangle2D.Double(ROOM.getX() - WALL, ROOM.getY() - WALL,
                ROOM.getWidth() + 2 * WALL, ROOM.getHeight() + 2 * WALL));
        walls.subtract(new Area(ROOM));
        g.setColor(wallColor);
        g.fill(walls);
        g.setColor(Furniture.shade(wallColor, 0.55));
        g.setStroke(new BasicStroke(2f));
        g.draw(walls);
    }

    private void drawDimensions(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.setColor(new Color(255, 255, 255, 200));
        String top = "Room width: " + (int) ROOM.getWidth() + " cm";
        String left = "Depth: " + (int) ROOM.getHeight() + " cm";
        g.drawString(top, (int) (ROOM.getCenterX() - g.getFontMetrics().stringWidth(top) / 2.0), (int) ROOM.getY() - WALL - 8);
        Graphics2D r = (Graphics2D) g.create();
        r.rotate(-Math.PI / 2, ROOM.getX() - WALL - 10, ROOM.getCenterY());
        r.drawString(left, (int) (ROOM.getX() - WALL - 10 - r.getFontMetrics().stringWidth(left) / 2.0), (int) ROOM.getCenterY());
        r.dispose();
    }
}
