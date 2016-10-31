/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ssl.bluetruth.servlet.filter;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ssl.bluetruth.database.DatabaseManager;
import ssl.bluetruth.database.DatabaseManagerException;
import ssl.bluetruth.utils.AuditTrailProcessor;

/**
 *
 * @author nthompson
 */
public class AuthenticatedSessionFilter implements Filter {

    private static final Logger LOGGER = LogManager.getLogger(AuthenticatedSessionFilter.class);
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;
    // SCJS 023 appended column 'last_password_update_timestamp' to the query string
    private String FETCH_SESSION_DATA = "SELECT branding.css_url, instation_user.timezone_name, last_password_update_timestamp, "
            + "(expiry_days-extract(epoch from (NOW() - last_password_update_timestamp))/(3600*24)) >= 0 as active "
            + "FROM branding LEFT JOIN instation_user ON branding.brand = instation_user.brand "
            + "WHERE instation_user.username = ?;";
    // SCJS 023 END

    public AuthenticatedSessionFilter() {
    }

    private void doBeforeProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        LOGGER.debug("AuthenticatedSessionFilter:DoBeforeProcessing");

        HttpServletRequest hsr = (HttpServletRequest) request;
        HttpSession session = hsr.getSession(true);

        if (hsr.getUserPrincipal() != null
                && sessionDataInvalid(session)) {
            String username = hsr.getUserPrincipal().getName();
            setSessionData(username, session);
            AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.USER_LOGIN, "User logged in");
        }

        if (!(Boolean) session.getAttribute("active")) {
            ServletContext context = getFilterConfig().getServletContext();
            RequestDispatcher rd = context.getRequestDispatcher("/WEB-INF/PasswordUpdate.jsp");
            rd.forward(request, response);
        }
    }

    private boolean sessionDataInvalid(HttpSession session) {
        return session.getAttribute("css_url") == null
                || session.getAttribute("user_timezone") == null
                || session.getAttribute("active") == null;
    }

    private void setSessionData(String username, HttpSession session) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            DatabaseManager dm = DatabaseManager.getInstance();
            connection = dm.getDatasource().getConnection();
            stmt = connection.prepareStatement(FETCH_SESSION_DATA);
            stmt.setString(1, username);
            rs = stmt.executeQuery();
            rs.next();
            session.setAttribute("username", username);
            session.setAttribute("css_url", rs.getString("css_url"));
            session.setAttribute("user_timezone", rs.getString("timezone_name"));
            // SCJS 023 START
            session.setAttribute("active", rs.getBoolean("active"));
            // SCJS 023 END
        } catch (SQLException ex) {
            LOGGER.info(ex.getMessage());
        } catch (NamingException ex) {
            LOGGER.fatal(ex.getMessage());
        } catch (DatabaseManagerException ex) {
            LOGGER.fatal(ex.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                }
                stmt = null;
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
                connection = null;
            }
        }
    }

    private void doAfterProcessing(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        LOGGER.debug("AuthenticatedSessionFilter:DoAfterProcessing");

        HttpServletRequest hsr = (HttpServletRequest) request;
        HttpSession session = hsr.getSession(true);

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

        LOGGER.debug("AuthenticatedSessionFilter:doFilter()");

        doBeforeProcessing(request, response);

        Throwable problem = null;
        try {
            chain.doFilter(request, response);
        } catch (Throwable t) {
            // If an exception is thrown somewhere down the filter chain,
            // we still want to execute our after processing, and then
            // rethrow the problem after that.
            problem = t;
            LOGGER.warn(t.getMessage());
        }

        doAfterProcessing(request, response);

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
            LOGGER.debug("AuthenticatedSessionFilter:Initializing filter");
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("AuthenticatedSessionFilter()");
        }
        StringBuilder sb = new StringBuilder("AuthenticatedSessionFilter(");
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
}
