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

/**
 * Util class for alert messages
 */
@Component
public class HtmlAlertsUtils {

    private final SeleniumWebDriverHelper webDriverHelper;
    public static final String ALERT_SUCCESS = "alert-success";
    public static final String ALERT_DANGER = "alert-danger";

    @Autowired
    public HtmlAlertsUtils(SeleniumWebDriverHelper webDriverHelper) {
        this.webDriverHelper = webDriverHelper;
    }

    public void assertAlertSuccess() {
        WebElementHelper success_element = webDriverHelper.findElement(getXPath(ALERT_SUCCESS));
        Assert.assertTrue(success_element.isDisplayed());
    }

    public void assertAlertError() {
        WebElementHelper success_element = webDriverHelper.findElement(getXPath(ALERT_DANGER));
        Assert.assertTrue(success_element.isDisplayed());
    }

    public void assertAlertError(HtmlModalUtils htmlModalUtils) {
        WebElementHelper success_element;
        success_element = webDriverHelper.findElement(htmlModalUtils.getContentXPath() + getXPath(ALERT_DANGER));
        Assert.assertTrue(success_element.isDisplayed());
    }
    
    public void waitForAlertMessage(String message, String alertClass) throws InterruptedException {
        String xPath = getXPath(alertClass) + "//span[contains(., '" + message + "')]";
        webDriverHelper.waitForElement(xPath, 0, 10);
    }
    
    private String getXPath(String alertClass) {
        return "//div[contains(@class,'" + alertClass + "') and not(contains(@class ,'hide'))]";
    }
}
