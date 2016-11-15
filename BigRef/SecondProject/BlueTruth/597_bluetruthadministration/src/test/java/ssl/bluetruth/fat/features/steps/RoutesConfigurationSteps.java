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

/**
 *
 * @author svenkataramanappa
 */
public class RoutesConfigurationSteps {

    private WebDriver browser;

    public RoutesConfigurationSteps(SharedState sharedState) throws MalformedURLException {
        browser = sharedState.getBrowser();
    }

    @Before
    public void setupDriver() throws MalformedURLException {
        browser.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        browser.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        browser.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
    }

    @After
    public void closeBrowser() throws MalformedURLException {
        if (browser != null) {
            browser.quit();
        }
    }

    @Given("^I am viewing routes configuration$")
    public void I_am_viewing_routes_config_routes_config() throws Throwable {
        goto_configuration_page_routes_config();
        browser.findElement(By.id("route-link")).click();
        assertEquals("Cloud Instation - Routes", browser.getTitle());
    }

    @When("^I create a new route with name \"(.*)\" and description \"(.*)\"$")
    public void I_create_a_new_route_with_name_and_description_routes_config(String routeName, String routeDesc) throws Throwable {
        Thread.sleep(2000);

        fetchBtn_RC("Add new route").click();

        WebElement routeField = browser.findElement(By.xpath("//input[@id='route_name']"));
        routeField.clear();
        routeField.sendKeys(routeName);

        WebElement routeDescription = browser.findElement(By.xpath("//textArea"));
        routeDescription.clear();
        routeDescription.sendKeys(routeDesc);

        browser.findElement(By.xpath("//div[div[@id='new-route-dialog']]//button[span[contains(text(), 'Add new route')]]")).click();
    }

    @Then("^I can see a route in the routes table with name \"(.*)\" and description \"(.*)\"$")
    public void I_can_see_a_route_in_the_routes_table_with_name_and_description_routes_config(String routeName, String routeDesc) throws Throwable {
        checkRoute_routes_config(routeName);
    }
 
    @When("^I edit route with name \"(.*)\" to \"(.*)\"$")
    public void I_edit_route_with_name_to_routes_config(String currRouteName, String newRouteName) throws Throwable {
        checkRoute_routes_config(currRouteName);
        editRoute_routes_config(newRouteName);
    }

    @When("^I delete route with name \"(.*)\"$")
    public void I_delete_route_with_name_routes_config(String routeName) throws Throwable {
        checkRoute_routes_config(routeName);
        fetchLink_RC("delete").click();
        deleteRoute_routes_config();
    }

    @Then("^I can see route with name \"(.*)\" does not exist in the routes table$")
    public void I_can_see_route_with_name_does_not_exist_in_the_routes_table_routes_config(String routeName) throws Throwable {
        filterRoute_routes_config(routeName);
        assertTrue(isRoutePresent_routes_config());
    }

    @Given("^I have a span with name \"(.*)\"$")
    public void I_have_a_span_with_name_routes_config(String spanName) throws Throwable {
        // do nothing
    }

    @Given("^I have a route with name \"(.*)\"$")
    public void I_have_a_route_with_name_routes_config(String routeName) throws Throwable {
        // do nothing
    }

    @When("^I add span \"(.*)\" to route \"(.*)\"$")
    public void I_add_span_to_route_routes_config(String spanName, String routeName) throws Throwable {
        filterRoute_routes_config(routeName);
        browser.findElement(By.xpath("//table[@id='routes-table']/tbody/tr/td[3]/a/span[2]")).click();  // view button
        filterSpanInRouteTable_routes_config(spanName);
        fetchLink_RC("Add").click();
    }
    
    @Then("^I can see span \"(.*)\" added to route \"(.*)\"$")
    public void I_can_see_span_added_to_route_routes_config(String spanName, String routeName) throws Throwable {
        filterSpanInRouteTable_routes_config(spanName);
        boolean status = false;
        for (WebElement action : browser.findElements(By.xpath("//table[@id='route-spans-table']/tbody/tr[1]/td[3]/a/span[2]"))) {
            if (action.getText().equals("Remove")) {
                status = true;
                break;
            }
        }
        assertTrue(status);
    }

    @When("^I remove span \"(.*)\" from route \"(.*)\"$")
    public void I_remove_span_from_route_routes_config(String spanName, String routeName) throws Throwable {
        filterRoute_routes_config(routeName);
        browser.findElement(By.xpath("//table[@id='routes-table']/tbody/tr/td[3]/a/span[2]")).click();  // view button
        filterSpanInRouteTable_routes_config(spanName);
        fetchLink_RC("Remove").click();
    }

    @Then("^I can see span \"(.*)\" removed from route \"(.*)\"$")
    public void I_can_see_span_removed_from_route_routes_config(String spanName, String routeName) throws Throwable {
        filterSpanInRouteTable_routes_config(spanName);
        boolean status = false;
        for (WebElement action : browser.findElements(By.xpath("//table[@id='route-spans-table']/tbody/tr[1]/td[3]/a/span[2]"))) {
            if (action.getText().equals("Add")) {
                status = true;
                break;
            }
        }
        assertTrue(status);
    }

    private void filterSpanInRouteTable_routes_config(String spanName) throws InterruptedException {
        fetchBtn_RC("Filter").click();
        WebElement field = browser.findElement(By.xpath("//div[@id='span-filter-dialog']//div[label[contains(text(), 'Span Name')]]/input"));
        field.clear();
        field.sendKeys(spanName);
        Thread.sleep(500);
        fetchBtn_RC("Close").click();
    }
    
    private WebElement fetchBtn_RC(String btn) {
        return browser.findElement(By.xpath("//button[span[contains(text(),'" + btn + "')]]"));
    }

    private WebElement fetchLink_RC(String a) {
        return browser.findElement(By.xpath("//a[span[contains(text(), '"+ a +"')]]"));
    }
    
    private void checkRoute_routes_config(String routeName) throws InterruptedException {
        filterRoute_routes_config(routeName);
        assertEquals(1, fetchRowCount_routes_config());
        assertFalse(isRoutePresent_routes_config());
    }  

    private boolean isRoutePresent_routes_config() {
        boolean ret = false;
        List<WebElement> tds = browser.findElements(By.xpath("//table[@id='routes-table']/tbody/tr/td"));
        if (tds.size() == 1) 
            ret = true;
        return ret;
    }
    
    private void deleteRoute_routes_config() {
        browser.findElement(By.xpath("//table//a[span[contains(text(), 'delete')]]")).click();
        fetchBtn_RC("Confirm").click();
    }
    
    private void editRoute_routes_config(String routeName) {
        browser.findElement(By.xpath("//table[@id='routes-table']/tbody/tr/td")).click();
        browser.findElement(By.name("value")).clear();
        browser.findElement(By.name("value")).sendKeys(routeName);
        browser.findElement(By.xpath("//form/button")).click();
    }
    
    private void goto_configuration_page_routes_config() {
        browser.findElement(By.id("configuration-link")).click();
        assertEquals("Cloud Instation - Configuration", browser.getTitle());
    }

    private void filterRoute_routes_config(String routeName) throws InterruptedException {
        fetchBtn_RC("Filter").click();
        WebElement field = browser.findElement(By.xpath("//div[@id='route-filter-dialog']//div[label[contains(text(), 'Route Name')]]/input"));
        field.clear();
        field.sendKeys(routeName);
        Thread.sleep(2000);
        fetchBtn_RC("Close").click();
    }

    private int fetchRowCount_routes_config() {
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
