/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.fat.features.steps;

import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import ssl.bluetruth.fat.Controls;
import static org.junit.Assert.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 *
 * @author pwood
 */
public class MapSteps {

    private final WebDriver browser;
    private final Controls controls;
    private static final double prestonLat = 53.73571574532637;
    private static final double prestonLng = -2.63671875;
    private static final Map<String, RelativePositionChecker> relativePositionCheckers =
            new HashMap<String, RelativePositionChecker>();
    private static final Map<String, Keys> arrowKeys = new HashMap<String, Keys>();
    private static final Map<String, String> placeLatLongs = new HashMap<String, String>();
    private static final Map<String, Span> spans = new HashMap<String, Span>();

    static {
        relativePositionCheckers.put("north", new IsNorthOf());
        relativePositionCheckers.put("south", new IsSouthOf());
        relativePositionCheckers.put("east", new IsEastOf());
        relativePositionCheckers.put("west", new IsWestOf());

        arrowKeys.put("up", Keys.ARROW_UP);
        arrowKeys.put("right", Keys.ARROW_RIGHT);
        arrowKeys.put("down", Keys.ARROW_DOWN);
        arrowKeys.put("left", Keys.ARROW_LEFT);
        arrowKeys.put("+", Keys.ADD);
        arrowKeys.put("-", Keys.SUBTRACT);

        placeLatLongs.put("Leeds", "53.79740645735382, -1.54632568359375");
        placeLatLongs.put("York", "53.962145476876934, -1.0800933837890623");
        placeLatLongs.put("Bradford", "53.79436463347384, -1.7499160766601562");
        placeLatLongs.put("Market Weighton", "53.86710508973725, -0.668792724609375");
        placeLatLongs.put("Detector A", "53.73571574532637, -2.63671875");
        placeLatLongs.put("Detector B", "51.13684, -2.735261");
        
        spans.put("Span A", new Span("Detector A", "Detector B"));
    }

    public MapSteps(SharedState sharedState) throws MalformedURLException {
        browser = sharedState.getBrowser();
        controls = sharedState.getControls();

    }

    @Given("^the map is centred on Preston$")
    public void centre_map_on_Preston() throws Throwable {
        Thread.sleep(2000);
        centreMap(prestonLat+","+prestonLng);
        Thread.sleep(2000);
        assertEquals(prestonLat, mapCentreLatitude(), 1e-4);
        assertEquals(prestonLng, mapCentreLongitude(), 1e-4);
    }

    @When("^I press the (.*) key$")
    public void I_press_the_key(String arrow) throws Throwable {
        final Keys arrowKey = arrowKeys.get(arrow);
        browser.findElement(By.id("map-canvas")).sendKeys(arrowKey);
        Thread.sleep(1000);
    }

    @Then("^the map is centred (.*) of Preston$")
    public void the_map_is_centred_relative_to_Preston(String relativePosition) throws Throwable {
        relativePositionCheckers.get(relativePosition)
                .check(mapCentre(), prestonLat, prestonLng);
    }

    @Given("^the map includes:$")
    public void make_map_contain(List<String> places) throws Throwable {
        Thread.sleep(2000);
        assertEquals(2, places.size());
        javascript("map.fitBounds(" + leafletLatLngBounds(places.get(0), places.get(1)) + ")");
        Thread.sleep(1000);
    }

    @Then("^the map excludes:$")
    public void the_map_wont_contain(List<String> places) throws Throwable {
        for (String place : places) {
            assertFalse("Map shouldn't contain " + place,
                    mapContains(place));
        }
    }

    @Then("^the map will exclude (.*)$")
    public void the_map_wont_contain(String place) throws Throwable {
        assertFalse("Map shouldn't contain " + place,
                mapContains(place));
    }

    @Then("^the map will include (.*)$")
    public void the_map_will_contain(String place) throws Throwable {
        assertTrue("Map should contain " + place,
                mapContains(place));
    }

    @Given("^the \"([^\"]*)\" detector is visible$")
    public void the_detector_is_visible(String detector) throws Throwable {
        Thread.sleep(2000);
        centreMap(placeLatLongs.get(detector));
        Thread.sleep(1000);
    }

    @When("^I select the \"([^\"]*)\" detector$")
    public void I_select_the_detector(String detector) throws Throwable {
        click("//div[@class='leaflet-marker-pane']/img[contains(@class,'leaflet-clickable')]");
        Thread.sleep(1000);
    }

    @Then("^a popup for \"([^\"]*)\" appears$")
    public void a_popup_for_appears_containing(String expectedDetector) throws Throwable {
        WebElement actualDetector =
                controls.elementByXPath("//div[@class='detector-popup']/div[@class='detector-name']");
        assertEquals(expectedDetector, actualDetector.getText());
    }

