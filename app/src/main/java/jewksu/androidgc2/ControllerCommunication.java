package jewksu.androidgc2;

import android.util.Log;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;

/** @file
 * Manage communication with Controller
 */

public class ControllerCommunication {
    private static final String TAG = "ControllerCommunication";

    String server_host;
    int server_port;
    Socket socket;

    public ControllerCommunication(String server_host, int server_port) {
        this.server_host = server_host;
        this.server_port = server_port;
        socket = null;
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

    private Document doCommunication(Document request) {
        SAXBuilder sxb = new SAXBuilder();
        Document response = null;

        try {
            if (socket == null) {
                Log.i(TAG, "Connecting to " + server_host + ":" + server_port);
                socket = new Socket(server_host, server_port);
            }

            // send request
            XMLOutputter xmlOutput = new XMLOutputter(Format.getCompactFormat());
            xmlOutput.output(request, socket.getOutputStream());
            socket.getOutputStream().write('\n'); // empty line to indicate end of request

            // get server response (buffer lines until an empty line is found)
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            StringBuilder xmlRequest = new StringBuilder();
            while (true)
            {
                String line = buffRead.readLine();
                if (line == null || line.isEmpty())
                    break;
                xmlRequest.append(line);
            }
            response = sxb.build(new StringReader(xmlRequest.toString()));
        } catch (UnknownHostException e) {
            Log.e(TAG, "Unknown host", e);
        } catch (IOException e) {
            Log.e(TAG, "Error during connection to server", e);
        } catch (JDOMException e) {
            Log.e(TAG, "Invalid XML", e);
        }

        return response;
    }

    public void endCommunication() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "Error while closing socket", e);
            }
            socket = null;
        }
    }
}
