/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.query;

import java.util.Collection;
import java.util.Map;
import ssl.bluetruth.chart.writer.ChartWriter;

/**
 *
 * @author pwood
 */
public class AggregateRowProcessor implements RowProcessor {
    Collection<RowProcessor> processors;

    public AggregateRowProcessor(Collection<RowProcessor> processors) {
        this.processors = processors;
    }

    public void modify(ChartWriter chart) {
        for(RowProcessor processor: processors) {
            processor.modify(chart);
        }
    }

    public void plot(Map map) {
        for (RowProcessor processor : processors) {
            processor.plot(map);
        }
    }
}
