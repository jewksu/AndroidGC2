package core;

import android.os.AsyncTask;
import android.util.Log;

import org.jdom2.Document;
import org.jdom2.Element;

import java.io.IOException;
import java.net.Socket;

/** @file
 * Manage communication with Controller
 *
 * Android forbids Socket communication in main thread (UI).
 * So an AsyncTask is used to perform all socket communication in background.
 */

public class ControllerCommunication {
    private static final String TAG = "ControllerCommunication";

    // callback to provide response to application
    static public interface ResponseListener {
        public void onControllerResponse(Document response);
    }

    // inner class to manage socket in a dedicated thread
    private class BackgroundTask extends AsyncTask<Document, Void, Document> {

        // background function, called in a dedicated thread
        // start socket if needed, send request, get server response
        @Override
        protected Document doInBackground(Document... request) {
            if (xmlsocket == null || xmlsocket.isClosed()) {
                Log.i(TAG, "Connecting to " + server_host + ":" + server_port);
                Socket socket = null;
                xmlsocket = null;
                try {
                    socket = new Socket(server_host, server_port);
                } catch (IOException e) {
                    Log.e(TAG, "Error during connection to server", e);
                    return null;
                }
                xmlsocket = new XMLSocket(socket);
            }

            xmlsocket.write(request[0]);
            return xmlsocket.read();
        }

        // end of processing, provide response; called in main thread
        @Override
        protected void onPostExecute(Document response) {
            super.onPostExecute(response);
            // call parent class handler
            ControllerCommunication.this.onBgTaskEnd(response);
        }
    }

    String server_host;
    int server_port;
    XMLSocket xmlsocket;
    BackgroundTask bgTask;
    ResponseListener listener;

    // Constructor
    public ControllerCommunication(String server_host, int server_port, ResponseListener listener) {
        this.server_host = server_host;
        this.server_port = server_port;
        this.listener = listener;
        xmlsocket = null;
        bgTask = null;
    }

    // stop BG task, if any
    private void stopBgTask() {
        if (bgTask != null)
        {
            bgTask.cancel(true);
            bgTask = null;
        }
    }

    // Close underlying socket
    public void close() {
        stopBgTask();
        if (xmlsocket != null) {
            xmlsocket.close(); // seems that close can be done in main thread
            xmlsocket = null;
        }
    }

    // Send a simple request (type, no data) to Controller
    public void simpleRequest(String requestType) {
        // build full request
        Element rootReq = new Element("request");
        Document request = new Document(rootReq);
        Element eltReqType = new Element("request_type");
        eltReqType.setText(requestType);
        rootReq.addContent(eltReqType);

        // and send it
        complexRequest(request);
    }

    // Send a complex request (type + data) to Controller
    public void complexRequest(Document request) {
        // stop pending BG task if any
        stopBgTask();

        // start AsyncTask to send request
        bgTask = new BackgroundTask();
        bgTask.execute(request);
    }

    private void onBgTaskEnd(Document response) {
        bgTask = null;
        listener.onControllerResponse(response);
    }
}
