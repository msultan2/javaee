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
package com.ssl.rmas.test.shared.utils;

import com.ssl.cukes.SeleniumWebDriverHelper;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HtmlDatePickerUtils {

    @Autowired private SeleniumWebDriverHelper webDriverHelper;

    public void select(HtmlModalUtils htmlModalUtils, String name, LocalDate date) {
        String day = String.valueOf(date.getDayOfMonth());
        String month = String.valueOf(date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        String year = String.valueOf(date.getYear()).substring(2);
        open(htmlModalUtils, name);
        zoomOut(htmlModalUtils, name);
        zoomOut(htmlModalUtils, name);
        zoomIn(htmlModalUtils, name, year);
        zoomIn(htmlModalUtils, name, month);
        zoomIn(htmlModalUtils, name, day);
    }

    private void open(HtmlModalUtils htmlModalUtils, String name) {
        String xPath = getDatePickerXPath(htmlModalUtils, name)+"//span//button";
        webDriverHelper.findElement(xPath).click();
    }

    private void zoomOut(HtmlModalUtils htmlModalUtils, String name){
        String xPath = getDatePickerXPath(htmlModalUtils, name)+"//table//button[contains(@role,'heading')]";
        webDriverHelper.findElement(xPath).click();
    }

    private void zoomIn(HtmlModalUtils htmlModalUtils, String name, String buttonName) {
        String xPath = getDatePickerXPath(htmlModalUtils, name)+"//table//button[contains(.,'"+buttonName+"')]";
        webDriverHelper.findElement(xPath).click();
    }

    private String getDatePickerXPath(HtmlModalUtils htmlModalUtils, String name) {
        return htmlModalUtils.getContentXPath()+"//label[text()='"+name+"']/..//date-picker";
    }
}
