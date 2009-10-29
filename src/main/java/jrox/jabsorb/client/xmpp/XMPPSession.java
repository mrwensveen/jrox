package jrox.jabsorb.client.xmpp;

import java.util.concurrent.Future;

import jrox.util.xmpp.XMPPConnectionManager;
import jrox.util.xmpp.XMPPURI;

import org.jabsorb.JSONSerializer;
import org.jabsorb.client.ClientError;
import org.jabsorb.client.async.AsyncResultCallback;
import org.jabsorb.client.async.AsyncSession;
import org.jabsorb.client.async.SettableFuture;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class XMPPSession implements AsyncSession {
	private static final Logger log = LoggerFactory.getLogger(XMPPSession.class);

	private final XMPPConnection xmppConnection;

	public XMPPSession(final XMPPURI uri) throws XMPPException {
		this.xmppConnection = XMPPConnectionManager.getInstance().getXMPPConnection(uri);
	}

	public Future<JSONObject> send(final JSONObject request) {
		return send(request, null);
	}

	public Future<JSONObject> send(final JSONObject request,
			final AsyncResultCallback<AsyncSession, JSONObject, JSONObject> callback) {

		try {
			// the current messageId
			final String messageId = request.getString(JSONSerializer.ID_FIELD);

			final SettableFuture<JSONObject> future = new SettableFuture<JSONObject>();
			final PacketListener listener = new PacketListener() {
				public void processPacket(final Packet packet) {
					final String receiveString = ((Message) packet).getBody();
					log.debug("receive: " + receiveString);

					try {
						final JSONObject response = new JSONObject(receiveString);

						// if it's a result (not a request from someone else, or even our own request)
						if (response.has(JSONSerializer.RESULT_FIELD) &&
								response.has(JSONSerializer.ID_FIELD) &&
								messageId.equals(response.getString(JSONSerializer.ID_FIELD))) {

							// Since this is the message we've been waiting for, we don't need this listener anymore
							XMPPSession.this.removeMessageListener(this);

							// Set the response on the future
							future.set(response);

							// Call the callback
							callback.onAsyncResult(XMPPSession.this, future, request);
						}
					} catch (final JSONException e) {
						// okay
						log.debug("Ignoring non-JSON message");
					}
				}
			};

			addMessageListener(listener);
			sendMessage(request.toString());

			return future;
		} catch (final XMPPException e) {
			log.error("Unable to send JSON-RPC request: " + request.toString(), e);
			throw new ClientError(e);
		} catch (final JSONException e) {
			log.error("Unable to get id from JSON-RPC request: " + request.toString(), e);
			throw new ClientError(e);
		}
	}

	public void close() {
		xmppConnection.disconnect();
	}

	/**
	 * @param message
	 * @param packetListener
	 * @throws XMPPException
	 */
	protected abstract void sendMessage(String message) throws XMPPException;

	protected abstract void addMessageListener(PacketListener listener);

	protected abstract void removeMessageListener(PacketListener listener);

	/**
	 * @return the xmppConnection
	 */
	public XMPPConnection getXmppConnection() {
		return xmppConnection;
	}
}
