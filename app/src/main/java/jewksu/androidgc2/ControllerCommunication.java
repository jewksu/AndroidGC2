package jewksu.androidgc2;

import android.util.Log;

import org.jdom2.Document;
import org.jdom2.Element;

import java.io.IOException;
import java.net.Socket;

import core.XMLSocket;

/** @file
 * Manage communication with Controller
 */

public class ControllerCommunication {
    private static final String TAG = "ControllerCommunication";

    String server_host;
    int server_port;
    XMLSocket xmlsocket;

    public ControllerCommunication(String server_host, int server_port) {
        this.server_host = server_host;
        this.server_port = server_port;
        xmlsocket = null;
    }

    public String getSupervisionState() {
        String dateState = "ERROR";

        Element rootReq = new Element("request");
        Document request = new Document(rootReq);
        Element eltReqType = new Element("request_type");
        eltReqType.setText("REQ_SUPERVISION_STATE");
        rootReq.addContent(eltReqType);

        Log.i(TAG, "Sending request REQ_SUPERVISION_STATE");
        Document response = doCommunication(request);

        // check response type
        if (response != null) {
            Element rootResp = response.getRootElement();
            String responseType = rootResp.getChild("response_type").getTextNormalize().toUpperCase();
            Log.i(TAG, "Server response: " + responseType);
            if (responseType.equals("RESP_SUPERVISION_STATE")) {
                // get supervision data
                Element supervisionState = rootResp.getChild("supervision_state");
                dateState = supervisionState.getChild("date_state").getTextNormalize();
            }
        }
        return dateState;
    }

    // Send request and get associated response
    private Document doCommunication(Document request) {
        if (xmlsocket == null || xmlsocket.isClosed()) {
            Log.i(TAG, "Connecting to " + server_host + ":" + server_port);
            Socket socket = null;
            try {
                socket = new Socket(server_host, server_port);
            } catch (IOException e) {
                Log.e(TAG, "Error during connection to server", e);
                return null;
            }
            xmlsocket = new XMLSocket(socket);
        }

        xmlsocket.write(request);
        return xmlsocket.read();
    }

    public void endCommunication() {
        xmlsocket.close();
        xmlsocket = null;
    }
}
