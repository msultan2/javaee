/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ssl.bluetruth.receiver.v2.test;

import java.util.Date;

import com.ssl.bluetruth.receiver.v2.GenericParsable;
import com.ssl.bluetruth.receiver.v2.misc.annotations.Hex;
import com.ssl.bluetruth.receiver.v2.misc.annotations.Variable;

/**
 *
 * @author Liban Abdulkadir
 */
  public class Testing extends GenericParsable {
        public @Variable(index=0) int id;
        public @Variable(index=1) Date datetime;
        public @Hex @Variable(index=2) int hex_int;
        public Testing() {}
 
    }