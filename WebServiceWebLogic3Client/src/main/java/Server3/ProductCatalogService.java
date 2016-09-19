/**
 * ProductCatalogService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package Server3;

public interface ProductCatalogService extends javax.xml.rpc.Service {
    public java.lang.String getproductCatalogAddress();

    public Server3.ProductCatalog getproductCatalog() throws javax.xml.rpc.ServiceException;

    public Server3.ProductCatalog getproductCatalog(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
