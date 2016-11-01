package ssl.bluetruth.sat.features.steps;

import com.google.common.base.Function;
import cucumber.api.DataTable;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import static org.junit.Assert.*;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import ssl.bluetruth.selenium.BrowserCreator;

/**
 *
 * @author pwood
 */
public class SatSteps {

    private BrowserCreator browserCreator;
    private WebDriver browser;

    public SatSteps() throws MalformedURLException {
        browserCreator = new BrowserCreator();
    }
    
    @Before
    public void setupDriver() throws MalformedURLException {
        browser = browserCreator.createBrowser();
        
        browser.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        browser.manage().timeouts().pageLoadTimeout(5, TimeUnit.SECONDS);
        browser.manage().timeouts().setScriptTimeout(2, TimeUnit.SECONDS);
    }
    
    @After
    public void closeBrowser() throws MalformedURLException {
        if(browser != null) {
            browser.quit();
        }
    }

    @Given("^I am on (.*)$")
    public void I_am_on(String url) throws Throwable {
        browser.get(url);
    }

    @When("^I enter my username and password$")
    public void I_enter_my_username_and_password() throws Throwable {
        browser.findElement(By.name("j_username")).sendKeys("pwood");
        browser.findElement(By.name("j_password")).sendKeys("ssl1324");
        browser.findElement(By.id("sign-in-button")).click();
    }

    @Then("^the login is successful$")
    public void the_login_is_successful() throws Throwable {
        assertTrue(browser.findElement(By.id("user-links")).getText()
                .contains("signed in as pwood"));
    }

    @Then("^the homepage is displayed$")
    public void the_homepage_is_displayed() throws Throwable {
        assertEquals("http://www.bluetruth.co.uk/Home", browser.getCurrentUrl());
        assertEquals("Cloud Instation - Home", browser.getTitle());
    }

    @Given("^I am logged in$")
    public void I_am_logged_in() throws Throwable {
        browser.findElement(By.name("j_username")).sendKeys("pwood");
        browser.findElement(By.name("j_password")).sendKeys("ssl1324");
        browser.findElement(By.id("sign-in-button")).click();
    }

    @When("^I sign out$")
    public void I_sign_out() throws Throwable {
        browser.findElement(By.id("sign-out-link")).click();
    }

    @Then("^the login page is displayed$")
    public void the_login_page_is_displayed() throws Throwable {
        assertEquals(
                "Sign in",
                browser.findElement(By.id("sign-in-button")).getAttribute("value"));
    }

    @Then("^there is a link to (.*)$")
    public void there_is_a_link_to(String link) throws Throwable {
        assertNotNull(browser.findElement(By.linkText(link)));
    }

    @When("^I select the (.*) link$")
    public void I_select_the_link(String link) throws Throwable {
        browser.findElement(By.partialLinkText(link)).click();
    }

    @Then("^there is an? (.*) table$")
    public void there_is_a_table(String columnName) throws Throwable {
        final List<WebElement> tableHeaders = browser.findElements(By.tagName("th"));
        for (WebElement tableHeader : tableHeaders) {
            if (tableHeader.getText().contains(columnName)) {
                return;
            };
        }
        fail(columnName + " isn't in the table");
    }

    @Then("^the table has at least (\\d+) rows$")
    public void the_table_has_at_least_rows(int expectedRows) {
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

        assertTrue(String.valueOf(totalRows),
                totalRows > expectedRows);
    }

    @Then("^the table headers are:$")
    public void the_table_headers_are(DataTable expectedHeaders) throws Throwable {
        List<List<String>> actualHeaders = new ArrayList<List<String>>();

        final List<WebElement> tableHeaders = browser.findElements(By.xpath("//thead//tr//th/div"));
        for (WebElement tableHeader : tableHeaders) {
            actualHeaders.add(Arrays.asList(tableHeader.getText()));
        }
        expectedHeaders.diff(actualHeaders);
    }
}
