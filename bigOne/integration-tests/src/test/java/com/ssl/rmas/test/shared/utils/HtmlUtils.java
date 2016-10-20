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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.ssl.cukes.SeleniumWebDriverHelper;
import com.ssl.cukes.SeleniumWebDriverHelper.WebElementHelper;
import com.ssl.cukes.TestTimeoutException;

/**
 * Common html util class
 *
 */
@Component
public class HtmlUtils {

    @Autowired
    private SeleniumWebDriverHelper webDriverHelper;

    public static final String DOWNLOAD_STATIC_DATA_BUTTON = "Refresh device data";
    public static final String WELCOME_TO_RMAS_TITLE = "Welcome to RMAS";
    public static final String NEXT_BUTTON = "Next";
    public static final String USER_REGISTRATION_LINK = "Sign Up";
    public static final String VIEW_PROFILE_LINK = "View Profile";
    public static final String USER_REGISTRATION_SUBMIT_BUTTON = "Submit";
    public static final String USER_REGISTRATION_TERMS_AND_CONDITIONS_LINK = "Terms of Use Agreement";
    public static final String LOGIN_PAGE_TITLE = "Login";
    public static final String LOGOUT_LINK = "Logout";
    public static final String SUCCESS_USER_REGISTRATION_MESSAGE = "User registration successful. An email will be sent to the user once the access request has been approved or rejected.";
    public static final String UPDATE_SSH_KEY_BUTTON = "Update key";
    public static final String RESET_DEVICE_BUTTON = "Reset device";
    public static final String REMOVE_OLD_SSH_KEYS_BUTTON = "Remove old keys";
    public static final String VERIFY_BUTTON = "Verify firmware";
    public static final String DOWNLOAD_LOGS_BUTTON = "Download logs";
    public static final String DEFAULT_RMAS_BASE_URL = "http://localhost:8080/";
    public static final String DEFAULT_RAIT_BASE_URL = "http://localhost:8180/";
    public static final String BASE_URL = "#";
    public static final String DEVICES_LINK = "Devices";
    public static final String ADD_DEVICE_LINK = "Add device";
    public static final String SUCCESS_DEVICE_ENROLMENT_MESSAGE = "Device saved successfully";
    public static final String UNSUCCESS_DEVICE_ENROLMENT_MESSAGE = "Failed to save the device, a device with this IP address already exists";
    public static final String PASSWORD_HAS_EXPIRED_MESSAGE = "Your password has expired, please change it before continuing";

    public void assertTitleText(String titleText) {
        assertTitleText(titleText, "2");
    }

    public void assertTitleText(String titleText, String titleSize) {
        assertTrue(webDriverHelper.findElement("//h" + titleSize + "[text()='" + titleText + "']").isDisplayed());
    }

    public void assertLabelText(String titleText) {
        assertTrue(webDriverHelper.findElement("//label[text()='" + titleText + "']").isDisplayed());
    }

    public void assertPanelHeading(String titleText) {
        assertTrue(webDriverHelper.findElement(getPanelHeadingXPath(titleText)).isDisplayed());
    }

    public void assertAlertText(String titleText) {
        assertTrue(webDriverHelper.findElement(getXpath(titleText)).isDisplayed());
    }

    public void assertAlertText(String titleText, int secondsToWait) throws TestTimeoutException {
        webDriverHelper.waitForElementToBeDisplayed(getXpath(titleText), secondsToWait);
        assertAlertText(titleText);
    }

    public void assertAlertType(String type) {
        assertTrue(webDriverHelper.findElement("//div[contains(@class,'alert-" + type + "')]").isDisplayed());
    }

    public void closeAllAlerts() {
        webDriverHelper.findElements("//div[contains(@class,'alert')]//button").stream().forEach((elementHelper) -> {
            elementHelper.click();
        });
    }

    public void goToRaitHomePage() {
        goToRaitUrl(BASE_URL);
    }

    public void goToRmasHomePage() {
        goToRmasUrl(BASE_URL);
    }

    public void refreshPage() {
        webDriverHelper.refresh();
    }

    public void clickSave() {
        webDriverHelper.findElement("//button[contains(.,'Save')]").click();
    }

