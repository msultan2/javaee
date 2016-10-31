/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.chart.writer;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import ssl.bluetruth.chart.common.FloatPoint;
import ssl.bluetruth.chart.common.Rectangle;
import ssl.bluetruth.chart.style.series.LinesStyle;
import ssl.bluetruth.common.BlueTruthException;

/**
 *
 * @author pwood
 */
@Ignore("Fix me")
public class CsvChartWriterTest {

    private final CsvChartWriter csvChartWriter;
    private final ByteArrayOutputStream outputStream;

    public CsvChartWriterTest() {
        csvChartWriter = new CsvChartWriter();
        outputStream = new ByteArrayOutputStream();
    }

    @Test
    public void mimeTypeIsApplicationCsv() {
        assertEquals("application/csv", csvChartWriter.getMimeType());
    }

    @Test
    public void noDataProducesEmptyCsv() throws Exception, BlueTruthException {
        csvChartWriter.write(outputStream);

        assertEquals("", outputStream.toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addFloatSeriesThrowsException() {
        csvChartWriter.addFloatSeries(
                "", Collections.EMPTY_LIST, new LinesStyle());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addIntegerSeriesThrowsException() {
        csvChartWriter.addIntegerSeries(
                "", Collections.EMPTY_LIST, new LinesStyle());
    }

    @Test
    public void emptySingleStackedArea() throws BlueTruthException {
        Map<String, Collection<FloatPoint>> areaPoints
                = new HashMap<String, Collection<FloatPoint>>();
        areaPoints.put("span-A", Collections.EMPTY_LIST);

        csvChartWriter.addStackedArea(areaPoints);

        csvChartWriter.write(outputStream);

        assertEquals("timestamp,span-A,total\n", outputStream.toString());
    }

    @Test
    public void emptyMultipleStackedAreas() throws BlueTruthException {
        Map<String, Collection<FloatPoint>> areaPoints
                = new HashMap<String, Collection<FloatPoint>>();
        areaPoints.put("span-A", Collections.EMPTY_LIST);
        areaPoints.put("span-B", Collections.EMPTY_LIST);
        areaPoints.put("span-C", Collections.EMPTY_LIST);

        csvChartWriter.addStackedArea(areaPoints);

        csvChartWriter.write(outputStream);

        assertEquals("timestamp,span-B,span-A,span-C,total\n", outputStream.toString());
    }

    @Test
    public void singleStackedArea() throws BlueTruthException {
        Map<String, Collection<FloatPoint>> areaPoints
                = new HashMap<String, Collection<FloatPoint>>();
        areaPoints.put("span-A", Arrays.asList(
                new FloatPoint(1, 230),
                new FloatPoint(2, 200),
                new FloatPoint(3, 440),
                new FloatPoint(4, 170),
                new FloatPoint(5, 260)
        ));

        csvChartWriter.addStackedArea(areaPoints);

        csvChartWriter.write(outputStream);

        assertEquals(
                "timestamp,span-A,total\n"
                + "1,230,230\n"
                + "2,200,200\n"
                + "3,440,440\n"
                + "4,170,170\n"
                + "5,260,260\n",
                outputStream.toString());
    }

    @Test
    public void multipleStackedAreas() throws BlueTruthException {
        Map<String, Collection<FloatPoint>> areaPoints
                = new HashMap<String, Collection<FloatPoint>>();
        areaPoints.put("span-A", Collections.EMPTY_LIST);
        areaPoints.put("span-B", Collections.EMPTY_LIST);
        areaPoints.put("span-C", Collections.EMPTY_LIST);

        areaPoints.put("span-A", Arrays.asList(
                new FloatPoint(1, 230),
                new FloatPoint(2, 200),
                new FloatPoint(3, 440),
                new FloatPoint(4, 170),
                new FloatPoint(5, 260)
        ));

        areaPoints.put("span-B", Arrays.asList(
                new FloatPoint(1, 620),
                new FloatPoint(2, 605),
                new FloatPoint(3, 612),
                new FloatPoint(4, 635),
                new FloatPoint(5, 599)
        ));

        areaPoints.put("span-C", Arrays.asList(
                new FloatPoint(1, 120),
                new FloatPoint(2, 125),
                new FloatPoint(3, 118),
                new FloatPoint(4, 126),
                new FloatPoint(5, 129)
        ));

        csvChartWriter.addStackedArea(areaPoints);

        csvChartWriter.write(outputStream);

        assertEquals(
                "timestamp,span-B,span-A,span-C,total\n"
                + "1,620,230,120,970\n"
                + "2,605,200,125,930\n"
                + "3,612,440,118,1170\n"
                + "4,635,170,126,931\n"
                + "5,599,260,129,988\n",
                outputStream.toString());
    }

    @Test
    public void ignoresLastValuesIfMultipleStackedAreasDifferentSizes() throws BlueTruthException {
        Map<String, Collection<FloatPoint>> areaPoints
                = new HashMap<String, Collection<FloatPoint>>();

        areaPoints.put("span-A", Arrays.asList(
                new FloatPoint(1, 230),
                new FloatPoint(2, 210),
                new FloatPoint(3, 440)
        ));

        areaPoints.put("span-B", Arrays.asList(
                new FloatPoint(1, 220),
                new FloatPoint(2, 200)
        ));

        csvChartWriter.addStackedArea(areaPoints);

        csvChartWriter.write(outputStream);

        assertEquals(
                "timestamp,span-B,span-A,total\n"
                + "1,220,230,450\n"
                + "2,200,210,410\n",
                outputStream.toString());
    }

    @Test(expected = BlueTruthException.class)
    public void throwsAnExceptionIfMultipleStackedTimestampsAreDifferent() throws BlueTruthException {
        Map<String, Collection<FloatPoint>> areaPoints
                = new HashMap<String, Collection<FloatPoint>>();

        areaPoints.put("span-A", Arrays.asList(
                new FloatPoint(1, 230),
                new FloatPoint(2, 210)
        ));

        areaPoints.put("span-B", Arrays.asList(
                new FloatPoint(3, 220),
                new FloatPoint(4, 200)
        ));

        csvChartWriter.addStackedArea(areaPoints);

        csvChartWriter.write(outputStream);
    }

    @Test
    public void exceptionIfMultipleStackedTimestampsAreDifferent() {
        Map<String, Collection<FloatPoint>> areaPoints
                = new HashMap<String, Collection<FloatPoint>>();

        areaPoints.put("span-A", Arrays.asList(
                new FloatPoint(1, 230),
                new FloatPoint(2, 210)
        ));

        areaPoints.put("span-B", Arrays.asList(
                new FloatPoint(3, 220),
                new FloatPoint(4, 200)
        ));

        csvChartWriter.addStackedArea(areaPoints);
        try {
            csvChartWriter.write(outputStream);
        } catch (BlueTruthException ex) {
            assertEquals("Missing timestamp: expected 3, got 1", ex.getMessage());
        }
    }
}
