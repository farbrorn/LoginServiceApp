/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.saljex.loginservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;
import se.saljex.sxlibrary.SXConstant;

/**
 *
 * @author Ulf
 */
public class LoginHelper {

	
	public static UserSetWrapper login(Connection con, String anvandare, String losen) throws SQLException {
		UserSetWrapper userSetWrapper = null;
		PreparedStatement stm = con.prepareStatement("select * from saljare s where forkortning = ? and losen = ? and namn in (select anvandare from anvbehorighet a where a.anvandare = s.namn and behorighet = ?)");
		stm.setString(1, anvandare);
		stm.setString(2, losen);
		stm.setString(3, SXConstant.BEHORIGHET_INTRA_LOGIN);
		ResultSet rs = stm.executeQuery();
		if (rs.next()) {
			String uuid = createNewLogin(con, anvandare);
			if (uuid!=null) {
				userSetWrapper = getUser(con, anvandare);
				userSetWrapper.setUuid(uuid);
				userSetWrapper.setUuidCreated(new java.util.Date());
			}
		}
/*		try { 
			removeOldUuids(con);
		} catch (SQLException e) {
			Logger.getLogger("sx-loginservice").severe("SQL-Fel:" + e.getMessage()); e.printStackTrace();
		}
*/		return userSetWrapper;
	}
	
	public static UserSetWrapper loginByUUID(Connection con, String uuid) throws SQLException {
		UserSetWrapper userSetWrapper = null;
		if (uuid==null) return null;
		PreparedStatement stm = con.prepareStatement("select uuid, anvandare, expiredate, crdate from loginservice where uuid=? and expiredate is not null and expiredate>=?");
		stm.setString(1, uuid);
		stm.setDate(2, new java.sql.Date(new java.util.Date().getTime()) );
		ResultSet rs = stm.executeQuery();
		if (rs.next()) {
			
			//Check om inloggningen är för gammal och måste förnyas
			java.sql.Timestamp crdate = rs.getTimestamp(4);
			if (crdate!= null) {
				long diffInDays = (new java.util.Date().getTime() - crdate.getTime()) / (1000 * 60 * 60 * 24);
				if (diffInDays>100) {
					setExpireDate(con, uuid, null); //Ogiltigförklara uuid
				} else {
					userSetWrapper = getUser(con, rs.getString(2));
					userSetWrapper.setUuid(uuid);
					prolongExpireDate(con, uuid);
				}
			}
		}		
		return userSetWrapper;
	}

	public static void removeOldUuids(Connection con) throws SQLException{
		PreparedStatement stm = con.prepareStatement("delete from loginservice where crdate is null or crdate <=?");
		java.sql.Timestamp removeDate = new java.sql.Timestamp(new java.util.Date().getTime()-(1000 * 60 * 60 * 24 * 365 * 2));	// Äldre än 2 år rensas bort
		stm.setTimestamp(1, removeDate);
		stm.executeUpdate();
	}
	
	protected static UserSetWrapper getUser(Connection con, String anvandare) throws SQLException{
		UserSetWrapper userSetWrapper = null;
		PreparedStatement stm = con.prepareStatement("select namn, epost, lagernr from saljare where forkortning=?");
		PreparedStatement stm2 = con.prepareStatement("select a.behorighet from anvbehorighet a, saljare s where s.forkortning=? and a.anvandare = s.namn");
		stm.setString(1, anvandare);
		ResultSet rs = stm.executeQuery();
		
		if (rs.next()) {
			userSetWrapper = new UserSetWrapper();
			userSetWrapper.setNamn(rs.getString(1));
			userSetWrapper.setAnvandare(anvandare);
			userSetWrapper.setEpost(rs.getString(2));
			userSetWrapper.setLagernr(rs.getInt(3));
			stm2.setString(1, anvandare);
			ResultSet rs2 = stm2.executeQuery();
			ArrayList<String> arr = new ArrayList();
			while (rs2.next()) {
				arr.add(rs2.getString(1));
			}
			userSetWrapper.setBehorigheter(arr);
		}
		return userSetWrapper;
	}
	
	
	protected static String createNewLogin(Connection con, String anvandare) throws  SQLException{
		String uuid = null;
		String tempUuid;
		PreparedStatement stm = con.prepareStatement("insert into loginservice (uuid, anvandare, expiredate) values (?,?,?)");
		int cn = 0;
		while (true) {
			cn++;
			if (cn>100) throw new SQLException("Kan inte skapa nytt uuid. För många försök. Försök igen.");
			tempUuid = UUID.randomUUID().toString();
			stm.setString(1, tempUuid);
			stm.setString(2, anvandare);
			stm.setDate(3, null);
			if (stm.executeUpdate() > 0) {
				prolongExpireDate(con, tempUuid);
				uuid = tempUuid;
				break;
			}			
		}
		return uuid;
	}
	
	//Förläng tiden för en giltig inloggning. Utgår från dagens dautm och lägger till giltighetstiden
	public static void prolongExpireDate(Connection con, String uuid) throws  SQLException{
		setExpireDate(con, uuid, new java.sql.Date(new java.util.Date().getTime()+1000*60*60*24*4));
	}
	
	public static void setExpireDate(Connection con, String uuid, java.sql.Date expireDate) throws  SQLException{
		PreparedStatement stm = con.prepareStatement("update loginservice set expiredate=? where uuid=?");
		stm.setDate(1, expireDate);
		stm.setString(2, uuid);
		int cnt = stm.executeUpdate();
		if (cnt<1) throw(new SQLException("UUID kunde inte hittas."));
	}
	
	
	public static void logoutAllUserSessions(Connection con, User user) throws  SQLException{
		PreparedStatement stm = con.prepareStatement("update loginservice set expiretime=null where anvandare=?");
		stm.setString(1, user.getAnvandare());
		stm.executeUpdate();
	}

	
	public static void logoutSession(Connection con, User user) throws  SQLException{
		PreparedStatement stm = con.prepareStatement("update loginservice set expiretime=null where uuid=?");
		stm.setString(1, user.getUuid());
		stm.executeUpdate();
		user.setUserAsLoggedOut();
	}

	

	
}
