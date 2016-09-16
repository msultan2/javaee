package com.blogspot.javabrains;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class XmlServletAdv extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub		
		System.out.println("hello from servlet GET method");
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		response.getWriter().println("<b>hello</b> from servlet xml GET method on web page<br>");
		
		String UserName=request.getParameter("userName");
		response.getWriter().println("UserName="+UserName);
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub	
		response.setContentType("text/html");
		PrintWriter out=response.getWriter();
		System.out.println("hello from servlet POST method");
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		out.println("<b>hello</b> from servlet xml POST method on web page<br>");
		
		String userName=request.getParameter("userName");
		String fullName=request.getParameter("fullName");
		out.println("Hello from POST method "+userName + "! your full name is:"+fullName);
		
		String prof =request.getParameter("prof");
		out.println("you are a "+prof);
		
//		String location = request.getParameter("location");
		String[] location = request.getParameterValues("location");
		out.println("you are at "+location.length+" Places");
		
		for (int i=0;i<location.length;i++){
			out.println(location[i]);
		}
		
	}
}
