package ssl.bluetruth.chart.jfreechart;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class CustomLineAndShapeRenderer extends XYLineAndShapeRenderer {

    @Override
    protected boolean isLinePass(int pass) {
        return pass == 1;
    }

    /**
     * Returns <code>true</code> if the specified pass is the one for drawing
     * items.
     *
     * @param pass  the pass.
     *
     * @return A boolean.
     */
    @Override
    protected boolean isItemPass(int pass) {
        return pass == 0;
    }
}
