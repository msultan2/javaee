/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ssl.bluetruth.chart.common;

import java.util.Arrays;
import java.util.Collection;
import ssl.bluetruth.common.BlueTruthException;
import static java.lang.String.format;

/**
 *
 * @author pwood
 */
public class Average {
    private final String average;
    private final static Collection<String> allowableAverages = Arrays.asList(
            "mean",
            "median",
            "mode");
    
    public Average(String average) throws BlueTruthException {
        assertAllowable(average);
        this.average = average;
    }

    public Average(String average, String defaultAverage) throws BlueTruthException {
        if(allowable(average)) {
            this.average = average;
        } else {
            assertAllowable(defaultAverage);
            this.average = defaultAverage;
        }
    }

    public boolean isMean() {
        return average.equals("mean");
    }

    public boolean isMedian() {
        return average.equals("median");
    }

    public boolean isMode() {
        return average.equals("mode");
    }

    @Override
    public String toString() {
        return average;
    }

    private static void assertAllowable(String average) throws BlueTruthException {
        if(!allowable(average)) {
            throw new BlueTruthException(format("Unexpected average %s", average));
        } else {
            // okay!
        }
    }

    private static boolean allowable(String average) {
        return allowableAverages.contains(average);
    }
}
