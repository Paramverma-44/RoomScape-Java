package roomdesigner;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/** Main window: menu bar, furniture palette (left), canvas (centre), properties (right). */
public class DesignerFrame extends JFrame {
    private final RoomPanel room = new RoomPanel();
    private final JLabel status = new JLabel(" Ready");
    private final JLabel selName = new JLabel("Nothing selected");
    private final JSpinner wSpin = new JSpinner(new SpinnerNumberModel(100, 10, 700, 5));
    private final JSpinner hSpin = new JSpinner(new SpinnerNumberModel(100, 10, 700, 5));
    private final JSlider angle = new JSlider(0, 359, 0);
    private final JButton colorBtn = new JButton("Change colour");
    private final JPanel swatch = new JPanel();
    private boolean updating;

    public DesignerFrame() {
        super("Virtual Interior Room Designer  -  2D Java Graphics");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildPalette(), BorderLayout.WEST);
        add(room, BorderLayout.CENTER);
        add(buildProperties(), BorderLayout.EAST);
        status.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
        add(status, BorderLayout.SOUTH);
        setJMenuBar(buildMenu());

        room.setSelectionListener(this::showSelection);
        room.setStatusListener(s -> status.setText(" " + s));
        showSelection(null);

        pack();
        setLocationRelativeTo(null);
    }

    // ---------------- palette ----------------
    private JComponent buildPalette() {
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 4));
        p.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        for (FurnitureType t : FurnitureType.values()) {
            JButton b = new JButton(t.label, new ImageIcon(preview(t)));
            b.setVerticalTextPosition(SwingConstants.BOTTOM);
            b.setHorizontalTextPosition(SwingConstants.CENTER);
            b.setFont(b.getFont().deriveFont(Font.PLAIN, 11f));
            b.setMargin(new Insets(4, 2, 4, 2));
            b.setToolTipText("Add " + t.label + " to the room");
            b.addActionListener(e -> room.addItem(t));
            p.add(b);
        }
        JPanel wrap = new JPanel(new BorderLayout());
        JLabel title = new JLabel("  Furniture", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setBorder(BorderFactory.createEmptyBorder(8, 4, 0, 0));
        wrap.add(title, BorderLayout.NORTH);
        wrap.add(new JScrollPane(p, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        wrap.setPreferredSize(new Dimension(200, 640));
        return wrap;
    }

    private static Image preview(FurnitureType t) {
        int S = 44;
        BufferedImage img = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Furniture f = new Furniture(t, 0, 0);
        double s = (S - 6) / Math.max(f.w, f.h);
        AffineTransform at = new AffineTransform();
        at.translate(S / 2.0, S / 2.0);
        at.scale(s, s);
        g.transform(at);
        f.draw(g, false);
        g.dispose();
        return img;
    }

    // ---------------- properties ----------------
    private JComponent buildProperties() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setPreferredSize(new Dimension(210, 640));

        JLabel title = new JLabel("Properties");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        p.add(title);
        p.add(Box.createVerticalStrut(8));
        selName.setFont(selName.getFont().deriveFont(Font.ITALIC));
        p.add(selName);
        p.add(Box.createVerticalStrut(10));

        p.add(new JLabel("Width (cm)"));
        p.add(wSpin);
        p.add(Box.createVerticalStrut(6));
        p.add(new JLabel("Height (cm)"));
        p.add(hSpin);
        p.add(Box.createVerticalStrut(6));
        p.add(new JLabel("Rotation (degrees)"));
        p.add(angle);
        p.add(Box.createVerticalStrut(10));

        swatch.setPreferredSize(new Dimension(180, 22));
        swatch.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        swatch.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        p.add(swatch);
        p.add(Box.createVerticalStrut(4));
        p.add(colorBtn);
        p.add(Box.createVerticalStrut(14));

        p.add(btn("Rotate 90\u00B0  (R)", e -> { if (room.getSelected() != null) room.rotateSelected(90); }));
        p.add(btn("Duplicate  (Ctrl+D)", e -> room.duplicateSelected()));
        p.add(btn("Bring to front", e -> room.bringToFront()));
        p.add(btn("Send to back", e -> room.sendToBack()));
        p.add(btn("Delete  (Del)", e -> room.deleteSelected()));
        p.add(Box.createVerticalStrut(14));

        JTextArea help = new JTextArea(
                "HOW TO USE\n" +
                "\u2022 Click a furniture button to add it\n" +
                "\u2022 Drag an item to move it\n" +
                "\u2022 Drag the white corner handle to resize\n" +
                "\u2022 Mouse wheel rotates 15\u00B0\n" +
                "\u2022 Arrow keys nudge the item\n" +
                "\u2022 Ctrl+Z undo");
        help.setEditable(false);
        help.setOpaque(false);
        help.setFont(help.getFont().deriveFont(11f));
        help.setLineWrap(true);
        help.setWrapStyleWord(true);
        p.add(help);

        wSpin.addChangeListener(e -> { if (!updating) room.setSelectedSize(((Number) wSpin.getValue()).doubleValue(), ((Number) hSpin.getValue()).doubleValue()); });
        hSpin.addChangeListener(e -> { if (!updating) room.setSelectedSize(((Number) wSpin.getValue()).doubleValue(), ((Number) hSpin.getValue()).doubleValue()); });
        angle.addChangeListener(e -> { if (!updating) room.setSelectedAngle(angle.getValue()); });
        colorBtn.addActionListener(e -> {
            Furniture f = room.getSelected();
            if (f == null) return;
            Color c = JColorChooser.showDialog(this, "Choose colour", f.color);
            if (c != null) { room.setSelectedColor(c); swatch.setBackground(c); }
        });
        return p;
    }

    private JButton btn(String text, java.awt.event.ActionListener al) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        b.addActionListener(al);
        return b;
    }

    private void showSelection(Furniture f) {
        updating = true;
        boolean on = f != null;
        wSpin.setEnabled(on); hSpin.setEnabled(on); angle.setEnabled(on); colorBtn.setEnabled(on);
        if (on) {
            selName.setText(f.type.label);
            wSpin.setValue((int) Math.round(f.w));
            hSpin.setValue((int) Math.round(f.h));
            angle.setValue((int) Math.round(f.angle) % 360);
            swatch.setBackground(f.color);
            status.setText(String.format(" %s  -  %d x %d cm, rotated %d\u00B0", f.type.label,
                    Math.round(f.w), Math.round(f.h), Math.round(f.angle)));
        } else {
            selName.setText("Nothing selected");
            swatch.setBackground(getBackground());
        }
        updating = false;
    }

    // ---------------- menu ----------------
    private JMenuBar buildMenu() {
        JMenuBar mb = new JMenuBar();

        JMenu file = new JMenu("File");
        file.add(item("New design", KeyEvent.VK_N, e -> {
            if (JOptionPane.showConfirmDialog(this, "Clear the whole room?", "New design", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                room.clearAll();
        }));
        file.add(item("Load sample room", 0, e -> room.loadSample()));
        file.addSeparator();
        file.add(item("Open design...", KeyEvent.VK_O, e -> openDesign()));
        file.add(item("Save design...", KeyEvent.VK_S, e -> saveDesign()));
        file.add(item("Export as PNG image...", KeyEvent.VK_E, e -> exportPng()));
        file.addSeparator();
        file.add(item("Exit", 0, e -> System.exit(0)));

        JMenu edit = new JMenu("Edit");
        edit.add(item("Undo", KeyEvent.VK_Z, e -> room.undo()));
        edit.add(item("Duplicate", KeyEvent.VK_D, e -> room.duplicateSelected()));
        edit.add(item("Delete selected", 0, e -> room.deleteSelected()));

        JMenu roomMenu = new JMenu("Room");
        roomMenu.add(item("Wall colour...", 0, e -> {
            Color c = JColorChooser.showDialog(this, "Wall colour", room.getWallColor());
            if (c != null) room.setWallColor(c);
        }));
        roomMenu.add(item("Floor colour...", 0, e -> {
            Color c = JColorChooser.showDialog(this, "Floor colour", room.getFloorColor());
            if (c != null) room.setFloorColor(c);
        }));
        JMenu style = new JMenu("Floor style");
        ButtonGroup bg = new ButtonGroup();
        for (int i = 0; i < RoomPanel.FLOOR_STYLES.length; i++) {
            final int idx = i;
            JRadioButtonMenuItem r = new JRadioButtonMenuItem(RoomPanel.FLOOR_STYLES[i], i == room.getFloorStyle());
            r.addActionListener(e -> room.setFloorStyle(idx));
            bg.add(r);
            style.add(r);
        }
        roomMenu.add(style);
        roomMenu.addSeparator();
        JCheckBoxMenuItem grid = new JCheckBoxMenuItem("Show grid", true);
        grid.addActionListener(e -> room.setShowGrid(grid.isSelected()));
        JCheckBoxMenuItem snap = new JCheckBoxMenuItem("Snap to grid", true);
        snap.addActionListener(e -> room.setSnap(snap.isSelected()));
        roomMenu.add(grid);
        roomMenu.add(snap);

        JMenu help = new JMenu("Help");
        help.add(item("About", 0, e -> JOptionPane.showMessageDialog(this,
                "Virtual Interior Room Designer\nBuilt with Java Swing and Java 2D (Graphics2D).\n\n" +
                "Scale: 1 pixel = 1 cm.", "About", JOptionPane.INFORMATION_MESSAGE)));

        mb.add(file); mb.add(edit); mb.add(roomMenu); mb.add(help);
        return mb;
    }

    private JMenuItem item(String text, int key, java.awt.event.ActionListener al) {
        JMenuItem m = new JMenuItem(text);
        if (key != 0) m.setAccelerator(KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        m.addActionListener(al);
        return m;
    }

    // ---------------- file actions ----------------
    private void openDesign() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Room design (*.room)", "room"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                room.load(fc.getSelectedFile());
                status.setText(" Loaded " + fc.getSelectedFile().getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not open file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveDesign() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Room design (*.room)", "room"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = withExt(fc.getSelectedFile(), ".room");
            try {
                room.save(f);
                status.setText(" Saved " + f.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not save file:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportPng() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("PNG image", "png"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = withExt(fc.getSelectedFile(), ".png");
            try {
                room.exportPng(f);
                status.setText(" Exported " + f.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Could not export image:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static File withExt(File f, String ext) {
        return f.getName().toLowerCase().endsWith(ext) ? f : new File(f.getPath() + ext);
    }
}
