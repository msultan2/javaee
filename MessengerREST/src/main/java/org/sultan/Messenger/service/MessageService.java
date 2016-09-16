package org.sultan.Messenger.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.sultan.Messenger.dataBase.DatabaseClass;
import org.sultan.Messenger.model.Message;

public class MessageService {

	private Map<Long, Message> messages= DatabaseClass.getMessages();

	public MessageService(){
		messages.put(1L,new Message(1,"Hellow World","Sultan"));
		messages.put(2L,new Message(2,"Hellow Jersy","Sultan"));
	}
	
	public List<Message> getAllMessages(){
		return new ArrayList<Message>(messages.values());
	}
	
	
	public Message getMessage(Long id){
		return messages.get(id);
	}
	
	public Message updateMessage(Message newMessage){
		messages.put(newMessage.getId(), newMessage);
		return newMessage;
	}
	
	public void removeMessage(Long id){
//		messages.put(id, null);
		messages.remove(id);
	}
	
	public Message addMessage(Message newMessage){
		newMessage.setId(messages.size()+1);
		messages.put(newMessage.getId(), newMessage);
		return newMessage;
	}
	public List<Message> getAllMessagesPerYear(int year){
		Calendar cal = Calendar.getInstance();
		List<Message> messagesYear=new ArrayList();
		for (Message message : messages.values()){
			cal.setTime(message.getCreated());
			if(cal.get(Calendar.YEAR)==year){
				messagesYear.add(message);
			}
		}
		return messagesYear;
	}
	
	public List<Message> getAllMessagesPaginated(int start,int len){
		ArrayList<Message> paginatedMessages=new ArrayList(messages.values());
		return paginatedMessages.subList(start,start+ len);
	}
}
