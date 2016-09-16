package org.sultan.Messenger.Resources;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

//import org.sultan.Messenger.Resources.Beans.MessageFilterBean;
import org.sultan.Messenger.model.Message;
import org.sultan.Messenger.service.MessageService;

@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)		// to be applied for all methods
//@Produces(MediaType.TEXT_PLAIN)		//response type
public class MessageResource {
	
	MessageService messageService=new MessageService();
	
	@GET
//	public List<Message> getMessages(@QueryParam("year") int year,
//										@QueryParam("start") int start,
//										@QueryParam("size") int size){
//		//http://localhost:8080/Messenger/webapi/messages?year=2016
//		//http://localhost:8080/Messenger/webapi/messages?start=2&size=1
//		if(year>0) return messageService.getAllMessagesPerYear(year);
//		if(start>0 && size>0) return messageService.getAllMessagesPaginated(start, size); 
//			return messageService.getAllMessages();				
//	}
	
//	public List<Message> getMessages(@BeanParam MessageFilterBean filterBean){
//
//		//http://localhost:8080/Messenger/webapi/messages?year=2016
//		//http://localhost:8080/Messenger/webapi/messages?start=2&size=1
//		if(filterBean.getYear()>0) return messageService.getAllMessagesPerYear(filterBean.getYear());
//		if(filterBean.getStart()>0 && filterBean.getSize()>0) return messageService.getAllMessagesPaginated(filterBean.getStart(), filterBean.getSize()); 
//		return messageService.getAllMessages();				
//		}

	public String getMessages(){
//		return messageService.getAllMessages();
		return "XX";
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)		//response type
	public Message updateMessage(Message updateMessage){
		return messageService.updateMessage(updateMessage);
	}
	
	
	@GET
	@Path("/{messageId}")
	public Message getMessage(@PathParam("messageId") long messageid2){
		return messageService.getMessage(messageid2);
	}
	
//	@POST 
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Message addMessage(Message newMessage){
//		return messageService.addMessage(newMessage);
//	}
	
	@POST //changinf status code 
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addMessage(Message newMessage, @Context UriInfo uriInfo) throws URISyntaxException{
		Message retMessage=messageService.addMessage(newMessage);
//		return Response.status(Status.CREATED)
//				.entity(retMessage)
//				.build();
//			return Response.created(new URI("/messenger/webapi/messages/"+retMessage.getId()))
			return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(retMessage.getId())).build())
					.entity(retMessage)
					.build();
	}

	
	@DELETE
	@Path("/{messageId}")
	public String removeMessage(@PathParam("messageId") long id){
		messageService.removeMessage(id);
		return "Deleted";
	}
	
//	@GET  removed to handover everything to the subresource
	@Path("/{messageId}/comments")
	@Produces(MediaType.TEXT_PLAIN)		// to be applied for all methods
	public CommentsResource test(){//@BeanParam MessageFilterBean filterBean){
		return new CommentsResource();
	}
	
	
	
}
