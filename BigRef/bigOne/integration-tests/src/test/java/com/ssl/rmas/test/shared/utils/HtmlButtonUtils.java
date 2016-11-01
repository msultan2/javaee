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

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ssl.cukes.SeleniumWebDriverHelper;
import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.HtmlDeviceFilterUtils;

@Component
public class HtmlButtonUtils {

    public static final String REFRESH_BUTTON = "Refresh";
    public static final String SAVE_BUTTON = "Save";
    public static final String EDIT_BUTTON = "Edit";
    public static final String SEARCH_BUTTON = "Search";
    public static final String PLUS_BUTTON_CLASS = "glyphicon-plus";
    public static final String OK_BUTTON = "Ok";

    @Autowired private SeleniumWebDriverHelper webDriverHelper;

    public void click(String buttonName) {
        webDriverHelper.findElement(getButtonXPath(buttonName)).click();
    }

    public void click(HtmlTableUtils htmlTableUtils, String rowName, String headerName, String buttonName) {
        htmlTableUtils.getElement(rowName, headerName, getButtonXPath(buttonName)).click();
    }

    public void click(HtmlModalUtils htmlModalUtils, String buttonName) {
        webDriverHelper.findElement(htmlModalUtils.getFooterXPath()+getButtonXPath(buttonName)).click();
    }

    public void click(HtmlDeviceFilterUtils HtmlDeviceFilterUtils, String buttonName) {
        webDriverHelper.findElement(HtmlDeviceFilterUtils.getXPath()+getButtonXPath(buttonName)).click();
    }

    public void clickOfClass(String buttonClass) {
        webDriverHelper.findElement(getButtonXPathOfClass(buttonClass)).click();
    }

    public void assertDisabled(String buttonName) {
        assertFalse(webDriverHelper.findElement(getButtonXPath(buttonName)).isEnabled());
    }

    public void assertDisplayed(String buttonName) {
        assertTrue(webDriverHelper.findElement(getButtonXPath(buttonName)).isDisplayed());
    }

    public void assertDisplayed(HtmlTableUtils htmlTableUtils, String rowName, String headerName, String buttonName) {
        assertTrue(htmlTableUtils.getElement(rowName, headerName, getButtonXPath(buttonName)).isDisplayed());
    }

    public void assertNotDisplayed(String buttonName) {
        assertFalse(webDriverHelper.findElement(getButtonXPath(buttonName)).isDisplayed());
    }

    public void assertNotDisplayed(HtmlTableUtils htmlTableUtils, String rowName, String headerName, String buttonName) {
        assertFalse(htmlTableUtils.getElement(rowName, headerName, getButtonXPath(buttonName)).isDisplayed());
    }

    public void waitForButtonToBeClickable(String buttonName, int secondsToWait) throws TestTimeoutException {
        webDriverHelper.waitForElementToBeClickable(getButtonXPath(buttonName), secondsToWait);
    }

    public void waitAndClick(String buttonName, int secondsToWait) throws TestTimeoutException {
        waitForButtonToBeClickable(buttonName, secondsToWait);
        click(buttonName);
    }

    private String getButtonXPath(String buttonName) {
        return "//button[contains(.,'"+buttonName+"')]";
    }

    private String getButtonXPathOfClass(String iconClass) {
        return "//button//span[contains(@class,'" + iconClass + "')]/..";
    }
}
