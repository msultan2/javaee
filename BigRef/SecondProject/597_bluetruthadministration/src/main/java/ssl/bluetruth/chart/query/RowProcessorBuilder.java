/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.query;

/**
 *
 * @author pwood
 */
public class RowProcessorBuilder {
    private RowProcessor processor;

    public RowProcessorBuilder(RowProcessor processor) {
        this.processor = processor;
    }

    public RowProcessorBuilder unless(String predicateField) {
        processor = new ProcessRowUnless(processor, predicateField);
        return this;
    }

    public RowProcessorBuilder unless(boolean predicate) {
        return when(!predicate);
    }

    public RowProcessorBuilder when(String predicateField) {
        processor = new ProcessRowWhen(processor, predicateField);
        return this;
    }

    public RowProcessorBuilder when(boolean predicate) {
        if(!predicate) {
            processor = new NullRowProcessor();
        }
        return this;
    }

    public RowProcessorBuilder gt(String integerField, int value) {
        processor = new ProcessRowWhenIntegerGT(processor, integerField, value);
        return this;
    }

    public RowProcessorBuilder gt(String floatField, double value) {
        processor = new ProcessRowWhenFloatGT(processor, floatField, value);
        return this;
    }

    public RowProcessorBuilder and() {
        return this;
    }

    public RowProcessor build() {
        return processor;
    }
}
