/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.saljex.loginservice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.sql.DataSource;

/**
 *
 * @author Ulf
 */
@Stateless
public class LoginServiceBean implements LoginServiceBeanRemote {
	@Resource(mappedName = "sxadm")
	private DataSource sxadm;

	@RolesAllowed("admin")
	@Override
	public String test() {
		return "Testen";
	}


	
	
	
	@Override
	public User loginByUUID(String uuid) {
		Connection con=null;
		try {
			con = sxadm.getConnection();
			return LoginHelper.loginByUUID(con, uuid);
		} catch (SQLException e) {
			Logger.getLogger("sx-loginservice").severe("SQL-Fel:" + e.getMessage()); e.printStackTrace(); return null;
		} finally { try {con.close(); } catch (Exception eee) {}}		

	}

	@Override
	public User login(String anvandare, String losen) {
		Connection con=null;
		try {
			con = sxadm.getConnection();
			return LoginHelper.login(con, anvandare, losen);
		} catch (SQLException e) {
			Logger.getLogger("sx-loginservice").severe("SQL-Fel:" + e.getMessage()); e.printStackTrace(); return null;
		} finally { try {con.close(); } catch (Exception eee) {}}		
	}

	@Override
	public void logoutSession(User user) {
		Connection con=null;
		try {
			con = sxadm.getConnection();
			LoginHelper.logoutSession(con, user);
		} catch (SQLException e) {
			Logger.getLogger("sx-loginservice").severe("SQL-Fel:" + e.getMessage()); e.printStackTrace();
		} finally { try {con.close(); } catch (Exception eee) {}}		
	}

	
	@Override
	public void logoutAllUserSessions(User user) {
		Connection con=null;
		try {
			con = sxadm.getConnection();
			LoginHelper.logoutAllUserSessions(con, user);
		} catch (SQLException e) {
			Logger.getLogger("sx-loginservice").severe("SQL-Fel:" + e.getMessage()); e.printStackTrace();
		} finally { try {con.close(); } catch (Exception eee) {}}				
	}
	
	
	
}
