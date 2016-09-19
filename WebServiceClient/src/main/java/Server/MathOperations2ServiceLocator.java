/**
 * MathOperations2ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package Server;

public class MathOperations2ServiceLocator extends org.apache.axis.client.Service implements Server.MathOperations2Service {

    public MathOperations2ServiceLocator() {
    }


    public MathOperations2ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public MathOperations2ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for MathOperations2
    private java.lang.String MathOperations2_address = "http://localhost:13265/WebServiceServer/services/MathOperations2";

    public java.lang.String getMathOperations2Address() {
        return MathOperations2_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String MathOperations2WSDDServiceName = "MathOperations2";

    public java.lang.String getMathOperations2WSDDServiceName() {
        return MathOperations2WSDDServiceName;
    }

    public void setMathOperations2WSDDServiceName(java.lang.String name) {
        MathOperations2WSDDServiceName = name;
    }

    public Server.MathOperations2 getMathOperations2() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(MathOperations2_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getMathOperations2(endpoint);
    }

    public Server.MathOperations2 getMathOperations2(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            Server.MathOperations2SoapBindingStub _stub = new Server.MathOperations2SoapBindingStub(portAddress, this);
            _stub.setPortName(getMathOperations2WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setMathOperations2EndpointAddress(java.lang.String address) {
        MathOperations2_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (Server.MathOperations2.class.isAssignableFrom(serviceEndpointInterface)) {
                Server.MathOperations2SoapBindingStub _stub = new Server.MathOperations2SoapBindingStub(new java.net.URL(MathOperations2_address), this);
                _stub.setPortName(getMathOperations2WSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("MathOperations2".equals(inputPortName)) {
            return getMathOperations2();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://Server", "MathOperations2Service");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://Server", "MathOperations2"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("MathOperations2".equals(portName)) {
            setMathOperations2EndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
