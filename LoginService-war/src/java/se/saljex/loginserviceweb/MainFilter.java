/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.saljex.loginserviceweb;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import se.saljex.loginservice.LoginServiceConstants;
import se.saljex.loginservice.User;
import se.saljex.loginservice.LoginServiceBeanRemote;

/**
 *
 * @author Ulf
 */
@WebFilter(filterName = "MainFilter", urlPatterns = {"/*"})
public class MainFilter implements Filter {
	@EJB
	private LoginServiceBeanRemote loginServiceBean;

	@Resource(mappedName = "sxadm")
	private DataSource sxadm;
	
	private static final boolean debug = true;
	// The filter configuration object we are associated with.  If
	// this value is null, this filter instance is not currently
	// configured. 
	private FilterConfig filterConfig = null;
	
	public MainFilter() {
	}	
	

	/**
	 *
	 * @param request The servlet request we are processing
	 * @param response The servlet response we are creating
	 * @param chain The filter chain we are processing
	 *
	 * @exception IOException if an input/output error occurs
	 * @exception ServletException if a servlet error occurs
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest httpReq = null;
		HttpServletResponse httpRes = null;
		try { httpReq = (HttpServletRequest) request; } catch (Exception e) {}
		try { httpRes = (HttpServletResponse) response; } catch (Exception e) {}
		
		Throwable problem = null;
		Connection con = null;
		try { 
			con = sxadm.getConnection();
			request.setAttribute("sxconnection", con);
			request.setAttribute("sxdatasource", sxadm);
			request.setAttribute("LoginServiceBean", loginServiceBean);
			
			User tempUser = null;
			
			if (httpReq!=null && httpRes!=null) {
				Cookie cArr[] =  httpReq.getCookies();
				if (cArr!=null) {
					for (int x=0; x<cArr.length; x++) {
						if (LoginServiceConstants.COOKIE_LOGINSERVICE.equals(cArr[x].getName())) {
							tempUser = loginServiceBean.loginByUUID(cArr[x].getValue());
							request.setAttribute("cookieUser", tempUser);
							break;
						}
					}
				}
				
			}
			
			
			chain.doFilter(request, response);
		} catch (Throwable t) {
			// If an exception is thrown somewhere down the filter chain,
			// we still want to execute our after processing, and then
			// rethrow the problem after that.
			problem = t;
			t.printStackTrace();
		} finally {
			try {con.close(); } catch (Exception eeee) {}
		}
		

		// If there was a problem, we want to rethrow it if it is
		// a known type, otherwise log it.
		if (problem != null) {
			if (problem instanceof ServletException) {
				throw (ServletException) problem;
			}
			if (problem instanceof IOException) {
				throw (IOException) problem;
			}
			sendProcessingError(problem, response);
		}
	}

	/**
	 * Return the filter configuration object for this filter.
	 */
	public FilterConfig getFilterConfig() {
		return (this.filterConfig);
	}

	/**
	 * Set the filter configuration object for this filter.
	 *
	 * @param filterConfig The filter configuration object
	 */
	public void setFilterConfig(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

	/**
	 * Destroy method for this filter
	 */
	public void destroy() {		
	}

	/**
	 * Init method for this filter
	 */
	public void init(FilterConfig filterConfig) {		
		this.filterConfig = filterConfig;
		if (filterConfig != null) {
			if (debug) {				
				log("MainFilter:Initializing filter");
			}
		}
	}

	/**
	 * Return a String representation of this object.
	 */
	@Override
	public String toString() {
		if (filterConfig == null) {
			return ("MainFilter()");
		}
		StringBuffer sb = new StringBuffer("MainFilter(");
		sb.append(filterConfig);
		sb.append(")");
		return (sb.toString());
	}
	
	private void sendProcessingError(Throwable t, ServletResponse response) {
		String stackTrace = getStackTrace(t);		
		
		if (stackTrace != null && !stackTrace.equals("")) {
			try {
				response.setContentType("text/html");
				PrintStream ps = new PrintStream(response.getOutputStream());
				PrintWriter pw = new PrintWriter(ps);				
				pw.print("<html>\n<head>\n<title>Error</title>\n</head>\n<body>\n"); //NOI18N

				// PENDING! Localize this for next official release
				pw.print("<h1>The resource did not process correctly</h1>\n<pre>\n");				
				pw.print(stackTrace);				
				pw.print("</pre></body>\n</html>"); //NOI18N
				pw.close();
				ps.close();
				response.getOutputStream().close();
			} catch (Exception ex) {
			}
		} else {
			try {
				PrintStream ps = new PrintStream(response.getOutputStream());
				t.printStackTrace(ps);
				ps.close();
				response.getOutputStream().close();
			} catch (Exception ex) {
			}
		}
	}
	
	public static String getStackTrace(Throwable t) {
		String stackTrace = null;
		try {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			t.printStackTrace(pw);
			pw.close();
			sw.close();
			stackTrace = sw.getBuffer().toString();
		} catch (Exception ex) {
		}
		return stackTrace;
	}
	
	public void log(String msg) {
		filterConfig.getServletContext().log(msg);		
	}
}
