/**
 * ProductCatalog1ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package Server4;

public class ProductCatalog1ServiceLocator extends org.apache.axis.client.Service implements Server4.ProductCatalog1Service {

    public ProductCatalog1ServiceLocator() {
    }


    public ProductCatalog1ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ProductCatalog1ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for productCatalog1
    private java.lang.String productCatalog1_address = "http://localhost:13265/WebServiceWebLogic4/services/productCatalog1";

    public java.lang.String getproductCatalog1Address() {
        return productCatalog1_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String productCatalog1WSDDServiceName = "productCatalog1";

    public java.lang.String getproductCatalog1WSDDServiceName() {
        return productCatalog1WSDDServiceName;
    }

    public void setproductCatalog1WSDDServiceName(java.lang.String name) {
        productCatalog1WSDDServiceName = name;
    }

    public Server4.ProductCatalog1 getproductCatalog1() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(productCatalog1_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getproductCatalog1(endpoint);
    }

    public Server4.ProductCatalog1 getproductCatalog1(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            Server4.ProductCatalog1SoapBindingStub _stub = new Server4.ProductCatalog1SoapBindingStub(portAddress, this);
            _stub.setPortName(getproductCatalog1WSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setproductCatalog1EndpointAddress(java.lang.String address) {
        productCatalog1_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (Server4.ProductCatalog1.class.isAssignableFrom(serviceEndpointInterface)) {
                Server4.ProductCatalog1SoapBindingStub _stub = new Server4.ProductCatalog1SoapBindingStub(new java.net.URL(productCatalog1_address), this);
                _stub.setPortName(getproductCatalog1WSDDServiceName());
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
        if ("productCatalog1".equals(inputPortName)) {
            return getproductCatalog1();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://Server4", "productCatalog1Service");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://Server4", "productCatalog1"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("productCatalog1".equals(portName)) {
            setproductCatalog1EndpointAddress(address);
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
