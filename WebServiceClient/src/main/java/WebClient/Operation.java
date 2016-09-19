package WebClient;

import java.rmi.RemoteException;

import Server.MathOperations2Proxy;

public class Operation {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MathOperations2Proxy mathOperationProxy = new MathOperations2Proxy() ;
		
		try {
			System.out.println(mathOperationProxy.addSum2(2,3));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
