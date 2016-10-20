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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ssl.cukes.SeleniumWebDriverHelper;
import com.ssl.cukes.SeleniumWebDriverHelper.WebElementHelper;
import java.util.Arrays;
import java.util.List;

@Component
public class HtmlModalUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlModalUtils.class);

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_PARTIAL_SUCCESS = "PARTIAL_SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";
    public static final String STATUS_UPDATED = "UPDATED";

    @Autowired private SeleniumWebDriverHelper webDriverHelper;

    public void assertTitleText(String titleText) {
        String xPath = "//div[@class = 'modal-body']//tr[contains(.,'"+titleText+"')]";
        assertTrue(webDriverHelper.findElement(xPath).isDisplayed());
    }

    public void set(String rowName, String value) {
        String xPath = "//div[@class = 'modal-body']//tr[contains(.,'"+rowName+"')]//input";
        SeleniumWebDriverHelper.WebElementHelper element = webDriverHelper.findElement(xPath);
        element.click().clear().sendKeys(value);
    }

    public void set(String rowName, int value) {
        set(rowName, String.valueOf(value));
    }

    public String getFooterXPath() {
        return "//div[@class = 'modal-footer']";
    }

    public String getContentXPath() {
        return "//div[@class = 'modal-content']";
    }

    public String getButtonXPath(String buttonName) {
        return "//button[contains(.,'"+buttonName+"')]";
    }

    public void assertTextIsDisplayed(String titleText) {
        assertTrue(isTextDisplayed(titleText));
    }
    
    private boolean isTextDisplayed(String titleText) {
        String xPath = "//div[@class = 'modal-body']//div[text()[contains(.,'" + titleText + "')]]";
        return webDriverHelper.findElement(xPath).isDisplayed();
    }

    public void clickFooterButton(String buttonName) {
        String xPathOfButtonInModal = getFooterXPath() + getButtonXPath(buttonName);
        webDriverHelper.findClickableElement(xPathOfButtonInModal).click();
    }

    public boolean isFileLessThanOneMinOld(File file) {
        Instant lastModified = Instant.ofEpochMilli(file.lastModified());
        return Instant.now().minus(1, ChronoUnit.MINUTES).isBefore(lastModified);
    }

    public void waitForStatus(List<String> validStatus) throws InterruptedException {
        long startTime = System.nanoTime();
        long secondsRunning = 0L;
        long secondsToLogWarning = 5L;
        long secondsToFail = 10L;
        while (secondsRunning < secondsToFail) {
            if (checkStatusText(validStatus)) {
                return;
            }
            Thread.sleep(250);
            secondsRunning = TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
            if (secondsRunning > secondsToLogWarning) {
                LOGGER.debug("WARNING: waiting too much {} seconds for device respond ", secondsRunning);
            }
        }
        fail("waited too much (" + secondsRunning + " seconds) for " + validStatus);
    }
    
    public void waitForStatus(String text) throws InterruptedException {
        waitForStatus(Arrays.asList(text));
    }

    public void clickOnButton (String text) {
        String xPath = "//div[@class = 'modal-body']//..//button[contains(.,'" + text + "')]";
        webDriverHelper.findClickableElement(xPath).click();
    }
    
    private boolean checkStatusText(List<String> validStatus) {
        String xPath = "//div[contains(@class,'modal-body')]//div[.//label[contains(text(),'Status:')]]";
        try {
            WebElementHelper elementWithStatusLabel = webDriverHelper.findElement(xPath);
            String elementText = elementWithStatusLabel.getText();
            for( String status: validStatus) {
                if(elementText.contains(status)) {
                    return true;
                }
            }
            return false;
        } catch (NoSuchElementException | AssertionError ignored) {
            return false;
        }
    }
    
    public void assertLinkIsDisplayed(String linkText){        
        assertTrue(isLinkDisplayed(linkText));
    }
    
    private boolean isLinkDisplayed(String linkText){
        String xPath =getXpathForLink(linkText);
        return webDriverHelper.findClickableElement(xPath).isDisplayed();       
    }
    
    private String  getXpathForLink(String linkName){
        return "//div[@class = 'modal-body']//a[contains(.,'"+linkName+ "')]";
    }
}
