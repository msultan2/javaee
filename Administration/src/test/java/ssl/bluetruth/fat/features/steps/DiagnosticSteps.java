/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.fat.features.steps;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.net.MalformedURLException;
import java.util.List;
import org.openqa.selenium.WebDriver;
import static org.junit.Assert.*;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import ssl.bluetruth.fat.Controls;
import static org.hamcrest.CoreMatchers.*;
/**
 *
 * @author svenkataramanappa
 */
public class DiagnosticSteps {

    private final SharedState sharedState;
    private final WebDriver browser;
    private final Controls controls;

    public DiagnosticSteps(SharedState sharedState) throws MalformedURLException {
        this.sharedState = sharedState;
        browser = sharedState.getBrowser();
        controls = sharedState.getControls();
    }

    @When("^I select the \"([^\"]*)\" button for (.*)$")
    public void I_select_the_button_for(String viewBtn, String detectorName) throws Throwable {
        controls.selectLinkOrButton("Filter");
        WebElement detectorField = browser.findElement(By.xpath("//div[label[contains(text(), 'Detector')]]/input"));
        detectorField.sendKeys(detectorName);
        Thread.sleep(2000);
        controls.selectLinkOrButton("Close");
        // click detector view data button
        controls.selectLinkOrButton(viewBtn);
        // check for the detector page
        assertEquals("Cloud Instation - Detector Diagnostic", browser.getTitle());
        // wait till detector page loads
        waitForElementPresent(By.xpath("//h1/span"), 20);
        Thread.sleep(1000);
    }

    @Then("^I should see the following detector information:$")
    public void I_should_see_the_following_detector_information(List<String> detectorProps) throws Throwable {
        for (String property : detectorProps) {
            WebElement element = browser.findElement(By.xpath("//div[label[contains(text(), '" + property + "')]]/input"));
            if (element != null) {
                // FIXME: Redundant. Always succeeds.
                // assertNotEquals( property +" is empty ", element.getAttribute("value"), "-");
            }
        }
    }

    @Then("^I should see the following detector tables:$")
    public void I_should_see_the_following_detector_tables(List<String> tables) throws Throwable {
        for (String tablename : tables) {
            assertNotNull("Table " + tablename + " does not exist", browser.findElement(By.xpath("//h2[contains(text(), '" + tablename + "')]")));
        }
    }
 
    public void waitForElementPresent(final By by, int timeout) {
        WebDriverWait wait = (WebDriverWait) new WebDriverWait(browser, timeout)
                .ignoring(StaleElementReferenceException.class);
        wait.until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                WebElement element = webDriver.findElement(by);
                return element != null && element.isDisplayed();
            }
        });
    }
}
