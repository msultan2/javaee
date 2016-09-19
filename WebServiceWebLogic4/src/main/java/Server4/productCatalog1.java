package Server4;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService (name="SultanWebService")
public class productCatalog1 {

//	@WebMethod
	public String getProductCaregories(){
//		List<String> categories = new ArrayList<>();
//		categories.add("Books");
//		categories.add("Music");
//		categories.add("Movies");
		return "Sultan";
		
	}
}
