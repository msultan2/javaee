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

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import static org.junit.Assert.fail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DateTimeUtils {
    
    @Autowired private Clock clock;
    
    public void assertDateIsTodayOrExceptionallyYesterday(LocalDate localDate) {
        LocalDate today = LocalDate.now(clock);
        if (!today.isEqual(localDate)) {
            LocalTime now = LocalTime.now(clock);
            if (now.getHour() == 0 && (now.getMinute() == 0)) {
                LocalDate yesterday = today.minus(1, ChronoUnit.DAYS);
                if (!yesterday.isEqual(localDate)) {
                    fail("Expected localDate should be "+today+" or "+yesterday+" in the first minute of the day. But was: "+localDate);
                }
            } else {
                fail("Expected localDate should be "+today+". But was: "+localDate);
            }
        }
    }

}
