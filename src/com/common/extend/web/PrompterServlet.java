package com.common.extend.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PrompterServlet extends HttpServlet {

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String jpg = req.getParameter("jpg");
		if(jpg!=null) getCode(req, resp);
		else{
			String code = req.getParameter("code");
			resp.setContentType("text/html");
			PrintWriter out = resp.getWriter();
			out.println("<form method=\"post\" action=\""+req.getRequestURI()+"\">");
			out.println("<img src=\""+req.getRequestURI()+"?jpg\"/>");
			out.println("<input type=\"text\" name=\"code\" />");
			out.println("<input type=\"submit\" value=\"Submit\"/>");
			out.println("</form>");
			out.println(code==null?"":" "+code);
			out.flush();
		}
	}

	private void getCode(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		resp.setContentType("image/jpeg");
		String path = PromptDialog.path;
		if(path!=null && new File(path).exists()){
			InputStream is = new FileInputStream(path);
			OutputStream os = resp.getOutputStream();
			int c ;
			while( (c=is.read()) !=-1 ){
				os.write(c);
			}
			os.flush();
			os.close();
			is.close();
		}
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String code = req.getParameter("code");
		PromptDialog.ready = code;
		PromptDialog.path = null;
		this.doGet(req, resp);
	}
	
}
