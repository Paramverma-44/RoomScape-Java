package roomdesigner;

import java.awt.*;
import java.awt.geom.*;

/**
 * One item placed in the room. (x, y) is the CENTRE of the item in room
 * coordinates (1 pixel = 1 cm). All drawing is done in local coordinates
 * around the centre, then moved/rotated using an AffineTransform.
 */
public class Furniture {
    public FurnitureType type;
    public double x, y, w, h, angle;   // angle in degrees
    public Color color;

    public Furniture(FurnitureType t, double x, double y) {
        this.type = t;
        this.x = x;
        this.y = y;
        this.w = t.defW;
        this.h = t.defH;
        this.color = t.defColor;
    }

    public Furniture copy() {
        Furniture f = new Furniture(type, x, y);
        f.w = w; f.h = h; f.angle = angle; f.color = color;
        return f;
    }

    private AffineTransform xf() {
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        at.rotate(Math.toRadians(angle));
        return at;
    }

    /** Convert a room point to this item's local coordinates. */
    public Point2D toLocal(Point2D p) {
        try {
            return xf().inverseTransform(p, null);
        } catch (NoninvertibleTransformException e) {
            return p;
        }
    }

    public boolean contains(Point2D p) {
        Point2D l = toLocal(p);
        return Math.abs(l.getX()) <= w / 2 && Math.abs(l.getY()) <= h / 2;
    }

    /** True if p is on the resize handle (bottom-right corner). */
    public boolean onHandle(Point2D p) {
        Point2D l = toLocal(p);
        return Math.hypot(l.getX() - w / 2, l.getY() - h / 2) <= 10;
    }

    // ---------- save / load ----------
    public String serialize() {
        return type.name() + "," + x + "," + y + "," + w + "," + h + "," + angle + "," + color.getRGB();
    }

    public static Furniture parse(String line) {
        String[] p = line.split(",");
        Furniture f = new Furniture(FurnitureType.valueOf(p[0].trim()),
                Double.parseDouble(p[1]), Double.parseDouble(p[2]));
        f.w = Double.parseDouble(p[3]);
        f.h = Double.parseDouble(p[4]);
        f.angle = Double.parseDouble(p[5]);
        f.color = new Color(Integer.parseInt(p[6].trim()), true);
        return f;
    }

    // ---------- drawing ----------
    static Color shade(Color c, double f) {
        int r = (int) Math.max(0, Math.min(255, c.getRed() * f));
        int g = (int) Math.max(0, Math.min(255, c.getGreen() * f));
        int b = (int) Math.max(0, Math.min(255, c.getBlue() * f));
        return new Color(r, g, b);
    }

    private static void fill(Graphics2D g, Shape s, Color fill, Color line) {
        g.setColor(fill);
        g.fill(s);
        g.setColor(line);
        g.draw(s);
    }

