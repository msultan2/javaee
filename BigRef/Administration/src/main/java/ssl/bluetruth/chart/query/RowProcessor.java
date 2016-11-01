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
public interface RowProcessor {
    
    public void plot(Map map);

    public void modify(ChartWriter chart);
}
