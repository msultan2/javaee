/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.fat.features.steps;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import ssl.bluetruth.fat.Controls;
import ssl.bluetruth.selenium.BrowserCreator;

/**
 *
 * @author pwood
 */
public class SharedState {

    private BrowserCreator browserCreator;
    public final WebDriver browser;
    public final String sutUrl = "http://37.152.43.178";
    public String username = "";
    public String password = "";
    
    public SharedState() throws MalformedURLException {
        browserCreator = new BrowserCreator();
        browser = browserCreator.createBrowser();
        browser.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        browser.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        browser.manage().timeouts().setScriptTimeout(30, TimeUnit.SECONDS);
        browser.manage().window().setSize(new Dimension(1024, 768));
    }
    
    WebDriver getBrowser() {
        return browser;
    }
    
    Controls getControls() {
        return new Controls(browser);
    }
}

