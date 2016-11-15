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
public class SpansConfigurationSteps {

    private final SharedState sharedState;
    private WebDriver browser;

    public SpansConfigurationSteps(SharedState sharedState) throws MalformedURLException {
        this.sharedState = sharedState;
        browser = sharedState.getBrowser();
    }

//    @Before
//    public void setupDriver() throws MalformedURLException {
//        browser.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
//        browser.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
//        browser.manage().timeouts().setScriptTimeout(2, TimeUnit.SECONDS);
//    }
//
//    @After
//    public void closeBrowser() throws MalformedURLException {
//        if (browser != null) {
//            browser.quit();
//        }
//    }
    
    @Given("^I am on the bluetruth website$")
    public void I_am_viewing_the_BlueTruth_website_span_config() throws Throwable {
        browser.get(sharedState.sutUrl);
    }

    @Given("^I have logged in as \"(.*)\" with password \"(.*)\"$")
    public void I_have_logged_in_as_with_password_span_config(String username, String password) throws Throwable {
        browser.findElement(By.name("j_username")).sendKeys(username);
        browser.findElement(By.name("j_password")).sendKeys(password);
        browser.findElement(By.id("sign-in-button")).click();
    }
    
    @Given("^I am viewing spans configuration$")
    public void I_am_viewing_spans_configuration_span_config() throws Throwable {
        goto_configuration_page_span_config();
        browser.findElement(By.id("span-link")).click();
        assertEquals("Cloud Instation - Spans", browser.getTitle());
    }

    private void goto_configuration_page_span_config() {
        browser.findElement(By.id("configuration-link")).click();
        assertEquals("Cloud Instation - Configuration", browser.getTitle());
    }
    
    @When("^I create a new span with span name \"(.*)\", Start Detector \"(.*)\", End Detector \"(.*)\", Stationary \"(.*)\", very slow \"(.*)\", slow \"(.*)\", moderate \"(.*)\"$")
    public void createSpan_span_config(String spanName, String startDet, String endDet, String stat, String vSlow, String slow, String moderate) throws Throwable {
        Thread.sleep(2000);
        fetchBtn_SC("Add new span").click();
        // enter span details
        browser.findElement(By.id("span_name")).clear();
        browser.findElement(By.id("span_name")).sendKeys(spanName);
        new Select(browser.findElement(By.id("start_detector_id"))).selectByVisibleText(startDet);
        new Select(browser.findElement(By.id("end_detector_id"))).selectByVisibleText(endDet);
        browser.findElement(By.id("stationary")).clear();
        browser.findElement(By.id("stationary")).sendKeys(stat);
        browser.findElement(By.id("very_slow")).clear();
        browser.findElement(By.id("very_slow")).sendKeys(vSlow);
        browser.findElement(By.id("slow")).clear();
        browser.findElement(By.id("slow")).sendKeys(slow);
        browser.findElement(By.id("moderate")).clear();
        browser.findElement(By.id("moderate")).sendKeys(moderate);
        
        browser.findElement(By.xpath("//div[div[@id='new-span-dialog']]//button[span[contains(text(), 'Add new span')]]")).click();
    }
    
    @Then("^I observe the new span with span name \"(.*)\" is added to the spans table$")
    public void isSpanAdded_span_config(String spanName) throws Throwable {
        checkSpan_span_config(spanName); 
    }

    @When("^I edit span with name \"(.*)\" to \"(.*)\"$")
    public void I_edit_span_with_name_to_span_config(String currSpanName, String newSpanName) throws Throwable {
        checkSpan_span_config(currSpanName);
        editSpan_span_config(newSpanName);
    }

    @Then("^I observe that the span name is updated to \"(.*)\"$")
    public void I_observe_that_the_span_name_is_updated_to_span_config(String newSpanName) throws Throwable {
        checkSpan_span_config(newSpanName);
    }
 
    @When("^I delete span with name \"(.*)\"$")
    public void I_delete_span_with_name_span_config(String spanName) throws Throwable {
        checkSpan_span_config(spanName);
        deleteSpan_span_config();
    }

    @Then("^I can see span with name \"(.*)\" does not exist in the spans table$")
    public void I_can_see_span_with_name_does_not_exist_in_the_spans_table_span_config(String spanName) throws Throwable {
        filterSpan_span_config(spanName);
        assertEquals(fetchRowCount_span_config(), 1);
        assertTrue(isSpanPresent_span_config());
    }

    private void deleteSpan_span_config() {
        browser.findElement(By.xpath("//table//a[span[contains(text(), 'delete')]]")).click();
        fetchBtn_SC("Confirm").click();
    }
    
    private void editSpan_span_config(String spanName) {
        browser.findElement(By.xpath("//table[@id='span-table']/tbody/tr/td")).click();
        browser.findElement(By.name("value")).clear();
        browser.findElement(By.name("value")).sendKeys(spanName);
        browser.findElement(By.xpath("//form/button")).click();
    }
    
    private void filterSpan_span_config(String spanName) throws InterruptedException {
        fetchBtn_SC("Filter").click();
        WebElement field = browser.findElement(By.xpath("//div[@id='span-filter-dialog']//div[label[contains(text(), 'Span Name')]]/input"));
        field.clear();
        field.sendKeys(spanName);
        Thread.sleep(2000);
        fetchBtn_SC("Close").click();
    }
    
    private WebElement fetchBtn_SC(String btn) {
        return browser.findElement(By.xpath("//button[span[contains(text(),'" + btn +"')]]"));
    }

    private void checkSpan_span_config(String spanName) throws InterruptedException {
        filterSpan_span_config(spanName);
        assertEquals(1, fetchRowCount_span_config());
        assertFalse(isSpanPresent_span_config());
    }  

    private boolean isSpanPresent_span_config() {
        boolean ret = false;
        List<WebElement> tds = browser.findElements(By.xpath("//table[@id='span-table']/tbody/tr/td"));
        if (tds.size() == 1) 
            ret = true;
        return ret;
    }
    
    private int fetchRowCount_span_config() {
        FluentWait<WebDriver> wait = new FluentWait<WebDriver>(browser)
                .withTimeout(30, TimeUnit.SECONDS)
                .pollingEvery(2, TimeUnit.SECONDS)
                .ignoring(StaleElementReferenceException.class);

        int totalRows =
                wait.until(new Function<WebDriver, Integer>() {
            public Integer apply(WebDriver browser) {
                final List<WebElement> tableBodies = browser.findElements(By.tagName("tbody"));
                int totalRows = 0;
                for (WebElement tableBody : tableBodies) {
                    final List<WebElement> tableRows = tableBody.findElements(By.tagName("tr"));
                    totalRows += tableRows.size();
                }
                return totalRows;
            }
        });
        return totalRows;
    } 
}
