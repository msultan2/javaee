package Server;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService 
public class productCatalog {

//	@WebMethod
	public String getProductCaregories(){
//		List<String> categories = new ArrayList<>();
//		categories.add("Books");
//		categories.add("Music");
//		categories.add("Movies");
		return "Sultan";
		
	}
}
