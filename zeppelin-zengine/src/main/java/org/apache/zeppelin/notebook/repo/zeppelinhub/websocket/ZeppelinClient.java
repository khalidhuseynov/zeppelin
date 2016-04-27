package org.apache.zeppelin.notebook.repo.zeppelinhub.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZeppelinClient {
  private static final Logger LOG = LoggerFactory.getLogger(ZeppelinClient.class);
  private Client baseClient;
  private URI zeppelinUri;
  public WebSocketClient wsClient;
  public URI zeppelinWebsocketUrl;
  private ConcurrentHashMap<String, Session> zeppelinConnectionMap;

  public static ZeppelinClient newInstance(String url, Client client) {
    return new ZeppelinClient(url, client);
  }

  private ZeppelinClient(String zeppelinUri, Client client) {
    this.baseClient = client;
    zeppelinWebsocketUrl = URI.create(zeppelinUri);
    WebSocketClient wsClient = new WebSocketClient();
  }

  public void start() {
    try {
      if (wsClient != null) {
        wsClient.start();
      } else {
        LOG.warn("Cannot start zeppelin websocket client - isn't initialized");
      }
    } catch (Exception e) {
      LOG.error("Cannot start Zeppelin websocket client", e);
    }
  }

  public void stop() {
    try {
      if (wsClient != null) {
        wsClient.stop();
      } else {
        LOG.warn("Cannot stop zeppelin websocket client - isn't initialized");
      }
    } catch (Exception e) {
      LOG.error("Cannot stop Zeppelin websocket client", e);
    }
  }

  /* per notebook based ws connection */
  public Session getZeppelinConnection(String noteId) throws IOException, InterruptedException, ExecutionException {
    // return existing connection
    if (zeppelinConnectionMap.containsKey(noteId)) {
      LOG.info("Connection for {} exists in map", noteId);
      return zeppelinConnectionMap.get(noteId);
    }

    // create connection
    ClientUpgradeRequest request = new ClientUpgradeRequest();
    ZeppelinWebsocket socket = new ZeppelinWebsocket(noteId);
    Future<Session> future = wsClient.connect(socket, zeppelinUri, request);
    Session session = future.get();
    if (zeppelinConnectionMap.containsKey(noteId)) {
      session.close();
      session = zeppelinConnectionMap.get(noteId);
    } else {
      zeppelinConnectionMap.put(noteId, session);
    }
    LOG.info("Create Zeppelin websocket connection {} {}", zeppelinUri.toString(), noteId);
    return session;
  }

  private void sendToHub(ZeppelinWebsocket socket, String msgFromZeppelin) {
    Map<String, String> meta = new HashMap<String, String>();
    meta.put("token", baseClient.token);
    meta.put("noteId", socket.noteId);
    //TODO(khalid): refactor to general send
    // ZeppelinHubMessage msgToSendHub = new ZeppelinHubMessage(
    // msgFromZeppelin.getOp(), msgFromZeppelin.getData(), meta);
    // parentClient.getHubConnection().sendMessage(parentClient.serialize(msgToSendHub));
    // LOG.info("Send {} message from Zeppelin to Hub : ",
    // msgFromZeppelin.op.toString());
  }

  public class ZeppelinWebsocket implements WebSocketListener {
    public Session connection;
    public String noteId;

    public ZeppelinWebsocket(String noteId) {
      this.noteId = noteId;
    }

    @Override
    public void onWebSocketBinary(byte[] arg0, int arg1, int arg2) {

    }

    @Override
    public void onWebSocketClose(int code, String message) {
      LOG.info("Zeppelin connection closed with code: {}, message: {}", code, message);
      // parentClient.removeConnMap(noteId);
    }

    @Override
    public void onWebSocketConnect(Session session) {
      LOG.info("Zeppelin connection opened");
      this.connection = session;
    }

    @Override
    public void onWebSocketError(Throwable e) {
      LOG.warn("Zeppelin socket connection error ", e);
    }

    @Override
    public void onWebSocketText(String data) {
      LOG.debug("Zeppelin client received Message: " + data);
      // propagate to ZeppelinHub
      try {
        sendToHub(this, data);
      } catch (Exception e) {
        LOG.error("Failed to send message to ZeppelinHub: ", e);
      }
    }

  }

}