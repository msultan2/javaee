/*
 *   THIS SOFTWARE IS PROVIDED BY COSTAIN INTEGRATED TECHNOLOGY SOLUTIONS
 *   LIMITED ``AS IS'', WITH NO WARRANTY, TERM OR CONDITION OF ANY KIND,
 *   EXPRESS OR IMPLIED, AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING,
 *   BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL COSTAIN
 *   INTEGRATED TECHNOLOGY SOLUTIONS LIMITED BE LIABLE FOR ANY LOSSES, CLAIMS
 *   OR DAMAGES OF WHATEVER NATURE, INCLUDING ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES EVEN IF ADVISED OF THE
 *   POSSIBILITY OF SUCH DAMAGES OR LOSSES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 *   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE.
 * 
 *   Copyright 2016 © Costain Integrated Technology Solutions Limited.
 *   All Rights Reserved.
 */
package com.ssl.rmas.test.shared.utils;

import com.ssl.cukes.SeleniumWebDriverHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HtmlHeaderUtils {

    public static final String ADMINISTRATION_LINK = "Administration";
    public static final String PENDING_USER_REGISTRATIONS_LINK = "User Registration Requests";
    
    @Autowired
    private SeleniumWebDriverHelper webDriverHelper;
    final private String headerXPath = "//div[contains(@class, 'header-container')]";
    
    public void clickInsideUserMenu(String linkName) {
        String userMenuXPath = headerXPath+"//ul[contains(@class, 'navbar-right')]/li/a";
        webDriverHelper.findClickableElement(userMenuXPath).click();
        String userSubMenuXPath = headerXPath+getSubLinkXPath(linkName);
        webDriverHelper.findClickableElement(userSubMenuXPath).click();
    }
    
    public void clickInSubMenu(String linkName, String subLinkName) {
        click(linkName);
        subClick(subLinkName);
    }
    
    public void click(String linkName) {
        webDriverHelper.findElement(headerXPath+getLinkXPath(linkName)).click();
    }

    public void subClick(String subLinkName) {
        webDriverHelper.findElement(headerXPath+getSubLinkXPath(subLinkName)).click();
    }

    private String getLinkXPath(String linkName) {
        return "//ul[not (contains(@class, 'ng-hide'))]/li//a[text()='"+linkName+"']";
    }

    private String getSubLinkXPath(String linkName) {
        return "//ul[not (contains(@class, 'ng-hide'))]/li[contains(@class, 'dropdown open')]//a[text()='"+linkName+"']";
    }
}
