package org.sultan.Messenger.Resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/injectdemo")
@Produces(MediaType.TEXT_PLAIN)		// to be applied for all methods
@Consumes(MediaType.TEXT_PLAIN)		//response type
public class InjectDemoResource {
	
	@GET
	@Path("/annotations")
	public String getParamsUsingAnnotations(@MatrixParam("param") String matrixPatam,
											@HeaderParam("customHeaderValue") String header,
											@CookieParam("name") String Cookie){
		
//		@FormParam: used when submitting data from HTML form
		//http://localhost:8080/Messenger/webapi/injectdemo/annotations;param=value
		return "Matrix Param:" + matrixPatam +",Header value="+header + ", Cookie:"+Cookie;
	}
	
	@GET
	@Path("context")
	public String getParamsUsingContext(@Context UriInfo uriInfo, @Context HttpHeaders header){
		String cookies=header.getCookies().toString();
		String path= uriInfo.getAbsolutePath().toString();
		return "Path="+path+",Cookies:"+cookies ;
	}

}
