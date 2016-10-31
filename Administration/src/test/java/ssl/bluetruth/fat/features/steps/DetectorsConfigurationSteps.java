package ssl.bluetruth.fat.features.steps;

import com.google.common.base.Function;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import static org.junit.Assert.*;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;

/**
 *
 * @author svenkataramanappa
 */
public class DetectorsConfigurationSteps {

    private WebDriver browser;

    public DetectorsConfigurationSteps(SharedState sharedState) throws MalformedURLException {
        browser = sharedState.getBrowser();
    }

    @Before
    public void setupDriver() throws MalformedURLException {
        browser.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        browser.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        browser.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
    }

    @After
    public void closeBrowser() throws MalformedURLException {
        if (browser != null) {
            browser.quit();
        }
    }

    @Given("^I am viewing detectors configuration$")
    public void I_am_viewing_detectors_configuration_detector_config() throws Throwable {
        goto_configuration_page_detector_config();
        browser.findElement(By.id("detector-link")).click();
        assertEquals("Cloud Instation - Detectors", browser.getTitle());
    }

    @When("^I create a new detector with name \"(.*)\", Detector ID \"(.*)\", Latitude \"(.*)\", Longitude \"(.*)\", Mode \"(.*)\", Carriageway \"(.*)\" and logical group \"(.*)\"$")
    public void I_create_a_new_detector_with_name_Detector_ID_Latitude_Longitude_Mode_Carriageway_detector_config(
            String detectorName, String detectorID, String lat,
            String lng, String mode, String carriageWay, String lgroup) throws Throwable {
        Thread.sleep(2000);
        // click add new detector button
        browser.findElement(By.xpath("//button[span[contains(text(), 'Add new detector')]]")).click();

        // fill up detector details
        browser.findElements(By.xpath("//fieldset/input")).clear();
        browser.findElement(By.xpath("//input[@id='name']")).sendKeys(detectorName);
        browser.findElement(By.xpath("//input[@id='id']")).sendKeys(detectorID);
        browser.findElement(By.xpath("//input[@id='latitude']")).sendKeys(lat);
        browser.findElement(By.xpath("//input[@id='longitude']")).sendKeys(lng);

        new Select(browser.findElement(By.xpath("//select[@id='mode']"))).selectByVisibleText(mode);
        new Select(browser.findElement(By.xpath("//select[@id='carriageway']"))).selectByVisibleText(carriageWay);

        addLogicalGroup_detector_config(lgroup);
        browser.findElement(By.xpath("//div[div[form]]//button[span[contains(text(), 'Add new detector')]]")).click();
    }

    @Then("^I can see a detector in the detectors table with name \"(.*)\"$")
    public void I_can_see_a_detector_in_the_detectors_table_with_name_detector_config(String detectorName) throws Throwable {
        checkDetector_detector_config(detectorName);
    }

    @When("^I edit detector with name \"(.*)\" to \"(.*)\"$")
    public void I_edit_detector_with_name_to_detector_config(String currDetectorName, String newDetectorName) throws Throwable {
        checkDetector_detector_config(currDetectorName);
        editDetector_detector_config(newDetectorName);
    }

    @When("^I delete detector with name \"(.*)\"$")
    public void I_delete_detector_with_name_detector_config(String detectorName) throws Throwable {
        checkDetector_detector_config(detectorName);
        deleteDetector_detector_config();
    }

    @Then("^I can see detector with name \"(.*)\" does not exist in the detectors table$")
    public void I_can_see_detector_with_name_does_not_exist_in_the_detectors_table_detector_config(String detectorName) throws Throwable {
        filterDetector_detector_config(detectorName);
        assertEquals(fetchRowCountDetectorTable_detector_config(), 1);
        assertTrue(isDetectorPresent_detector_config());
    }

    @Given("^I have a detector with name \"(.*)\"$")
    public void I_have_a_detector_with_name_detector_config(String detectorName) throws Throwable {
        // do nothing 
    }

