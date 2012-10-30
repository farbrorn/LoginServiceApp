<%-- 
    Document   : logout
    Created on : 2012-okt-29, 15:33:41
    Author     : Ulf
--%>
<%@page import="se.saljex.loginservice.Util"%>
<%@page import="se.saljex.loginservice.User"%>
<%@page import="se.saljex.loginservice.LoginServiceBeanRemote"%>
<%
	String errStr = null;
	LoginServiceBeanRemote loginServiceBean = null;
	User user = null;
	try {  
		loginServiceBean = (LoginServiceBeanRemote)request.getAttribute("LoginServiceBean"); 
		Cookie co = Util.getLoginCookie(request.getCookies());
		if (co!=null) {
			user = loginServiceBean.loginByUUID(co.getValue());
			if (user != null) {
				loginServiceBean.logoutSession(user);
			}
		}
	} catch (Exception e) {
		errStr = "Fel vid utloggningen. " + e.getMessage();
	}
%>
<%@page contentType="text/html" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Säljex - Logga Ut</title>
    </head>
    <body>
<%= errStr==null ? "<h1>Du är utloggad!</h1>Observera att det kan dröja flera timmar innan aktiva applikationer loggas ut. Logga därför ut från dem manuellt innan du lämnar datorn." : "<h1>* * * Fel * * * Du är inte utloggad</h1> " + errStr %>
    </body>
</html>
