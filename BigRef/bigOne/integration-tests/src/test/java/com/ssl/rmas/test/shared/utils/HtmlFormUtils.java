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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ssl.cukes.SeleniumWebDriverHelper;
import com.ssl.cukes.TestTimeoutException;

@Component
public class HtmlFormUtils {

    public static final String TEXTAREA_TYPE = "textarea";
    public static final String SELECT_TYPE = "select";

    public static final String DEVICE_IP = "Device IP";
    public static final String PRIVATE_KEY = "Private key";
    public static final String BANDWIDTH_LIMIT = "Bandwidth limit (Kbps)";

    public static final String NAME = "Name";
    public static final String CONTACT_ADDRESS = "Address";
    public static final String EMAIL = "Email";
    public static final String PHONE = "Telephone number";
    public static final String MOBILE = "Mobile number";
    public static final String EMPLOYMENT_ORGANIZATION = "Organisation";
    public static final String MAINTENANCE_CONTRACT = "Maintenance region";
    public static final String RCC = "RCC";
    public static final String PROJECT_SPONSOR = "Project sponsor";
    public static final String REASON_ACCESS_REQUIRED = "Reason for access request";
    public static final String ACCESS_REQUIRED = "Details of access required";
    public static final String TERMS_AND_CONDITIONS = "I confirm that I have read and accepted the Terms of Use Agreement";
    public static final String REASON_ACCESS_REJECTED_APPROVED = "Reject/Approve reason";
    public static final String USER_GROUP = "User group";

    @Autowired
    private SeleniumWebDriverHelper webDriverHelper;

    public void clear(String key) {
        webDriverHelper.findElement(getXPath(key)).click().clear();
    }

    public void set(String key, String value) {
        webDriverHelper.findElement(getXPath(key)).click().clear().sendKeys(String.valueOf(value));
    }

    public void setSelect(String key, String optionName) {
        webDriverHelper.findElement(getSelectXPath(key, optionName)).click();
    }

    public void setSelect(int row, String key, String optionName) {
        List<SeleniumWebDriverHelper.WebElementHelper> elements = webDriverHelper.findElements(getSelectXPath(key, optionName));
        SeleniumWebDriverHelper.WebElementHelper element = elements.get(row);
        element.click();
    }

    public void set(String key, String type, String value) {
        webDriverHelper.findElement(getXPath(key, type)).click().clear().sendKeys(String.valueOf(value));
    }

    public void setFile(String key, String path) {
        webDriverHelper.findElement(getXPath(key)).sendKeys(String.valueOf(path));
    }

    public void assertInput(String label, String value){
        assertEquals(value, getElement(label).getAttribute("value"));
    }

    public void assertTextArea(String label, String value){
        assertEquals(value, getElement(label,TEXTAREA_TYPE).getAttribute("value"));
    }

    public void clickOnCheckbox(String key){
         webDriverHelper.findElement(getXPathFromSpan(key)).click();
    }

    private SeleniumWebDriverHelper.WebElementHelper getElement(String label){
        return webDriverHelper.findElement(getXPath(label));
    }

    private SeleniumWebDriverHelper.WebElementHelper getElement(String label,String type){
        return webDriverHelper.findElement(getXPath(label,type));
    }

    public String getXPath(String key) {
        return "//label[text()='" + key + "']/..//input";
    }

    public String getXPath(String key, String type) {
        return "//label[text()='" + key + "']/..//" + type;
    }

    private String getXPathFromSpan(String key) {
        return "//span[contains(.,'" + key + "' )]/..//input";
    }

    public String getSelectXPath(String key, String optionName) {
        return "//label[text()='" + key + "']/..//select/option[text()='" + optionName + "']";
    }

    public void assertSelect(String label, String value) {
        String selectXPath = getSelectXPath(label, value);
        SeleniumWebDriverHelper.WebElementHelper element = webDriverHelper.findElement(selectXPath);
        assertEquals(value, element.getText());
    }

    public void waitForFormElement(String labelText, int timeout) throws TestTimeoutException {
        webDriverHelper.waitForElementToBeDisplayed(getXPath(labelText), timeout);
    }

}
