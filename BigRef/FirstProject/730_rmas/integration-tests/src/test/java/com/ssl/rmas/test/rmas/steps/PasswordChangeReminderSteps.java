/**
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

import static com.ssl.rmas.test.rmas.steps.DeviceListSteps.TITLE_SIZE_1;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

public class PasswordChangeReminderSteps {

    private static final String CHANGE_PASSWORD = "Change Password";

    @Autowired
    private HtmlUtils htmlUtils;

    @Then("^I should be asked to change my password$")
    public void askToChangePassword() throws Throwable {
        htmlUtils.assertAlertText(HtmlUtils.PASSWORD_HAS_EXPIRED_MESSAGE);
        htmlUtils.assertTitleText(CHANGE_PASSWORD, TITLE_SIZE_1);
        htmlUtils.closeAllAlerts();
    }

}
