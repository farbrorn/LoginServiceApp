<%-- 
    Document   : index
    Created on : 2012-okt-29, 09:21:28
    Author     : Ulf
--%>
<%@page import="se.saljex.loginservice.Util"%>
<%@page import="se.saljex.loginservice.User"%>
<%@page import="se.saljex.loginservice.LoginServiceBeanRemote"%>
<%@page contentType="text/html" pageEncoding="ISO-8859-1"%>
<%
	User user = null; 
	String anvandare = request.getParameter("anvandare");
	String losen = request.getParameter("losen");
	String refpage=request.getParameter("ref");
	String errStr = null;
	if (anvandare==null || losen == null || anvandare.length()<1 || losen.length()<1) {

	} else {
		LoginServiceBeanRemote loginServiceBean = null;
		try {loginServiceBean = (LoginServiceBeanRemote)request.getAttribute("LoginServiceBean"); } catch (Exception e) {}
		if (loginServiceBean!=null) {
			user = loginServiceBean.login(anvandare, losen);
			if (user!=null) {
				response.addCookie(Util.createLoginCookie(user.getUuid()));
				if (refpage!=null) {
//					response.sendRedirect("http://" + refpage);
					response.sendRedirect( refpage);
				//	redirectStr =refpage;
				} else {
					response.sendRedirect( "inloggad.jsp");
				}
			} else {
				errStr = "Felaktiga inloggningsuppgifter";
			}
		}
	} 
	
%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
        <title>Säljex Login</title>

    </head>
    <body>
        <h1>Logga in</h1>
		<%= errStr!=null ? errStr+"<br>" : "" %>
		<%= refpage!=null ? refpage+"<br>" : "" %>
		<form action="" method="post">
			<table>
				<tr>
					<td>Användare:</td>
					<td><input type="text" name="anvandare"></td>
				</tr>
				<tr>
					<td>Lösen:</td>
					<td><input type="password" name="losen"></td>
				</tr>
				<tr>
					<td colspan="2">
						<input type="submit" name="OK">
					</td>
				</tr>
			</table>
		</form>
    </body>
	
</html>

