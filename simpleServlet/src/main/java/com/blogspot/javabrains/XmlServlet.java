package com.blogspot.javabrains;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class XmlServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub		
		System.out.println("hello from servlet GET method");
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		response.getWriter().print("<b>hello</b> from servlet xml GET method on web page");
	}
}