    @When("^I edit the detector configuration \"(.*)\" to \"(.*)\" for detector with name \"(.*)\"$")
    public void I_edit_the_detector_configuration_to_for_detector_with_name_detector_config(String detectorProperty, String value, String detectorName) throws Throwable {
        checkDetector_detector_config(detectorName);

        // click detector configuration button
        browser.findElement(By.xpath("//a/span[2][contains(text(), \"configuration\")]")).click();
        assertEquals("Cloud Instation - Detector Configuration", browser.getTitle());

        List<WebElement> divs = browser.findElements(By.xpath("//form/div/div"));
        for (WebElement div : divs) {
            if (div.findElement(By.tagName("label")).getText().contains(detectorProperty)) {
                div.findElement(By.tagName("input")).clear();
                div.findElement(By.tagName("input")).sendKeys(value);
                break;
            }
        }
        browser.findElement(By.xpath("//button[span[contains(text(), 'Save Configuration')]]")).click();
        browser.findElement(By.xpath("//button[span[contains(text(), 'OK')]]")).click();
    }

    @Then("^I can see the detector with name \"(.*)\" the configuration \"(.*)\" set to \"(.*)\"$")
    public void I_can_see_the_detector_with_name_the_configuration_set_to_detector_config(String detectorName, String detectorProperty, String value) throws Throwable {
        assertEquals("Cloud Instation - Detector Configuration", browser.getTitle());

        List<WebElement> divs = browser.findElements(By.xpath("//form/div/div"));
        for (WebElement div : divs) {
            if (div.findElement(By.tagName("label")).getText().contains(detectorProperty)) {
                if (!div.findElement(By.tagName("input")).getAttribute("value").toString().equals(value)) {
                    fail();
                }
                break;
            }
        }
    }

    private void checkDetector_detector_config(String detectorName) throws InterruptedException {
        filterDetector_detector_config(detectorName);
        assertEquals(1, browser.findElements(By.xpath("//table[@id='detector-table']/tbody/tr")).size());
        assertFalse(isDetectorPresent_detector_config());
    }

    private void editDetector_detector_config(String detectorName) {
        browser.findElement(By.xpath("//table/tbody/tr/td")).click();
        WebElement in = browser.findElement(By.xpath("//input"));
        in.clear();
        in.sendKeys(detectorName);
        browser.findElement(By.xpath("//form/button")).click();
    }

    private void deleteDetector_detector_config() {
        browser.findElement(By.xpath("//table//a[span[contains(text(), 'delete')]]")).click();  // click remove link
        fetchBtn_detector_config("Confirm").click();
    }

    private boolean isDetectorPresent_detector_config() {
        boolean ret = false;
        List<WebElement> tds = browser.findElements(By.xpath("//table[@id='detector-table']/tbody/tr/td"));
        if (tds.size() == 1) {
            ret = true;
        }
        return ret;
    }

    private void filterDetector_detector_config(String detectorName) throws InterruptedException {
        WebElement filterBtn = browser.findElement(By.xpath("//div[@id='detector-table_wrapper']//button[span[contains(text(),'Filter')]]"));
        filterBtn.click();
        WebElement field = browser.findElement(By.xpath("//div[@id='detector-filter-dialog']//div[label[contains(text(), 'Detector')]]/input"));
        field.clear();
        field.sendKeys(detectorName);
        Thread.sleep(1000);

        WebElement closeBtn = browser.findElement(By.xpath("//div[div[@id='detector-filter-dialog']]//button[span[contains(text(),'Close')]]"));
        closeBtn.click();
    }

    private void addLogicalGroup_detector_config(String lgroup) {
        List<WebElement> lgroups = browser.findElements(By.xpath("//input[@name='logical_group_names']"));
        for (WebElement ele : lgroups) {
            if (ele.getAttribute("value").toString().equals(lgroup)) {
                if (!ele.isEnabled()) {
                    ele.click();
                }
            }
        }
    }

    private WebElement fetchBtn_detector_config(String btn) {
        return browser.findElement(By.xpath("//button[span[contains(text(),'" + btn + "')]]"));
    }

    private void goto_configuration_page_detector_config() {
        browser.findElement(By.id("configuration-link")).click();
        assertEquals("Cloud Instation - Configuration", browser.getTitle());
    }
    
    private int fetchRowCountDetectorTable_detector_config() {
        FluentWait<WebDriver> wait = new FluentWait<WebDriver>(browser)
                .withTimeout(30, TimeUnit.SECONDS)
                .pollingEvery(10, TimeUnit.SECONDS)
                .ignoring(StaleElementReferenceException.class);

        int totalRows =
                wait.until(new Function<WebDriver, Integer>() {
            public Integer apply(WebDriver browser) {
                final List<WebElement> tableRows = browser.findElements(By.xpath("//table[@id='detector-table']/tbody/tr"));
                int totalRows = tableRows.size();
                return totalRows;
            }
        });
        return totalRows;
    }
}
