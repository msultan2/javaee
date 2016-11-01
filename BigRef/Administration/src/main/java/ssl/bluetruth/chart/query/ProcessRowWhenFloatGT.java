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
class ProcessRowWhenFloatGT implements RowProcessor {
    private final RowProcessor processor;
    private final String integerField;
    private final double value;

    public ProcessRowWhenFloatGT(RowProcessor processor, String floatField, double value) {
        this.processor = processor;
        this.integerField = floatField;
        this.value = value;
    }

    public void modify(ChartWriter chart) {
        processor.modify(chart);
    }

    public void plot(Map map) {
        if ((double) map.get(integerField) > value) {
            processor.plot(map);
        } else {
            // do nothing
        }
    }
}