    @Then("^the detector popup contains fields:$")
    @And("^the span popup contains fields:$")
    public void the_detector_popup_contains(DataTable expectedFields) throws Throwable {
        List<WebElement> popupRows = controls.elementsByXPath(
                "//div[@class='detector-popup']/div[@class='row'][div[@class='key']]");
        List<List<String>> actualFields = new ArrayList<List<String>>();
        for(WebElement popupRow: popupRows) {
            List<WebElement> field = popupRow.findElements(By.xpath("div"));
            String key = field.get(0).getText();
            String value = field.get(1).getText();
            actualFields.add(Arrays.asList(key, value));
        }
        expectedFields.diff(actualFields);
    }

    @Then("^the detector popup contains links to:$")
    @And("^the span popup contains links to:$")
    public void the_detector_popup_contains_links_to(DataTable expectedLinks) throws Throwable {
        List<WebElement> links = controls.elementsByXPath(
                "//div[@class='detector-popup']/div[@class='row']/a");
        List<List<String>> actualLinks = new ArrayList<List<String>>();
        for(WebElement link: links) {
            actualLinks.add(Arrays.asList(link.getText()));
        }
        expectedLinks.diff(actualLinks);
    }
    
    @Given("^the \"([^\"]*)\" span is visible$")
    public void make_span_visible(String spanName) throws Throwable {
        final Span span = spans.get(spanName);
        make_map_contain(Arrays.asList(span.from, span.to));
    }

    @When("^I select the \"([^\"]*)\" span$")
    public void I_select_the_span(String span) throws Throwable {
        click("//div/*[name()='svg']/*[name()='g']/*[@class='leaflet-clickable']");
    }
    
    @When("^I click the map north-east of \"([^\"]*)\"$")
    public void I_click_the_map_north_east_of(String place) throws Throwable {
        String xpath =
                "//div[@class='leaflet-marker-pane']/img[contains(@class,'leaflet-clickable')]";
        WebElement detector = browser.findElement(By.xpath(xpath));
        actions()
                .moveToElement(detector)
                .moveByOffset(300, -300)
                .click()
                .perform();
    }

    @Then("^the detector latitude > ([-\\d\\.]+)$")
    public void the_detector_latitude_is_greater_than(double latitude) throws Throwable {
        throw new PendingException();
    }

    @Then("^the detector longitude > ([-\\d\\.]+)$")
    public void the_detector_longitude_is_greater_than(double longitude) throws Throwable {
        throw new PendingException();
    }

    private Object javascript(String snippet) {
        return controls.javascript(snippet);
    }

    private Double mapCentreLatitude() {
        return (Double) javascript("return map.getCenter().lat");
    }

    private Double mapCentreLongitude() {
        return (Double) javascript("return map.getCenter().lng");
    }

    private void centreMap(String latLng) {
        javascript("map.setView(["+latLng+"], 13)");
    }

    private Map mapCentre() {
        return (Map) javascript("return map.getCenter()");
    }

    private String leafletLatLngBounds(String placeOne, String placeTwo) {
        return "L.latLngBounds(" + leafletLatLng(placeOne) + "," + leafletLatLng(placeTwo) + ")";
    }

    private String leafletLatLng(String place) {
        return "L.latLng("+getPlaceLatLng(place)+")";
    }

    private boolean mapContains(String place) {
        return (Boolean) javascript("return map.getBounds().contains(L.latLng("+getPlaceLatLng(place)+"))");
    }

    private String getPlaceLatLng(String place) {
        final String placeLatLng = placeLatLongs.get(place);
        assertNotNull(place + " LatLng isn't defined", placeLatLng);
        return placeLatLng;
    }

    private void click(String xpath) {
        browser.findElement(By.xpath(xpath)).click();
    }

    private Actions actions() {
        return new Actions(browser);
    }

    private static class IsNorthOf implements RelativePositionChecker {

        @Override
        public void check(Map position, double latitude, double longitude) {
            Double actualLatitude = (Double) position.get("lat");
            assertTrue(String.format("%s should be greater than %s", actualLatitude, latitude),
                    actualLatitude > latitude);
        }
    }

    private static class IsEastOf implements RelativePositionChecker {

        @Override
        public void check(Map position, double latitude, double longitude) {
            Double actualLongitude = (Double) position.get("lng");
            assertTrue(String.format("%s should be greater than %s", actualLongitude, longitude),
                    actualLongitude > longitude);
        }
    }

    private static class IsSouthOf implements RelativePositionChecker {

        @Override
        public void check(Map position, double latitude, double longitude) {
            Double actualLatitude = (Double) position.get("lat");
            assertTrue(String.format("%s should be less than %s", actualLatitude, latitude),
                    actualLatitude < latitude);
        }
    }

    private static class IsWestOf implements RelativePositionChecker {

        @Override
        public void check(Map position, double latitude, double longitude) {
            Double actualLongitude = (Double) position.get("lng");
            assertTrue(String.format("%s should be less than %s", actualLongitude, longitude),
                    actualLongitude < longitude);
        }
    }

    private static class Span {
        public final String from;
        public final String to;

        public Span(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }
}
