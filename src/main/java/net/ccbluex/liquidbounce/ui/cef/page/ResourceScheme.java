package net.ccbluex.liquidbounce.ui.cef.page;

import org.cef.browser.scheme.IScheme;
import org.cef.browser.scheme.SchemePreResponse;
import org.cef.browser.scheme.SchemeResponseData;
import org.cef.browser.scheme.SchemeResponseHeaders;
import net.ccbluex.liquidbounce.utils.ClientUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author montoyo
 */
public class ResourceScheme implements IScheme {

    private String contentType = null;
    private InputStream is = null;

    @Override
    public SchemePreResponse processRequest(String url) {
        url = url.substring("resource://".length());

        is = ResourceScheme.class.getResourceAsStream("/" + url);
        if(is == null) {
            ClientUtils.INSTANCE.logWarn("Resource " + url + " NOT found!");
            return SchemePreResponse.NOT_HANDLED; //Mhhhhh... 404?
        }

        contentType = null;
        int pos = url.lastIndexOf('.');
        if(pos >= 0 && pos < url.length() - 2)
            contentType = mimeTypeFromExtension(url.substring(pos + 1));

        return SchemePreResponse.HANDLED_CONTINUE;
    }

    @Override
    public void getResponseHeaders(SchemeResponseHeaders rep) {
        if(is == null) {
            rep.setStatus(404);
            rep.setStatusText("Not Found");
            return;
        }

        if(contentType != null)
            rep.setMimeType(contentType);

        rep.setStatus(200);
        rep.setStatusText("OK");
        rep.setResponseLength(-1);
    }

    @Override
    public boolean readResponse(SchemeResponseData data) {
        if(is == null)
            return false;

        try {
            int ret = is.read(data.getDataArray(), 0, data.getBytesToRead());
            if(ret <= 0)
                is.close();

            data.setAmountRead(Math.max(ret, 0));
            return ret > 0;
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static final HashMap<String, String> mimeTypeMap = new HashMap<>();

    static {
        loadMimeTypeMapping();
    }

    public static void loadMimeTypeMapping() {
        Pattern p = Pattern.compile("^(\\S+)\\s+(\\S+)\\s*(\\S*)\\s*(\\S*)$");
        String line = "";
        int cLine = 0;
        mimeTypeMap.clear();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(ResourceScheme.class.getResourceAsStream("/assets/minecraft/fdpclient/cef/mime.types")));

            while(true) {
                cLine++;
                line = br.readLine();
                if(line == null)
                    break;

                line = line.trim();
                if(!line.startsWith("#")) {
                    Matcher m = p.matcher(line);
                    if(!m.matches())
                        continue;

                    mimeTypeMap.put(m.group(2), m.group(1));
                    if(m.groupCount() >= 4 && !m.group(3).isEmpty()) {
                        mimeTypeMap.put(m.group(3), m.group(1));

                        if(m.groupCount() >= 5 && !m.group(4).isEmpty())
                            mimeTypeMap.put(m.group(4), m.group(1));
                    }
                }
            }

            br.close();
        } catch(Throwable e) {
            ClientUtils.INSTANCE.logError("[Mime Types] Error while parsing \"" + line + "\" at line " + cLine + " : " + e.getMessage());
            e.printStackTrace();
        }

        ClientUtils.INSTANCE.logInfo("Loaded " + mimeTypeMap.size() + " mime types");
    }

    private static String mimeTypeFromExtension(String ext) {
        ext = ext.toLowerCase();
        String ret = mimeTypeMap.get(ext);
        if(ret != null)
            return ret;

        //If the mimeTypeMap couldn't be loaded, fall back to common things
        switch(ext) {
            case "htm":
            case "html":
                return "text/html";

            case "css":
                return "text/css";

            case "js":
                return "text/javascript";

            case "png":
                return "image/png";

            case "jpg":
            case "jpeg":
                return "image/jpeg";

            case "gif":
                return "image/gif";

            case "svg":
                return "image/svg+xml";

            case "xml":
                return "text/xml";

            case "txt":
                return "text/plain";

            default:
                return null;
        }
    }
}