import java.util.List;

public class SVGEngine extends Engine {

    public SVGEngine(double _w, double _h) {
        super(_w, _h);
    }

    @Override
    public void open() {
        System.out.format("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">\n", (int) w, (int) h);
    }

    @Override
    public void close() {
        System.out.format("</svg>\n");
    }

    @Override
    public void draw_line(double nx, double ny) {
        System.out.format("<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" stroke=\"%s\" stroke-width=\"%d\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>\n",
            x, y, nx, ny, currentColor(), strokeWidth);
    }

    @Override
    public void draw_polygon(List<double[]> points, String color) {
        StringBuilder sb = new StringBuilder("<polygon points=\"");
        for (double[] p : points)
            sb.append(String.format("%f,%f ", p[0], p[1]));
        sb.append(String.format("\" fill=\"%s\" stroke=\"%s\" stroke-width=\"%d\"/>\n",
            color, color, strokeWidth));
        System.out.print(sb);
    }
}
