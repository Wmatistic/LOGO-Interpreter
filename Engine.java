import java.util.ArrayList;
import java.util.List;

public abstract class Engine {

    public final static String[] COLORS = {
        "black", "red", "green", "blue", "yellow", "cyan", "magenta", "coral",
        "gray", "orange", "purple", "brown", "pink", "lime", "navy", "teal"
    };

    final protected double w, h;
    protected double x, y;
    protected double headingDeg;
    protected boolean penDown;
    protected int coloridx;
    protected String customColor;
    protected int strokeWidth;

    protected boolean filling;
    protected List<double[]> fillPoints;
    protected String fillColor;

    protected Engine(double _w, double _h) {
        w = _w;
        h = _h;
        home();
    }

    public void home() {
        x = w / 2.0;
        y = h / 2.0;
        headingDeg = 0.0;
        penDown = true;
        coloridx = 0;
        customColor = null;
        strokeWidth = 2;
        filling = false;
        fillPoints = new ArrayList<>();
    }

    public void penDown() { penDown = true; }
    public void penUp()   { penDown = false; }

    public void setColor(int c) { coloridx = c % COLORS.length; customColor = null; }
    public void setColor(int r, int g, int b) {
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        customColor = String.format("rgb(%d,%d,%d)", r, g, b);
    }

    public void setStrokeWidth(int w) { strokeWidth = Math.max(1, w); }

    public void setX(double nx) {
        if (penDown) draw_line(nx, y);
        if (filling) fillPoints.add(new double[]{nx, y});
        x = nx;
    }

    public void setY(double ny) {
        if (penDown) draw_line(x, ny);
        if (filling) fillPoints.add(new double[]{x, ny});
        y = ny;
    }

    public void rotate(double degrees) { headingDeg += degrees; }

    public void move(double dist) {
        double r = Math.toRadians(headingDeg);
        double nx = x + dist * Math.cos(r);
        double ny = y + dist * Math.sin(r);
        if (penDown) draw_line(nx, ny);
        if (filling) fillPoints.add(new double[]{nx, ny});
        x = nx;
        y = ny;
    }

    public void beginFill() {
        filling = true;
        fillPoints = new ArrayList<>();
        fillPoints.add(new double[]{x, y});
        fillColor = currentColor();
    }

    public void endFill() {
        if (filling) {
            draw_polygon(fillPoints, fillColor);
            filling = false;
            fillPoints = new ArrayList<>();
        }
    }

    protected String currentColor() {
        return customColor != null ? customColor : COLORS[coloridx];
    }

    public void open()  {}
    public void close() {}
    public void draw_line(double nx, double ny) {}
    public void draw_polygon(List<double[]> points, String color) {}
}
