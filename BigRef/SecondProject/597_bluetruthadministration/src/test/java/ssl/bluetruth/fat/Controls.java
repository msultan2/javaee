/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.fat;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 *
 * @author pwood
 */
public class Controls {
    private final WebDriver browser;
    private String currentDialog;

    public Controls(WebDriver browser) {
        this.browser = browser;
    }

    public void selectLinkOrButton(String text) {
        new Actions(browser)
                .moveToElement(linkOrButton(text))
                .click()
                .pause(1000)
                .perform();
    }
    
    public boolean linkOrButtonExists(String text) {
        return null != linkOrButton(text);
    }

    private WebElement linkOrButton(String text) {
        String xpath =
                "(//a | //a/span | //button | //button/span)[contains(text(),'" + text + "')]";
        return elementByXPath(xpath);
    }

    public Object javascript(String snippet) {
        JavascriptExecutor executor = (JavascriptExecutor)browser;
        return executor.executeScript(snippet);
    }

    public WebElement elementByXPath(String xpath) {
        return browser.findElement(By.xpath(xpath));
    }

    public List<WebElement> elementsByXPath(String xpath) {
        return browser.findElements(By.xpath(xpath));
    }

    public WebElement dialog(String dialogTitle) {
        String contains = "[contains(text(),'" + dialogTitle + "')]";
        currentDialog = "(//div[span"+contains+"] | //div[h1"+contains+"])";
        return currentDialog();
    }

    public WebElement currentDialog() {
        return elementByXPath(currentDialog);
    }
}
