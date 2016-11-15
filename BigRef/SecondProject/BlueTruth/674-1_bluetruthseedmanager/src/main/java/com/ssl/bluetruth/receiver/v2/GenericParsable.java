package com.ssl.bluetruth.receiver.v2;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.HashMap;

/**
 *
 * @author liban
 */
public abstract class GenericParsable {

    public HashMap<String, String> vars = new HashMap<>();
    public Integer rnd;
    public String data;
    public void varSet(String a, String b) {
        vars.put(a, b);
    }

    public String varGet(String a) {
        return vars.get(a);
    }

    public boolean varHas(String a) {
        return vars.containsKey(a);
    }
    
    public void postParse() {
        
    }
    
    public void onValidated() {
        
    }
}
