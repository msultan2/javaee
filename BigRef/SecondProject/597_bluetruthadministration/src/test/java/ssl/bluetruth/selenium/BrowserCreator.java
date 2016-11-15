/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.selenium;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 *
 * @author pwood
 */
public class BrowserCreator {

    private Map<String, Capabilities> capabilities = new HashMap<String, Capabilities>();
    private URL seleniumServer;

    public BrowserCreator() throws MalformedURLException {
        seleniumServer = new URL("http://localhost:4444/wd/hub");
        
        capabilities.put("firefox", DesiredCapabilities.firefox());
        capabilities.put("chrome", DesiredCapabilities.chrome());
        capabilities.put("explorer", DesiredCapabilities.internetExplorer());
        capabilities.put("htmlunit", DesiredCapabilities.htmlUnitWithJs());
        capabilities.put("safari", DesiredCapabilities.safari());
        capabilities.put("phantomjs", DesiredCapabilities.phantomjs());
        capabilities.put("opera", DesiredCapabilities.opera());
    }
    
    public WebDriver createBrowser() throws MalformedURLException {
        String supportedBrowser = System.getProperty("bluetruth-supported-browser");
        Capabilities capability = capabilities.get(supportedBrowser);
        if(capability != null) {
            return new RemoteWebDriver(seleniumServer, capability);
        } else if(false) {
            return new FirefoxDriver();
        } else {
            HtmlUnitDriver htmlUnit = new HtmlUnitDriver();
            htmlUnit.setJavascriptEnabled(true);
            return htmlUnit;
        }
    }  
}
