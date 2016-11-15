package com.ssl.bluetruth.receiver.v2.test;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

/**
 *
 * @author liban
 * TODO: Use HSQLDB for unit testing instead
 */
public class DbCon {

    public static void init() {
        try {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
        InitialContext ic = new InitialContext();
        ic.createSubcontext("java:");
        ic.createSubcontext("java:/comp");
        ic.createSubcontext("java:/comp/env");
        ic.createSubcontext("java:/comp/env/jdbc");

        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName("org.postgresql.Driver");
        bds.setUrl("jdbc:postgresql://192.168.0.124:5432/bluetruth");
        bds.setUsername("bluetruth");
        bds.setPassword("1324");

        ic.bind("java:/comp/env/jdbc/bluetruth", bds);
        } catch(NamingException e) {
            e.printStackTrace();
        }
    }
}