    public void clickOnLink(String linkName) {
        webDriverHelper.findClickableElement(getLinkXPath(linkName)).click();
    }

    public void clickOnLink(String linkName, boolean textInsideSpan) {
        webDriverHelper.findClickableElement("//a" + ((textInsideSpan) ? "/span" : "") + "[text()='" + linkName + "']").click();
    }

    public void clickOnLink(HtmlTableUtils htmlTableUtils, String rowName, String headerName, String linkName) {
        htmlTableUtils.getElement(rowName, headerName, getLinkXPath(linkName)).click();
    }

    private String getLinkXPath(String linkName) {
        return "//a[text()='" + linkName + "']";
    }

    public SeleniumWebDriverHelper.WebElementHelper getClickableElement(String linkName) {
        return webDriverHelper.findClickableElement(getLinkXPath(linkName));
    }

    public void clickOnButton(String text) {
        webDriverHelper.findClickableElement("//button[contains(.,'" + text + "')]").click();
    }

    public void assertOkResponseFromCurrentUrl() throws URISyntaxException, IOException {
        Assert.assertThat(webDriverHelper.getURLResponseCode(webDriverHelper.getCurrentUrl()), is(equalTo(HttpStatus.OK)));
    }

    public void goToTab(int tabNumber) {
        List<String> tabs = new ArrayList<>(webDriverHelper.getAllWindowHandles());
        webDriverHelper.switchTo().window(tabs.get(tabNumber));
    }

    public void closeCurrentTab() {
        webDriverHelper.closeCurrentTab();
    }

    public void assertDivText(String text) {
        webDriverHelper.findElement("//div[text()='" + text + "']");
    }

    public void assertSpanText(String text) {
        webDriverHelper.findElement("//span[text()='" + text + "']");
    }

    public void assertParagraphContent(String text) {
        webDriverHelper.findElement("//p[text()='" + text + "']");
    }

    public String getParagraphContent() {
        WebElementHelper paragraphElement = webDriverHelper.findElement("//p");
        return paragraphElement.getText();
    }

    public String findParagraphContent(String textToFind) {
        WebElementHelper paragraphElement = webDriverHelper.findElement("//p[contains(text(), '" + textToFind + "')]");
        return paragraphElement.getText();
    }

    public void assertParagraphLength(int length) {
        WebElementHelper publicKeyElement = webDriverHelper.findElement("//p");
        assertTrue(publicKeyElement.getText().length() == length);
    }

    public void assertParagraphNotContent(String text) throws TestTimeoutException {
        webDriverHelper.waitForElementCount("//p[text()='" + text + "']", 0, 5);
    }

    public LocalDate getPanelHeadingLocalDate(String titleText) {
        return LocalDate.parse(webDriverHelper.findElement(getPanelHeadingXPath(titleText) + "//span").getText());
    }

    private String getPanelHeadingXPath(String titleText) {
        return "//*[contains(concat(' ',normalize-space(@class),' '),' panel-heading ') and text()='" + titleText + "']";
    }

    public void assertLinkNotPresent(String linkName) {
        assertFalse(webDriverHelper.findElement(getLinkXPath(linkName)).isDisplayed());
    }

    public void goToRaitUrl(String raitUrl) {
        webDriverHelper.loadUrl(System.getProperty("cucumber.rait.url", DEFAULT_RAIT_BASE_URL).concat(raitUrl));
    }

    public void goToRmasUrl(String rmasUrl) {
        webDriverHelper.loadUrl(System.getProperty("cucumber.rmas.url", DEFAULT_RMAS_BASE_URL).concat(rmasUrl));
    }

    public String getSpanNextToLabel(String label) {
        return webDriverHelper.findElement("//label[contains(.,'" + label + "')]/../span").getText();
    }

    public void waitForTitleText(String titleText, String titleSize, int secondsToWait) throws TestTimeoutException {
        webDriverHelper.waitForElementToBeDisplayed("//h" + titleSize + "[text()='" + titleText + "']", secondsToWait);
    }

    private String getXpath(String titleText) {
        return "//span[contains(., '" + titleText + "')]";
    }

}
