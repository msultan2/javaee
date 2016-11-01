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
class ProcessRowWhen implements RowProcessor {
    private final RowProcessor processor;
    private final String predicate;

    public ProcessRowWhen(RowProcessor processor, String predicate) {
        this.processor = processor;
        this.predicate = predicate;
    }

    public void modify(ChartWriter chart) {
        processor.modify(chart);
    }

    public void plot(Map map) {
        if ((boolean) map.get(predicate)) {
            processor.plot(map);
        } else {
            // do nothing
        }
    }
}
