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

package com.ssl.rmas.test.rmas.steps;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.LoginUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;


public class LockUnusedUserAccountSteps {

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private LoginUtils loginUtils;

    private static final String TITLE_SIZE_1 = "1";
    private String lockedAccountUserName = "rocio@ssl.com";
    private String lockedAccountCorrectPassword = "ssl1324";
    private String lockedAccountIncorrectPassword = "ssl";
    private String lockedAccountErrorMessage = "Your RMAS account has been locked, please contact the RMAS service desk";

    @When("^I enter details of an account that has not been used for a long time$")
    public void enterVaildCredentials() throws TestTimeoutException{        
        loginUtils.enterLoginDetailsAndClickLogin(lockedAccountUserName, lockedAccountCorrectPassword);
    }

    @When("^I enter invalid login details$")
    public void enterInvaildCredentials() throws TestTimeoutException{
        loginUtils.enterLoginDetailsAndClickLogin(lockedAccountUserName, lockedAccountIncorrectPassword);
    }

    @Then("^I should see a message telling me that my account has been locked$")
    public void assertAccountLockedMessage(){        
        htmlUtils.assertAlertText(lockedAccountErrorMessage);
    }

    @And("^I should not be able to log in to the RMAS system any more$")
    public void checkUserCannotLogin() throws TestTimeoutException{
        loginUtils.enterLoginDetailsAndClickLogin(lockedAccountUserName, lockedAccountCorrectPassword);
        htmlUtils.assertTitleText(HtmlUtils.LOGIN_PAGE_TITLE, TITLE_SIZE_1);
    }
}
