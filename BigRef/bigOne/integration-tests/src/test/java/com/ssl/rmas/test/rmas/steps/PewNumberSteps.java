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
 * Copyright 2016 (C) Costain Integrated Technology Solutions Limited.
 * All Rights Reserved.
 * 
 */
package com.ssl.rmas.test.rmas.steps;

import com.ssl.rmas.test.shared.utils.HtmlFormUtils;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

public class PewNumberSteps {

    private static final String PEW_NUMBER = "PEW123456";
    private static final String PEW_NUMBER_FIELD = "PEW number";
    private static final String CONFIRM_DETAILS = "I confirm that the PEW number is correct and my contact details are accurate and up to date";

    @Autowired
    private HtmlFormUtils htmlFormUtils;

    @Given("^I have entered a correct PEW number$")
    public void i_have_entered_a_correct_PEW_number() throws Throwable {
        htmlFormUtils.set(PEW_NUMBER_FIELD, PEW_NUMBER);
    }

    @Given("^I have confirmed that PEW number and my user details are correct$")
    public void i_have_confirmed_my_user_details_are_correct() throws Throwable {
        htmlFormUtils.clickOnCheckbox(CONFIRM_DETAILS);;
    }

}
