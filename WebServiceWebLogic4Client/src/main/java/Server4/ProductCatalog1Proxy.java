package Server4;

public class ProductCatalog1Proxy implements Server4.ProductCatalog1 {
  private String _endpoint = null;
  private Server4.ProductCatalog1 productCatalog1 = null;
  
  public ProductCatalog1Proxy() {
    _initProductCatalog1Proxy();
  }
  
  public ProductCatalog1Proxy(String endpoint) {
    _endpoint = endpoint;
    _initProductCatalog1Proxy();
  }
  
  private void _initProductCatalog1Proxy() {
    try {
      productCatalog1 = (new Server4.ProductCatalog1ServiceLocator()).getproductCatalog1();
      if (productCatalog1 != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)productCatalog1)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)productCatalog1)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (productCatalog1 != null)
      ((javax.xml.rpc.Stub)productCatalog1)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public Server4.ProductCatalog1 getProductCatalog1() {
    if (productCatalog1 == null)
      _initProductCatalog1Proxy();
    return productCatalog1;
  }
  
  public java.lang.String getProductCaregories() throws java.rmi.RemoteException{
    if (productCatalog1 == null)
      _initProductCatalog1Proxy();
    return productCatalog1.getProductCaregories();
  }
  
  
}