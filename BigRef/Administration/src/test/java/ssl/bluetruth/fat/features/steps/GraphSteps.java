/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.fat.features.steps;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ssl.bluetruth.fat.Controls;

/**
 *
 * @author pwood
 */
public class GraphSteps {

    private final SharedState sharedState;
    private final WebDriver browser;
    private final Controls controls;
    private Scenario scenario;

    public GraphSteps(SharedState sharedState) throws MalformedURLException {
        this.sharedState = sharedState;
        browser = sharedState.getBrowser();
        controls = sharedState.getControls();
    }

    @Before
    public void captureScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    @Given("^I want to see a graph like (.*)$")
    public void I_want_to_see_a_graph_like(String imageName) throws Throwable {
        byte[] expectedGraph = toBytes(getImage(imageName));
        scenario.embed(expectedGraph, "image/png");
    }

    @Then("^the page has a graph like (.*)$")
    public void the_page_has_a_graph_like(String imageName) throws Throwable {
        byte[] expectedGraph = toBytes(getImage(imageName));
        
        WebElement img = xpath("//div[@id='graph']/img");
        String imgSrc = img.getAttribute("src");

        WebClient webClient = new WebClient();

        HtmlPage page = webClient.getPage(sharedState.sutUrl);
        HtmlForm formByName = page.getForms().get(0);
        HtmlInput usernameField = formByName.getInputByName("j_username");
        HtmlInput passwordField = formByName.getInputByName("j_password");
        HtmlInput submit = formByName.getInputByValue("Sign in");

        usernameField.setValueAttribute(sharedState.username);
        passwordField.setValueAttribute(sharedState.password);
        submit.click();
        byte[] actualGraph = toBytes(webClient.getPage(imgSrc).getWebResponse().getContentAsStream());
        if (Arrays.equals(actualGraph, expectedGraph)) {
            // okay
        } else {
            scenario.embed(actualGraph, "image/png");
            fail("Expected graph didn't match actual graph");
        }
    }

    @When("^I configure the graph start and end dates:$")
    public void I_configure_the_graph_start_and_end_dates(DataTable dates) throws Throwable {
        controls.selectLinkOrButton("Graph Controls");

        List<List<String>> raw = dates.raw();
        String startDate = raw.get(0).get(1);
        String endDate = raw.get(1).get(1);

        String startDateInputXPath = "//div[label[contains(text(), 'Start Date:')]]/input";
        String endDateInputXPath = "//div[label[contains(text(), 'End Date:')]]/input";
        
        selectDate(startDateInputXPath, startDate);
        selectDate(endDateInputXPath, endDate);
        
        controls.selectLinkOrButton("Refresh");
    }

    private InputStream getImage(String imageName) {
        return getClass().getResourceAsStream("/ssl/bluetruth/fat/images/" + imageName + ".png");
    }

    private byte[] toBytes(InputStream inputStream) throws IOException {
        return IOUtils.toByteArray(inputStream);
    }

    private void selectDate(String dateInputXPath, String date) {
        WebElement dateInput = xpath(dateInputXPath);
        dateInput.click();
        selectMonthYear(dateInputXPath, date);
        selectDay(date);
        selectTime(date);
        assertEquals(
                date,
                xpath(dateInputXPath).getAttribute("value"));
    }

    private void selectMonthYear(String dateInputXPath, String date) {
        String yearMonth = yearMonth(date);
        while(!hasText(dateInputXPath, yearMonth)) {
            xpath("//a[@title='Prev']").click();
        }
    }

    private void selectDay(String date) {
        String day = day(date);
        xpath("//tbody/tr/td/a[contains(concat(' ',text(),' '),' "+day+" ')]")
                .click();
    }

    private String day(String date) throws NumberFormatException {
        return String.valueOf(Integer.parseInt(date.substring(8, 10)));
    }

    private void selectTime(String date) {
        selectHour(hour(date));
        selectMinute(minute(date));
        selectSecond(second(date));
    }

    private int hour(String date) {
        return Integer.parseInt(date.substring(11, 13));
    }

    private int minute(String date) {
        return Integer.parseInt(date.substring(14, 16));
    }

    private int second(String date) {
        return Integer.parseInt(date.substring(17, 19));
    }

    private void selectHour(int hour) {
        String sliderClass = "ui_tpicker_hour";
        final int maxValue = 23;
        selectSlider(sliderClass, maxValue, hour);
    }

    private void selectMinute(int minute) {
        String sliderClass = "ui_tpicker_minute";
        final int maxValue = 59;
        selectSlider(sliderClass, maxValue, minute);
    }

    private void selectSecond(int second) {
        String sliderClass = "ui_tpicker_second";
        final int maxValue = 59;
        selectSlider(sliderClass, maxValue, second);
    }

    private void selectSlider(String sliderClass, int maxValue, int value) {
        final WebElement hourSlider = xpath("//dd[@class='"+sliderClass+"']/div/a");
        hourSlider.click();
        for(int i = 0; i < maxValue; ++i) {
            hourSlider.sendKeys(Keys.ARROW_LEFT);
        }
        for(int i = 0; i < value; ++i) {
            hourSlider.sendKeys(Keys.ARROW_RIGHT);
        }
    }

    private WebElement xpath(String xpath) {
        return browser.findElement(By.xpath(xpath));
    }

    private boolean hasText(String dateInputXPath, String desiredText) {
        final String actualText = xpath(dateInputXPath).getAttribute("value");
        return actualText.contains(desiredText);
    }

    private String yearMonth(String date) {
        return date.substring(0, 7);
    }
}
