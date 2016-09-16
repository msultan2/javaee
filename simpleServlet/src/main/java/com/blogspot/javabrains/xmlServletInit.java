package com.blogspot.javabrains;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class SimpleServlet
 */
public class xmlServletInit extends HttpServlet {     
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub		
		System.out.println("hello from servlet GET method");
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		response.getWriter().print("<b>hello</b> from servlet GET method on web page");
		
		String userName=request.getParameter("userName");
		
		HttpSession session = request.getSession();
		ServletContext context=request.getServletContext();
		
		
		if (userName != "" && userName != null){
			session.setAttribute("savedUserName", userName);
			context.setAttribute("savedUserName", userName);
		}
		
		response.getWriter().println("<br>");
		response.getWriter().println("UserName="+userName);
		
		PrintWriter writer = response.getWriter();
		writer.println("<br>");
		writer.println("Request Parameter has user:"+userName);
		writer.println("<br>");
		writer.println("Session Parameter has user:"+(String) session.getAttribute("savedUserName"));
		writer.println("<br>");
		writer.println("Context Parameter has user:"+(String) context.getAttribute("savedUserName"));
		writer.println("<br>");
		writer.println("init Parameter has default user:"+(String) this.getServletConfig().getInitParameter("defaultName"));
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
