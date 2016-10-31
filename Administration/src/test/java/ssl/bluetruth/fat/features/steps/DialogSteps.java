/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.fat.features.steps;

import cucumber.api.DataTable;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import ssl.bluetruth.fat.Controls;

/**
 *
 * @author pwood
 */
public class DialogSteps {

    private final WebDriver browser;
    private final Controls controls;

    public DialogSteps(SharedState sharedState) throws MalformedURLException {
        browser = sharedState.getBrowser();
        controls = new Controls(browser);
    }

    @Then("^the dialog \"([^\"]*)\" is visible$")
    public void the_dialog_is_visible(String dialogTitle) throws Throwable {
        assertTrue(controls.dialog(dialogTitle)
                .isDisplayed());
    }
    
    @When("^I select \"([^\"]*)\" on the dialog$")
    public void I_select_on_the_dialog(String buttonText) throws Throwable {
        WebElement button = controls.currentDialog()
                .findElement(By.xpath(".//button[span[contains(text(),'"+buttonText+"')]]"));
        button.click();
    }    

    @Then("^the check boxes exist:$")
    public void the_check_boxes_exist(DataTable expectedCheckBoxes) throws Throwable {
        List<List<String>> actualCheckboxes = new ArrayList<List<String>>();

        final List<WebElement> checkboxes = browser.findElements(By.xpath("//div/label[input[@type='checkbox']]"));
        for (WebElement checkbox : checkboxes) {
            if (checkbox.isDisplayed()) {
                actualCheckboxes.add(Arrays.asList(checkbox.getText()));
            }
        }
        expectedCheckBoxes.diff(actualCheckboxes);
    }

    @Then("^the check box (.*) exists$")
    public void the_check_box_exists(String checkbox) throws Throwable {
        final String xpath = "//div/label[contains(text(),'" + checkbox + "')][input[@type='checkbox']]";
        assertNotNull(browser.findElement(By.xpath(xpath)));
    }

    @When("^I select checkbox (.*)$")
    public void I_select_checkbox(String checkboxLabel) throws Throwable {
        final String xpath = "//div/label[contains(text(),'" + checkboxLabel + "')]/input[@type='checkbox']";
        WebElement checkbox = browser.findElement(By.xpath(xpath));
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
    }

    @When("^I uncheck checkbox (.*)$")
    public void I_uncheck_checkbox(String checkboxLabel) throws Throwable {
        final String xpath = "//div/label[contains(text(),'" + checkboxLabel + "')]/input[@type='checkbox']";
        WebElement checkbox = browser.findElement(By.xpath(xpath));
        if (checkbox.isSelected()) {
            checkbox.click();
        }
    }

    @When("^I enter \"(.*)\" into the \"(.*)\" field$")
    public void I_enter_into_the_field(String filter, String fieldName) throws Throwable {
        final String xpath = "//div[label[contains(text(),'" + fieldName + "')]]/input";
        WebElement field = browser.findElement(By.xpath(xpath));
        field.clear();
        field.sendKeys(filter);
    }

    private Actions actions() {
        return new Actions(browser);
    }
}
