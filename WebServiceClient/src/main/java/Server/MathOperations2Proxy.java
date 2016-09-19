package Server;

public class MathOperations2Proxy implements Server.MathOperations2 {
  private String _endpoint = null;
  private Server.MathOperations2 mathOperations2 = null;
  
  public MathOperations2Proxy() {
    _initMathOperations2Proxy();
  }
  
  public MathOperations2Proxy(String endpoint) {
    _endpoint = endpoint;
    _initMathOperations2Proxy();
  }
  
  private void _initMathOperations2Proxy() {
    try {
      mathOperations2 = (new Server.MathOperations2ServiceLocator()).getMathOperations2();
      if (mathOperations2 != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)mathOperations2)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)mathOperations2)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (mathOperations2 != null)
      ((javax.xml.rpc.Stub)mathOperations2)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public Server.MathOperations2 getMathOperations2() {
    if (mathOperations2 == null)
      _initMathOperations2Proxy();
    return mathOperations2;
  }
  
  public int addSum2(int x, int y) throws java.rmi.RemoteException{
    if (mathOperations2 == null)
      _initMathOperations2Proxy();
    return mathOperations2.addSum2(x, y);
  }
  
  
}