/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.fat.features.steps;

import cucumber.api.Scenario;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.net.MalformedURLException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import ssl.bluetruth.fat.Controls;
import static ssl.bluetruth.fat.features.steps.RegexMatcher.matches;

/**
 *
 * @author pwood
 */
public class GeneralFunctionalitySteps {

    private final SharedState sharedState;
    private final WebDriver browser;
    private final Controls controls;
    private String lastFileContents;

    public GeneralFunctionalitySteps(SharedState sharedState) throws MalformedURLException {
        this.sharedState = sharedState;
        browser = sharedState.getBrowser();
        controls = sharedState.getControls();
    }

    @Given("^the test data is deployed and unmodified$")
    public void the_test_data_is_deployed_and_unmodified() throws Throwable {
        // @TODO
    }

    @Given("^the user has all user roles$")
    public void the_user_has_all_user_roles() throws Throwable {
        sharedState.username = "cucumber";
        sharedState.password = "12";
    }

    @Given("^the user is logged into the Instation$")
    public void the_user_is_logged_into_the_Instation() throws Throwable {
        browser.get(sharedState.sutUrl);
        browser.findElement(By.name("j_username")).sendKeys(sharedState.username);
        browser.findElement(By.name("j_password")).sendKeys(sharedState.password);
        browser.findElement(By.id("sign-in-button")).click();
    }

    @Given("^I am on the bluetruth test website$")
    public void I_am_viewing_the_BlueTruth_website() throws Throwable {
        browser.get(sharedState.sutUrl);
    }

    @When("^I select the \"(.*)\" link|button$")
    public void I_select_the_link(String text) throws Throwable {
        controls.selectLinkOrButton(text);
    }

    @Given("^I am on the bluetruth (.*) page$")
    public void I_am_on_the_bluetruth_page(String page) throws Throwable {
        controls.selectLinkOrButton(page);
    }

    @Then("^the page has a link to (.*)$")
    public void the_page_has_a_link_to(String link) {
        assertTrue(controls.linkOrButtonExists(link));
    }

    @Then("^the page has a button to (.*)$")
    public void the_page_has_a_button(String link) {
        assertTrue(controls.linkOrButtonExists(link));
    }

    @Then("^the page title is \"(.*)\"$")
    public void the_page_title_is(String title) throws Throwable {
        assertEquals(title, browser.getTitle());
    }

    @Then("^the page title matches \"(.*)\"$")
    public void the_page_title_matches(String re) throws Throwable {
        assertThat(browser.getTitle(), matches(re));
    }

    @When("^I download the file at link \"(.*)\"$")
    public void I_download_the_file_at_link(String buttonText) throws Throwable {
        String xpath = "//div/button[span[contains(text(),'" + buttonText + "')]]";
        browser.findElement(By.xpath(xpath)).click();
        lastFileContents = browser.getPageSource();
    }

    @Then("^the first line of the file is:$")
    public void the_file_contents_are(String expectedContents) throws Throwable {
        assertTrue(lastFileContents,
                lastFileContents.startsWith(expectedContents));
    }
}
