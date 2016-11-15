/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ssl.bluetruth.receiver.v2.entities;

import java.util.Date;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Liban Abdulkadir
 */
public class Fault {

    public Fault() {
    }
    public int fn; // Fault Number
    public int status;
    public Date time;

    public static Fault deserialise(String a) {
        String[] parts = a.split(":");
        Fault bd = new Fault();
        bd.fn = Integer.parseInt(parts[0]);
        bd.time = new Date(Integer.valueOf(parts[1], 16) * 1000L);
        bd.status = Integer.parseInt(parts[2]);
        return bd;
    }

    @Override
    public String toString() {
        return StringUtils.join(new Object[]{fn,
            Integer.toHexString((int) (time.getTime() / 1000L)), status
        }, ':');
    }
    
    
}
