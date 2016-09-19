package client;

import java.rmi.RemoteException;

import uk.ac.open.t320.HelloProxy;

public class TestClient {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HelloProxy helloProxy =new HelloProxy();
		helloProxy.setEndpoint("http://localhost:13265/HelloEsraa/services/Hello");
		try {
			String result = helloProxy.helloName("Test");
			System.out.println(result);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
