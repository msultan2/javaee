
package com.mycompany.java;
import java.util.Date;
import java.util.TimeZone;
public class Hello {
    public static void main(final String[] args) {
        TimeZone timeZoneLondon = TimeZone.getTimeZone("Europe/London");
        final long currentTimeMillis = System.currentTimeMillis();
        final long zonedTimeMillis = currentTimeMillis + timeZoneLondon.getOffset(currentTimeMillis);
        Date now = new Date(currentTimeMillis);
        Date zoneDate = new Date(zonedTimeMillis);
        System.out.println("Current Date:" + now);
        System.out.println("Current date Offset:" + timeZoneLondon.getOffset(currentTimeMillis));
        System.out.println("zoneDate Date:" + zoneDate);
    }
}