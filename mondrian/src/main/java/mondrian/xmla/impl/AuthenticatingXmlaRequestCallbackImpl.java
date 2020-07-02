
package mondrian.xmla.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbcp.BasicDataSource;
import org.w3c.dom.Element;

import mondrian.olap.Connection;
import mondrian.olap.DriverManager;
import mondrian.olap.Util;
import mondrian.olap.Util.PropertyList;
import mondrian.rolap.RolapConnection;
import mondrian.util.StaticThreadLocal;
import mondrian.xmla.XmlaConstants;



public class AuthenticatingXmlaRequestCallbackImpl extends AuthenticatingXmlaRequestCallback {

    // HTTP convenience constants
    private static final String CHARACTER_ENCODING = "UTF-8";
    private static final String AUTH_KEYWORD = "Authorization";
    private static final String BASIC_AUTH_KEYWORD = "Basic ";
    private static final String USER_AGENT_KEYWORD = "User-Agent";
    private static final String SERVICE_NAME_KEYWORD = "OData2";

    // OData AUTH convenience constants
    private static final String AUTHORIZATION_CHALLENGE_ATTRIBUTE = "WWW-AUTHENTICATE";
    private static final String AUTHORIZATION_CHALLENGE_REALM = "Denodo_OData_Service";
    private static final String AUTHORIZATION_CHALLENGE_BASIC =
        "BASIC" + " realm=\"" + AUTHORIZATION_CHALLENGE_REALM + "\", accept-charset=\"" + CHARACTER_ENCODING + "\"";
    private static final String BEARER = "Bearer";
    private static final String NEGOTIATE = "Negotiate";
    private static final String BASIC = "Basic";

    @Override
    public String authenticate(String username, String password, String sessionID) {
        if (username != null && username.equals("admin")) {
            return "xmla";
        } else {
            return null;
        }
    }

    @Override
    public String generateSessionId(Map<String, Object> context) {
        String sessionIdStr = Long.toString(17L * System.nanoTime() + 3L * System.currentTimeMillis(), 35);

        context.put(XmlaConstants.CONTEXT_XMLA_SESSION_ID, sessionIdStr);
        return sessionIdStr;
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {

        super.init(servletConfig);
    }

    @Override
    public void postAction(HttpServletRequest request, HttpServletResponse response, byte[][] responseSoapParts,
        Map<String, Object> context) throws Exception {
        super.postAction(request, response, responseSoapParts, context);
    }

    @Override
    public void preAction(HttpServletRequest request, Element[] requestSoapParts, Map<String, Object> context)
        throws Exception {
        String authHeader = request.getHeader("authorization");

//        context.put(XmlaConstants.CONTEXT_XMLA_PASSWORD, "admin");

        if (authHeader != null) {

            final String[] credentials = retrieveCredentials(authHeader);

            if (credentials == null) {

                return;
            }
            //TODO
            String login = escapeLiteral(credentials[0]);
            String password = escapeLiteral(credentials[1]);
            String encodedValue = authHeader.split(" ")[1];

            String connectString = "Provider=mondrian; DataSource=java:comp/env/jdbc/VDPdatabase; "
                + "Catalog=file:/C:/Users/pleira/workspace/bin/apache-tomcat-9.0"
                + ".14/webapps/xmondrian/WEB-INF/schema/Foodmart.xml; user=admin; password=admin; Role=xmla";
            BasicDataSource dataSource = new BasicDataSource();

            dataSource.setDriverClassName("com.denodo.vdp.jdbc.Driver");

            dataSource.setUrl("jdbc:vdb://localhost:9999/admin");

            dataSource.setUsername(login);
            context.put(XmlaConstants.CONTEXT_XMLA_USERNAME, login);
            dataSource.setPassword(password);
            context.put(XmlaConstants.CONTEXT_XMLA_PASSWORD, password);
            Connection conn = null;
            Util.PropertyList properties = new PropertyList();
            properties.put("Provider", "mondrian");
            properties.put("Catalog",
                "C:\\Users\\pleira\\workspace\\bin\\apache-tomcat-9.0"
                    + ".14\\webapps\\xmondrian\\WEB-INF\\schema\\Foodmart.xml");
            properties.put("PoolNeeded", "true");
            generateSessionId(context);
            Connection connection = (RolapConnection) DriverManager.getConnection(properties, null, dataSource);
            StaticThreadLocal.getThreadLocal().set(dataSource);


        }
        //     super.preAction(request, requestSoapParts, context);
    }

    private static String escapeLiteral(final String literal) {
        return literal.replaceAll("'", "''");
    }

    /**
     * This method extracts the credentials from the AUTH HTTP request segment
     *
     * @param authHeader AUTH HTTP request segment
     */
    private static final String[] retrieveCredentials(final String authHeader) throws UnsupportedEncodingException {
        final String credentialsString = authHeader.substring(BASIC_AUTH_KEYWORD.length());
        final String decoded = new String(Base64.decodeBase64(credentialsString), CHARACTER_ENCODING);

        final String[] credentials = new String[2];
        credentials[0] = decoded.substring(0, decoded.indexOf(':'));
        credentials[1] = decoded.substring(decoded.indexOf(':') + 1);
        return credentials;
    }

    @Override
    protected void throwAuthenticationException(String reason) {
        super.throwAuthenticationException(reason);
    }

    @Override
    public boolean processHttpHeader(HttpServletRequest request, HttpServletResponse response,
        Map<String, Object> context) throws Exception {
        String authHeader = request.getHeader("authorization");
        if (authHeader == null) {
            showLogin(response, "aa");
            return false;
        }

        return true;
    }


    private static void showLogin(final HttpServletResponse response, final String msg) throws IOException {
        // Set AUTH challenge in request
        response.setHeader(AUTHORIZATION_CHALLENGE_ATTRIBUTE, AUTHORIZATION_CHALLENGE_BASIC);
        response.setCharacterEncoding(CHARACTER_ENCODING);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, msg);
    }
}
