package preparation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

class ElementGeometry {

    /**
     * Приватный метод, возвращающий полигон на структуре XY
     */
    static Polygon getPolygon(double[][][] XY) {
        if (XY == null) return null; // если геометрии нет, то и в полигон не превратить
        GeometryFactory mGF = new GeometryFactory();
        // переструктурируем геометрию колец в Coordinate[][]
        // внешнее кольцо
        Coordinate[] geomOut = new Coordinate[XY[0].length];
        for (int l = 0; l < XY[0].length; l++) {
            geomOut[l] = new Coordinate(XY[0][l][0], XY[0][l][1]);
        }
        // внутренние кольца
        LinearRing[] internalRings = null;
        Coordinate[][] geomInt;
        if (XY.length >= 2) { // если внутренние кольца есть
            geomInt = new Coordinate[XY.length - 1][];
            internalRings = new LinearRing[XY.length - 1];
            // Начиная с первого кольца (не с нулевого)
            for (int k = 1; k < XY.length; k++) {
                geomInt[k - 1] = new Coordinate[XY[k].length];
                for (int l = 0; l < XY[k].length; l++) {
                    geomInt[k - 1][l] = new Coordinate(XY[k][l][0], XY[k][l][1]);
                }
                internalRings[k - 1] = new LinearRing(new CoordinateArraySequence(geomInt[k - 1]), mGF);
            } // for
        } // if

        return new Polygon(new LinearRing(new CoordinateArraySequence(geomOut), mGF), internalRings, mGF);
    }
}
