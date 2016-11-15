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

package com.ssl.rmas.test.rmas.utils;

import com.ssl.cukes.WebserviceConversationHelper;
import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import com.ssl.rmas.test.shared.utils.HtmlHeaderUtils;
import com.ssl.rmas.test.shared.utils.HtmlUtils;
import cucumber.api.Scenario;
import cucumber.api.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import static org.junit.Assert.fail;

public class UserUtils {
    
    @Autowired
    private HtmlUtils htmlUtils;
    @Autowired
    private HtmlFormUtils htmlFormUtils;
    @Autowired
    private WebserviceConversationHelper webserviceConvoHelper;
    private Scenario scenario;
    @Autowired
    private MongoUtils mongoUtils;
    @Autowired
    private HtmlHeaderUtils htmlHeaderUtils;
    
    private static final String CONTACT_ADDRESS = "London";
    private static final String PHONE = "1234 123456";
    private static final String MOBILE = "7574 023420";
    private static final String EMPLOYMENT_ORGANIZATION = "NA";
    private static final String MAINTENANCE_CONTRACT = "R1234";
    private static final String RCC = "South West";
    private static final String REASON_ACCESS_REQUIRED = "The reason";
    private static final String ACCESS_REQUIRED = "Level 3";

    @Before
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public void goToRegistrationPage() {
        htmlUtils.goToRmasHomePage();
        htmlUtils.clickOnLink(HtmlUtils.USER_REGISTRATION_LINK);
        waitForHEApproversForDebugIntention();
    }

    public void goToPendingRegistrationPage() {
        htmlUtils.goToRmasHomePage();
        htmlHeaderUtils.clickInSubMenu(HtmlHeaderUtils.ADMINISTRATION_LINK, HtmlHeaderUtils.PENDING_USER_REGISTRATIONS_LINK);
    }

    public void goToEnrolmentPage() {
         htmlUtils.goToRmasHomePage();
         htmlUtils.clickOnLink(HtmlUtils.DEVICES_LINK);
         htmlUtils.clickOnLink(HtmlUtils.ADD_DEVICE_LINK);
    }

    private void waitForHEApproversForDebugIntention() {
        long startTime = System.currentTimeMillis();
        boolean got200Result = false;
        while(!got200Result && (System.currentTimeMillis() - startTime) < 5000) {
            webserviceConvoHelper.doGetJSONService(System.getProperty("cucumber.rmas.url", "http://localhost:8080/") + "rmas-core/users/search/findAllHEApprovers");
            got200Result = webserviceConvoHelper.getResultsStatusCode().equals(HttpStatus.OK);
            if(!got200Result) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        if(!got200Result) {
            scenario.write("Timed out waiting for HE approvers list, response code was " + webserviceConvoHelper.getResultsStatusCode().toString());
            if(webserviceConvoHelper.getResultsBody()!=null) {
                scenario.write(webserviceConvoHelper.getResultsBody().toString());
            }
            webserviceConvoHelper.doGetJSONService(System.getProperty("cucumber.rmas.url", "http://localhost:8080/") + "rmas-core/beans");
            if(webserviceConvoHelper.getResultsBody()!=null) {
                scenario.write(webserviceConvoHelper.getResultsBody().toString());
            }
            webserviceConvoHelper.doGetJSONService(System.getProperty("cucumber.rmas.url", "http://localhost:8080/") + "rmas-core/profile");
            if(webserviceConvoHelper.getResultsBody()!=null) {
                scenario.write(webserviceConvoHelper.getResultsBody().toString());
            }
            webserviceConvoHelper.doGetJSONService(System.getProperty("cucumber.rmas.url", "http://localhost:8080/") + "rmas-core/profile/users");
            if(webserviceConvoHelper.getResultsBody()!=null) {
                scenario.write(webserviceConvoHelper.getResultsBody().toString());
            }
            scenario.write("Mongo collections in " + mongoUtils.getDBName() + " are : " + mongoUtils.getCollections());
            fail("Failed to get HE approvers list");
        }
    }

    public void register(String userName, String userEmail) {
        htmlFormUtils.set(HtmlFormUtils.NAME, userName);
        htmlFormUtils.set(HtmlFormUtils.CONTACT_ADDRESS, HtmlFormUtils.TEXTAREA_TYPE, CONTACT_ADDRESS);
        htmlFormUtils.set(HtmlFormUtils.EMAIL, userEmail);
        htmlFormUtils.set(HtmlFormUtils.PHONE, PHONE);
        htmlFormUtils.set(HtmlFormUtils.MOBILE, MOBILE);
        htmlFormUtils.set(HtmlFormUtils.EMPLOYMENT_ORGANIZATION, EMPLOYMENT_ORGANIZATION);
        htmlFormUtils.set(HtmlFormUtils.MAINTENANCE_CONTRACT, MAINTENANCE_CONTRACT);
        htmlFormUtils.setSelect(HtmlFormUtils.RCC, RCC);
        htmlFormUtils.setSelect(HtmlFormUtils.PROJECT_SPONSOR, LoginUtils.HE_APPROVER_USER_NAME);
        htmlFormUtils.set(HtmlFormUtils.REASON_ACCESS_REQUIRED, HtmlFormUtils.TEXTAREA_TYPE, REASON_ACCESS_REQUIRED);
        htmlFormUtils.set(HtmlFormUtils.ACCESS_REQUIRED, HtmlFormUtils.TEXTAREA_TYPE, ACCESS_REQUIRED);
        htmlFormUtils.clickOnCheckbox(HtmlFormUtils.TERMS_AND_CONDITIONS);
        htmlUtils.clickOnButton(HtmlUtils.USER_REGISTRATION_SUBMIT_BUTTON);
    }
    
    public void assertUserDetails(String userName, String userEmail) {
        htmlFormUtils.assertInput(HtmlFormUtils.NAME, userName);
        htmlFormUtils.assertInput(HtmlFormUtils.EMAIL, userEmail);
        htmlFormUtils.assertInput(HtmlFormUtils.EMPLOYMENT_ORGANIZATION, EMPLOYMENT_ORGANIZATION);
        htmlFormUtils.assertInput(HtmlFormUtils.MAINTENANCE_CONTRACT, MAINTENANCE_CONTRACT);
        htmlFormUtils.assertSelect(HtmlFormUtils.RCC, RCC);
        htmlFormUtils.assertTextArea(HtmlFormUtils.CONTACT_ADDRESS, CONTACT_ADDRESS);
        htmlFormUtils.assertInput(HtmlFormUtils.PHONE, PHONE);
        htmlFormUtils.assertInput(HtmlFormUtils.MOBILE, MOBILE);
        htmlFormUtils.assertTextArea(HtmlFormUtils.REASON_ACCESS_REQUIRED, REASON_ACCESS_REQUIRED);
        htmlFormUtils.assertTextArea(HtmlFormUtils.ACCESS_REQUIRED, ACCESS_REQUIRED);
    }
}
