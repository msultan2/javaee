/**
 * HelloService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package uk.ac.open.t320;

public interface HelloService extends javax.xml.rpc.Service {
    public java.lang.String getHelloAddress();

    public uk.ac.open.t320.Hello getHello() throws javax.xml.rpc.ServiceException;

    public uk.ac.open.t320.Hello getHello(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
