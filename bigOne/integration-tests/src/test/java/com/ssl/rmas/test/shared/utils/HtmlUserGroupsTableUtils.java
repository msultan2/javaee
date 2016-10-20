/*
 * THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 * LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 * EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 * INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 * OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 * 
 * Copyright 2016 © Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 */
package com.ssl.rmas.test.shared.utils;

import com.ssl.cukes.SeleniumWebDriverHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.junit.Assert.assertEquals;

@Component
public class HtmlUserGroupsTableUtils extends HtmlTableUtils {

    @Autowired
    private SeleniumWebDriverHelper webDriverHelper;

    @Autowired
    private HtmlFormUtils htmlFormUtils;

    @Override
    protected String getTableXPath() {
        return "//table[@class='table']";
    }

    public void setInTable(String key, String value) {
        webDriverHelper.findElement("//table" + htmlFormUtils.getXPath(key)).click().clear().sendKeys(String.valueOf(value));
    }

    public void setSelectInTable(String key, String optionName) {
        webDriverHelper.findElement("//table" + htmlFormUtils.getSelectXPath(key, optionName)).click();
    }

    public void assertInputInTable(String label, String value) {
        assertEquals(value, getElementInTable(label).getAttribute("value"));
    }

    public void assertSelectInTable(String label, String value) {
        assertEquals("string:" + value, getElementInTable(label, HtmlFormUtils.SELECT_TYPE).getAttribute("value"));
    }

    private SeleniumWebDriverHelper.WebElementHelper getElementInTable(String label) {
        return webDriverHelper.findElement("//table" + htmlFormUtils.getXPath(label));
    }

    private SeleniumWebDriverHelper.WebElementHelper getElementInTable(String label, String type) {
        return webDriverHelper.findElement("//table" + htmlFormUtils.getXPath(label, type));
    }

    public void clickOnButtonInTable(String text) {
        webDriverHelper.findClickableElement("//table//button[contains(.,'" + text + "')]").click();
    }
}
