/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.query;

import java.util.Map;
import ssl.bluetruth.chart.writer.ChartWriter;

/**
 *
 * @author pwood
 */
class NullRowProcessor implements RowProcessor {

    public void modify(ChartWriter chart) {
        // does nothing
    }

    @Override
    public void plot(Map map) {
        // does nothing
    }

}
