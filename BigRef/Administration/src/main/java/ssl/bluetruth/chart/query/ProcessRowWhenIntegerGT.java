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
class ProcessRowWhenIntegerGT implements RowProcessor {
    private final RowProcessor processor;
    private final String integerField;
    private final int value;

    public ProcessRowWhenIntegerGT(RowProcessor processor, String integerField, int value) {
        this.processor = processor;
        this.integerField = integerField;
        this.value = value;
    }

    public void modify(ChartWriter chart) {
        processor.modify(chart);
    }

    @Override
    public void plot(Map map) {
        if ((int) (double) map.get(integerField) > value) {
            processor.plot(map);
        } else {
            // do nothing
        }
    }
}
