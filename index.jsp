<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" %>
<%@ page language="java"%>
<%@ page import="oz.fetchcontent.datax.Datax" %>
<%@ page import="oz.fetchcontent.datax.Sqlmaker" %>
<%@ page import="oz.fetchcontent.datax.Newsdx" %>
<%@ page import="oz.fetchcontent.datax.kv" %>
<%@ page import="java.util.List" %>
<%@ page import="java.lang.String" %>
<%@ page import="oz.fetchcontent.main.rs" %>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta content="width=device-width,initial-scale=1.0" name="viewport">
</head>
<body style="max-width:320px">
<%
	String url = request.getParameter("target");
	String sql = Sqlmaker.queryNewsBylinkhash(url);
	
	List<List<kv> > rest = Datax.getInstance().queryDataviaDB(sql, new Newsdx());
	if (rest != null && rest.size() >0 ){
		List<kv> ctent = rest.get(0);
		
		kv e = kv.getkv(rs.ORILINK, ctent);
		String orilink = e.getString();
		
		e = kv.getkv(rs.STATUS, ctent);
		
		if (e.getInt() > 0){
			e = kv.getkv(rs.TITLE, ctent);
			String title = e.getString();
			
			e = kv.getkv(rs.CONTENT, ctent);
			String content = e.getString();
		
			out.println("<h1 align=\"center\">"+title+"</h1>");
			out.println("</br></br>");
			out.println("<div align=\"left\">");
			out.println(content);
			out.println("</div>");
			out.println("</br></br></br></br>");
			out.println("<div align=\"center\">");
			out.println("<a href=\""+orilink+"\">原文出处，转载于此</a></div>");
		}else{
			out.println("<h1><a href=\""+orilink+"\">无法抽取原文？</a></h1>");
		}
	}else{
		out.println("<h1>很抱歉，没有找到原文</h1>");
	}
%>
</body>
</html>