    public void draw(Graphics2D g0, boolean selected) {
        Graphics2D g = (Graphics2D) g0.create();
        g.transform(xf());
        g.setStroke(new BasicStroke(1.5f));
        double hw = w / 2, hh = h / 2;
        Color c = color, dark = shade(c, 0.65), light = shade(c, 1.25);

        switch (type) {
            case BED: {
                fill(g, new RoundRectangle2D.Double(-hw, -hh, w, h, 10, 10), c, dark);
                double pw = w * 0.4, ph = h * 0.15;
                fill(g, new RoundRectangle2D.Double(-hw + w * 0.07, -hh + h * 0.04, pw, ph, 8, 8), Color.WHITE, Color.GRAY);
                fill(g, new RoundRectangle2D.Double(hw - w * 0.07 - pw, -hh + h * 0.04, pw, ph, 8, 8), Color.WHITE, Color.GRAY);
                fill(g, new RoundRectangle2D.Double(-hw, -hh + h * 0.28, w, h * 0.72, 10, 10), light, dark);
                g.setColor(dark);
                g.draw(new Line2D.Double(-hw, -hh + h * 0.34, hw, -hh + h * 0.34));
                break;
            }
            case SOFA: {
                fill(g, new RoundRectangle2D.Double(-hw, -hh, w, h, 14, 14), dark, dark.darker());
                fill(g, new RoundRectangle2D.Double(-hw + w * 0.07, -hh + h * 0.28, w * 0.86, h * 0.68, 12, 12), c, dark);
                g.setColor(dark);
                g.draw(new Line2D.Double(0, -hh + h * 0.28, 0, hh - h * 0.04));
                break;
            }
            case ARMCHAIR: {
                fill(g, new RoundRectangle2D.Double(-hw, -hh, w, h, 16, 16), dark, dark.darker());
                fill(g, new RoundRectangle2D.Double(-hw + w * 0.18, -hh + h * 0.22, w * 0.64, h * 0.74, 12, 12), c, dark);
                break;
            }
            case DINING_TABLE: {
                fill(g, new RoundRectangle2D.Double(-hw, -hh, w, h, 12, 12), c, dark);
                fill(g, new RoundRectangle2D.Double(-hw + 8, -hh + 8, w - 16, h - 16, 8, 8), light, c);
                break;
            }
            case ROUND_TABLE: {
                fill(g, new Ellipse2D.Double(-hw, -hh, w, h), c, dark);
                fill(g, new Ellipse2D.Double(-hw + w * 0.12, -hh + h * 0.12, w * 0.76, h * 0.76), light, c);
                break;
            }
            case CHAIR: {
                fill(g, new RoundRectangle2D.Double(-hw, -hh + h * 0.2, w, h * 0.8, 8, 8), c, dark);
                fill(g, new RoundRectangle2D.Double(-hw, -hh, w, h * 0.22, 6, 6), dark, dark.darker());
                break;
            }
            case DESK: {
                fill(g, new Rectangle2D.Double(-hw, -hh, w, h), c, dark);
                fill(g, new Rectangle2D.Double(-w * 0.18, -hh + h * 0.12, w * 0.36, h * 0.12), new Color(40, 40, 40), Color.BLACK);
                fill(g, new Rectangle2D.Double(-w * 0.14, -hh + h * 0.45, w * 0.28, h * 0.2), new Color(220, 220, 220), Color.GRAY);
                fill(g, new Rectangle2D.Double(hw - w * 0.28, -hh + h * 0.1, w * 0.24, h * 0.8), light, dark);
                break;
            }
            case WARDROBE: {
                fill(g, new Rectangle2D.Double(-hw, -hh, w, h), c, dark);
                g.setColor(dark);
                g.draw(new Line2D.Double(0, -hh, 0, hh));
                g.draw(new Line2D.Double(-hw * 0.5, -hh, -hw * 0.5, hh));
                g.draw(new Line2D.Double(hw * 0.5, -hh, hw * 0.5, hh));
                g.setColor(Color.YELLOW.darker());
                g.fill(new Ellipse2D.Double(-6, 0, 4, 4));
                g.fill(new Ellipse2D.Double(2, 0, 4, 4));
                break;
            }
            case BOOKSHELF: {
                fill(g, new Rectangle2D.Double(-hw, -hh, w, h), c, dark);
                int n = Math.max(3, (int) (w / 14));
                Color[] bk = {new Color(0xE53935), new Color(0x1E88E5), new Color(0xFDD835), new Color(0x43A047)};
                for (int i = 0; i < n; i++) {
                    g.setColor(bk[i % bk.length]);
                    double bx = -hw + 4 + i * (w - 8) / n;
                    g.fill(new Rectangle2D.Double(bx, -hh + 4, (w - 8) / n - 2, h - 8));
                }
                break;
            }
            case TV_UNIT: {
                fill(g, new Rectangle2D.Double(-hw, -hh, w, h), c, Color.BLACK);
                fill(g, new Rectangle2D.Double(-w * 0.32, -hh + h * 0.12, w * 0.64, h * 0.3), new Color(20, 20, 25), Color.BLACK);
                g.setColor(new Color(90, 140, 220));
                g.fill(new Rectangle2D.Double(-w * 0.3, -hh + h * 0.16, w * 0.6, h * 0.22));
                break;
            }
            case RUG: {
                fill(g, new RoundRectangle2D.Double(-hw, -hh, w, h, 18, 18), c, dark);
                g.setColor(light);
                g.draw(new RoundRectangle2D.Double(-hw + 10, -hh + 10, w - 20, h - 20, 12, 12));
                g.setColor(shade(c, 1.5));
                g.draw(new RoundRectangle2D.Double(-hw + 20, -hh + 20, w - 40, h - 40, 8, 8));
                break;
            }
            case LAMP: {
                fill(g, new Ellipse2D.Double(-hw, -hh, w, h), c, dark);
                fill(g, new Ellipse2D.Double(-hw * 0.5, -hh * 0.5, hw, hh), Color.WHITE, c.darker());
                break;
            }
            case PLANT: {
                g.setColor(c);
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    g.fill(new Ellipse2D.Double(Math.cos(a) * hw * 0.45 - w * 0.22, Math.sin(a) * hh * 0.45 - h * 0.22, w * 0.44, h * 0.44));
                }
                fill(g, new Ellipse2D.Double(-w * 0.2, -h * 0.2, w * 0.4, h * 0.4), new Color(0x8D6E63), new Color(0x5D4037));
                break;
            }
            case DOOR: {
                g.setColor(c);
                g.setStroke(new BasicStroke(5f));
                g.draw(new Line2D.Double(-hw, hh, -hw, -hh));            // door leaf
                g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
                g.setColor(dark);
                g.draw(new Arc2D.Double(-hw - w, hh - w, 2 * w, 2 * w, 0, 90, Arc2D.OPEN)); // swing arc
                g.setStroke(new BasicStroke(4f));
                g.setColor(Color.DARK_GRAY);
                g.draw(new Line2D.Double(-hw, hh, hw, hh));              // door opening
                break;
            }
            case WINDOW: {
                fill(g, new Rectangle2D.Double(-hw, -hh, w, h), c, Color.DARK_GRAY);
                g.setColor(Color.WHITE);
                g.draw(new Line2D.Double(0, -hh, 0, hh));
                g.draw(new Line2D.Double(-hw, 0, hw, 0));
                break;
            }
        }

        if (selected) {
            g.setColor(new Color(0, 120, 255));
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{6f, 4f}, 0f));
            g.draw(new Rectangle2D.Double(-hw - 3, -hh - 3, w + 6, h + 6));
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(Color.WHITE);
            g.fill(new Rectangle2D.Double(hw - 5, hh - 5, 11, 11));
            g.setColor(new Color(0, 120, 255));
            g.draw(new Rectangle2D.Double(hw - 5, hh - 5, 11, 11));
        }
        g.dispose();
    }
}
