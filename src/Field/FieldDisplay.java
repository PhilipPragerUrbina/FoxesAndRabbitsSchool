package Field;

import java.util.LinkedHashMap;
import java.util.Map;

import Animals.Animal;
import Simulator.Simulator;
import processing.core.*;

public class FieldDisplay {
    // Colors used for empty locations.
    private static final int EMPTY_COLOR = 0xFFFFFFFF;

    // Color used for objects that have no defined color.
    private static final int UNKNOWN_COLOR = 0x66666666;
    private static final int DEFAULT_EDGE_BUFFER = 20;

    private PApplet p;  // the applet we want to display on
    private Field f;    // the field object we'll be displaying
    private int x, y, w, h; // (x, y) of upper left corner of display
    // the width and height of the display
    private float dx, dy;  // calculate the width and height of each box
    // in the field display using the size of the field
    // and the width and height of the display


    public FieldDisplay(PApplet p, Simulator s) {
        this(p, s.getField());
    }

    public FieldDisplay(PApplet p, Field f) {
        this(p, f, DEFAULT_EDGE_BUFFER, DEFAULT_EDGE_BUFFER, p.width - 2*DEFAULT_EDGE_BUFFER, p.height/2 - 2* DEFAULT_EDGE_BUFFER);
    }

    public FieldDisplay(PApplet p, Field f, int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.p = p;
        this.f = f;
        this.dx = (float)w / (int)f.getWidth();
        this.dy = (float)h / (int)f.getHeight();
    }

    public void drawField(Field field) {

        //draw
        for (Animal animal : field.getAnimals()) {
            Integer animal_color = animal.getColor().hashCode();
            Vector2 position = animal.getLocation();
            double radius = animal.getRadius();
            p.fill(animal_color);
            p.ellipse((float)position.x * dx,(float)position.y * dy,(float)radius*dx,(float)radius*dx);
        }
    }
    public Vector2 gridLocationAt(Vector2 l) {
        if (l.x > x && l.y < x + w && l.y > y && l.y < y+h) {
            return new Vector2((int)Math.floor((l.y-y)/dy), (int)Math.floor((l.x-x)/dx));
        } else return null;
    }

    public int getBottomEdge() {
        return y + h;
    }

    public int getTopEdge() {
        return y;
    }

    public int getLeftEdge() {
        return x;
    }

    public int getRightEdge() {
        return x+w;
    }
}