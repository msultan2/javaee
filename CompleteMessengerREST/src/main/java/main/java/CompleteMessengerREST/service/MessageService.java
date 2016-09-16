package main.java.CompleteMessengerREST.service;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import main.java.CompleteMessengerREST.model.Message;

public class MessageService {
	
	private SessionFactory sessionFactory;
	private Session session;

	public MessageService(){
		sessionFactory=new Configuration().configure().buildSessionFactory();
		session=sessionFactory.openSession();
	}
	
	public List<Message> getAllMessages(){
		return session.createCriteria(Message.class).list();
	}
	
	public void addNewMessage(Message message){
		session.beginTransaction();
		
		Criteria criteria = session
			    .createCriteria(Message.class)
			    .setProjection(Projections.max("id"));
		Integer maxId = (Integer)criteria.uniqueResult();
		
		message.setId(maxId+1);
		message.setCreated(new Date());
		session.save(message);
		session.getTransaction().commit();
//		session.close();
	}

	public Message getMessage(int messageId) {
		session.beginTransaction();
		return (Message) session.get(Message.class, messageId);
	}

	public List<Message> getFilterMessages(int start, int end) {
		Criteria cr = session.createCriteria(Message.class);
		cr.add(Restrictions.between("id", start, end));	
		return  cr.list();
	}

	public void deleteMessage(int messageId) {
		// TODO Auto-generated method stub
		session.beginTransaction();
		Message message=(Message) session.get(Message.class, messageId);
		session.delete(message);
		session.getTransaction().commit();
//		session.flush();
	}
}
