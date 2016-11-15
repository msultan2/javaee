/*
  * 
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
  * 
 */
 
package com.ssl.rmas.test.rmas.steps;

import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.test.rmas.utils.LoginUtils;
import com.ssl.rmas.test.shared.utils.HtmlButtonUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;


public class ConnectWithOldKeysSteps {
    @Autowired
    private HtmlUtils htmlUtils;
    
    @Autowired
    private HtmlButtonUtils htmlButtonUtils;   
    
    @Autowired
    private LoginUtils loginUtils;

    private static final String SSH_KEYS_LINK = "SSH Key";
    private static final String GENERATE_BUTTON = "Generate new SSH key pair";
    private final String helpdeskUserNameValue = "sara@ssl.com";
    private final String passwordValue = "ssl1324";
    private static final String SUCCESS_GENERATE_NEW_SSH_KEY_PAIR_MESSAGE = "New SSH key pair generated successfully";
   
    @Given("^I have Generated a new key pair$")
    public void clickOnGenerateKeyButton() throws TestTimeoutException{
        loginUtils.logIn(helpdeskUserNameValue, passwordValue);
        htmlUtils.clickOnLink(SSH_KEYS_LINK);
        htmlButtonUtils.waitAndClick(GENERATE_BUTTON, 1);
        htmlUtils.assertAlertText(SUCCESS_GENERATE_NEW_SSH_KEY_PAIR_MESSAGE, 3);
    }   
}
