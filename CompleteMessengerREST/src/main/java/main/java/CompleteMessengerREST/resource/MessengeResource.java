package main.java.CompleteMessengerREST.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import main.java.CompleteMessengerREST.model.Message;
import main.java.CompleteMessengerREST.service.MessageService;

@Path("/messages")
@Consumes(MediaType.APPLICATION_XML)
@Produces(MediaType.APPLICATION_XML)
public class MessengeResource {
	
	private MessageService messageService =new MessageService();
	
//	@GET	
////	@Produces(MediaType.APPLICATION_JSON)
//	public List<Message> getAllMessages(){
//		return  messageService.getAllMessages();
//	}
	
	@DELETE
	@Path("/{messageId}")
	public String deleteMessage(@PathParam("messageId") int messageId){
		System.out.println("deleting message id"+messageId);
		messageService.deleteMessage(messageId);
		return "Message Deleted";
	}

	@GET
	public List<Message> getFilterMessages(@QueryParam("start") int start, @QueryParam("end") int end){
		if (start>0 && end>0){
			System.out.println("getting Msg between "+start +" and " + end);
			return messageService.getFilterMessages(start,end);
		}
		return  messageService.getAllMessages();
	}
	
	@GET	
	@Path("/{messageId}")
	public Message getOneMessages(@PathParam("messageId") int messageId){
		System.out.println("getting message ID:"+messageId);
		return  messageService.getMessage(messageId);
	}
	
	@PUT
	public Message addMessage(Message message){
		messageService.addNewMessage(message);
		System.out.println("Adding Msg");
		return message;
	}
}
