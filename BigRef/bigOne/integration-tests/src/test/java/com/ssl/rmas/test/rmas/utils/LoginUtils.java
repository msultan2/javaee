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
 */
package com.ssl.rmas.test.rmas.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.io.BaseEncoding;
import com.ssl.cukes.TestTimeoutException;
import com.ssl.rmas.security.TotpAuthenticator;
import com.ssl.rmas.test.rmas.steps.DeviceListSteps;
import com.ssl.rmas.test.shared.utils.HtmlButtonUtils;
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlHeaderUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;

import cucumber.api.java.Before;

public class LoginUtils {

    private static final String USER_NAME_FIELD = "Username";
    private static final String PASSWORD_FIELD = "Password";
    private static final String LOG_IN_BUTTON = "Log In";
    private static final String LOG_OUT_LINK = "Log Out";
    public static final String NORMAL_USER_NAME ="Sergio";
    public static final String HE_APPROVER_USER_NAME ="Roberto";
    public static final String HELPDESK_USER_NAME ="Sara";

    private final String TWO_FA_FIELD = "Two factor authentication code";

    private TotpAuthenticator authenticator = new TotpAuthenticator();
    private Clock clock;

    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlFormUtils htmlFormUtils;
    @Autowired
    private HtmlHeaderUtils htmlHeaderUtils;
    @Autowired
    private HtmlButtonUtils htmlButtonUtils;

    @Autowired
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void logIn(String user, String password) throws TestTimeoutException {
        enterLoginDetailsAndClickLogin(user, password);
        htmlUtils.waitForTitleText(DeviceListSteps.WELCOME_TO_RMAS_TITLE, "2", 20);
    }

    public void logIn(String user, String password, String secret) throws TestTimeoutException, InvalidKeyException, NoSuchAlgorithmException {
        enterLoginDetailsAndClickLogin(user, password);
        htmlFormUtils.waitForFormElement(TWO_FA_FIELD, 5);
        String token = generateCodeFromSecret(secret);
        htmlFormUtils.set(TWO_FA_FIELD, token);
        htmlButtonUtils.waitAndClick(LOG_IN_BUTTON, 1);
        htmlUtils.waitForTitleText(DeviceListSteps.WELCOME_TO_RMAS_TITLE, "2", 20);
    }

    public void logInAndRedirectedTo(String user, String password, String pageTitle) throws TestTimeoutException {
        enterLoginDetailsAndClickLogin(user, password);
        htmlUtils.waitForTitleText(pageTitle, "1", 20);
    }

    public String generateCodeFromSecret(String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        return Long.toString(authenticator.getCode(BaseEncoding.base32().decode(secret), clock.millis() / 1000 / 30));
    }

    public void enterLoginDetailsAndClickLogin(String user, String password) throws TestTimeoutException {        htmlUtils.goToRmasHomePage();
        htmlUtils.waitForTitleText("Login", "1", 20);
        htmlFormUtils.set(USER_NAME_FIELD, user);
        htmlFormUtils.set(PASSWORD_FIELD, password);
        htmlButtonUtils.waitAndClick(LOG_IN_BUTTON, 1);
    }

    public void waitFor2faField() throws TestTimeoutException {
        htmlFormUtils.waitForFormElement(TWO_FA_FIELD, 2);
    }

    @Before
    public void tryToLogOut() {
        try {
            htmlHeaderUtils.clickInsideUserMenu(LOG_OUT_LINK);
        } catch(AssertionError e) {}
    }

    public void enter2faCodeAndClickLogin(String code) {
        htmlFormUtils.set(TWO_FA_FIELD, code);
        htmlButtonUtils.click("Log In");
    }

    public void generateAndenter2faCodeAndClickLogin(String secret) throws InvalidKeyException, NoSuchAlgorithmException {
        String token = generateCodeFromSecret(secret);
        htmlFormUtils.set(TWO_FA_FIELD, token);
        htmlButtonUtils.click("Log In");
    }
}
