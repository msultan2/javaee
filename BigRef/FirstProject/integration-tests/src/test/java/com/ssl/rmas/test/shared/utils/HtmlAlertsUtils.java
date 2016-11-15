 /*
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2016 © Simulation Systems Ltd. All Rights Reserved.
 *
 */
package com.ssl.rmas.test.shared.utils;

import com.ssl.cukes.SeleniumWebDriverHelper;
import com.ssl.cukes.SeleniumWebDriverHelper.WebElementHelper;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.junit.Assert.assertTrue;

 /**
 * Util class for alert messages
 */
@Component
public class HtmlAlertsUtils {

    private final SeleniumWebDriverHelper webDriverHelper;
    public static final String ALERT_SUCCESS = "alert-success";
    public static final String ALERT_DANGER = "alert-danger";
    public static final String ALERT_WARNING = "alert-warning";

    @Autowired
    public HtmlAlertsUtils(SeleniumWebDriverHelper webDriverHelper) {
        this.webDriverHelper = webDriverHelper;
    }

    public void assertAlertSuccess() {
        WebElementHelper success_element = webDriverHelper.findElement(getXPathDiv(ALERT_SUCCESS));
        Assert.assertTrue(success_element.isDisplayed());
    }

    public void assertAlertError() {
        WebElementHelper success_element = webDriverHelper.findElement(getXPathDiv(ALERT_DANGER));
        Assert.assertTrue(success_element.isDisplayed());
    }

    public void assertAlertError(HtmlModalUtils htmlModalUtils) {
        WebElementHelper success_element;
        success_element = webDriverHelper.findElement(htmlModalUtils.getContentXPath() + getXPathDiv(ALERT_DANGER));
        Assert.assertTrue(success_element.isDisplayed());
    }

    public void assertAlertNotPresent(String message, String alertClass) {
        Assert.assertTrue(webDriverHelper.isElementNotPresent(getXPathSpan(message, alertClass)));
    }
    
    public void waitForAlertMessage(String message, String alertClass) {
        String xPath = getXPathSpan(message, alertClass);
        webDriverHelper.waitForElement(xPath, 0, 10);
    }

    public String getAlertMessage(String message, String alertClass){
        String xPath = getXPathSpan(message, alertClass);
        return webDriverHelper.waitForElement(xPath, 0, 10).getText();
    }

    public String getXPathSpan(String message, String alertClass) {
        return getXPathDiv(alertClass) +"//span[contains(., '" + message + "')]";
    }

    private String getXPathDiv(String alertClass) {
        return "//div[contains(@class,'" + alertClass + "') and not(contains(@class ,'hide'))]";
    }

    private String getXPathDiv(String message, String alertClass) {
        return "//div[contains(@class,'" + alertClass + "') and contains(., '" + message + "') and not(contains(@class ,'hide'))]";
    }

     private void assertSuccess(String titleText) {
         assertTrue(webDriverHelper.findElement(getXPathSpan(titleText, ALERT_SUCCESS)).isDisplayed());
     }

    private void assertFailure(String titleText) {
        assertTrue(webDriverHelper.findElement(getXPathSpan(titleText, ALERT_DANGER)).isDisplayed());
    }

     private void closeSuccess(String titleText) {
         webDriverHelper.findElement(getXPathDiv(titleText, ALERT_SUCCESS)+"//button").click();
     }

     public void assertAndCloseSuccess(String titleText) {
         assertSuccess(titleText);
         closeSuccess(titleText);
     }

    private void closeFailure(String titleText) {
        webDriverHelper.findElement(getXPathDiv(titleText, ALERT_DANGER) + "//button").click();
    }

    public void assertAndCloseFailure(String titleText) {
        assertFailure(titleText);
        closeFailure(titleText);
    }
}
