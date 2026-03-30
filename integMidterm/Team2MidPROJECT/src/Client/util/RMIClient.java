package Client.util;

import shared.RemoteService;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RMIClient {

    private static final String SERVER_HOST =
            System.getProperty("server.host", "localhost");
    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "FoodWasteService";

    public static String sendRequest(String xmlRequest) {
        try {
            Registry registry = LocateRegistry.getRegistry(SERVER_HOST, RMI_PORT);
            RemoteService service = (RemoteService) registry.lookup(SERVICE_NAME);
            return service.processRequest(xmlRequest);
        } catch (Exception e) {
            return buildErrorResponse("Connection failed: " + e.getMessage());
        }
    }

    public static String buildRequest(String action, String username) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<request>\n"
                + "  <action>" + action + "</action>\n"
                + "  <username>" + escapeXML(username) + "</username>\n"
                + "</request>";
    }

    public static String buildRequest(String action, String username, String dataContent) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<request>\n"
                + "  <action>" + action + "</action>\n"
                + "  <username>" + escapeXML(username) + "</username>\n"
                + "  <data>\n"
                + dataContent + "\n"
                + "  </data>\n"
                + "</request>";
    }

    public static String getStatus(String responseXML) {
        return extractTag(responseXML, "status");
    }

    public static String getMessage(String responseXML) {
        return extractTag(responseXML, "message");
    }

    public static boolean isSuccess(String responseXML) {
        return "SUCCESS".equals(getStatus(responseXML));
    }

    public static String getDataBlock(String responseXML) {
        int start = responseXML.indexOf("<data>");
        int end   = responseXML.lastIndexOf("</data>");
        if (start == -1 || end == -1) return "";
        return responseXML.substring(start + 6, end).trim();
    }

    private static String extractTag(String xml, String tagName) {
        String open  = "<"  + tagName + ">";
        String close = "</" + tagName + ">";
        int start = xml.indexOf(open);
        int end   = xml.indexOf(close);
        if (start == -1 || end == -1) return "";
        return xml.substring(start + open.length(), end).trim();
    }

    private static String buildErrorResponse(String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<response><status>ERROR</status>"
                + "<message>" + escapeXML(message) + "</message></response>";
    }

    private static String escapeXML(String text) {
        if (text == null) return "";
        return text
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&apos;");
    }
}