package org.sultan.Messenger.Resources;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.sultan.Messenger.Resources.Beans.MessageFilterBean;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
public class CommentsResource {
	
	@GET
//	@Path("/{commentId}")
	public String Test(){//@BeanParam MessageFilterBean filterBean){
		return "Test new Resource";//+filterBean.getCommentId();
	}
	
	@GET
	@Path("/{commentId}")
	public String Test2(@PathParam("commentId") long commentId, @PathParam("messageId") long messageId){
		return "Test new Resource, Comment Id:"+commentId+", Message Id:"+messageId;
	}
}
