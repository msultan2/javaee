package com.ssl.bluetruth.receiver.v2.seed;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Liban Abdulkadir
 */
public class LCG {

    private int a, m;

    public LCG(int a, int m) {
        this.a = a;
        this.m = m;
    }

    public long next(long x) {
        return (((a * x) & 0xFFFFFFFFL) % m);
    }
    
    public static int mask(long x) {
        return (int)(x & 0xFFFFL);
    }
}
