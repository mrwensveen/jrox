/**
 *
 */
package jrox;

import jrox.util.xmpp.XMPPConnectionManager;
import jrox.util.xmpp.XMPPURI;
import jrox.util.xmpp.XMPPUtil;

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.serializer.response.results.JSONRPCResult;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use this class to connect to an XMPP server and expose objects to.
 * @author matthijs
 */
public class ServerConnection {
	private static final Logger log = LoggerFactory.getLogger(ServerConnection.class);

	private final XMPPConnection xmppConnection;
	private final XMPPURI uri;

	public ServerConnection(final String uri) throws XMPPException {
		this(new XMPPURI(uri));
	}

	public ServerConnection(final XMPPURI uri) throws XMPPException {
		this.uri = uri;
		this.xmppConnection = XMPPConnectionManager.getInstance().getXMPPConnection(uri);
		initConnection();
	}

	/**
	 * Initializes the connection. Adds a MessageListener to process incoming
	 * messages. When overriding this method, make sure to call
	 * {@link #initConnection(String, String)}
	 * @throws XMPPException
	 */
	protected void initConnection() throws XMPPException {
		// listen to a room only?
		if (uri.getRoom() != null) {
			final MultiUserChat muc = new MultiUserChat(xmppConnection, uri.getRoom());

			final String nickname = XMPPUtil.getNickname(uri);
			try {
				// we're not interested in any history
				final DiscussionHistory history = new DiscussionHistory();
				history.setMaxChars(0);

				// attemp to join
				muc.join(nickname, uri.getRoomPassword(), history, 60000);
			} catch (final XMPPException xmppException) {
				if (xmppException.getXMPPError() != null && XMPPError.Condition.item_not_found.toString().equals(xmppException.getXMPPError().getCondition())) {
					// attemt to create
					muc.create(nickname);
				} else {
					throw xmppException;
				}
			}

			final PacketListener packetListener = new PacketListener() {
				public void processPacket(final Packet packet) {
					if (packet instanceof Message) {
						final JSONRPCResult result = ServerConnection.this.processMessage(muc, (Message) packet);
						if (result != null) {
							try {
								muc.sendMessage(result.toString());
							} catch (final XMPPException e) {
								log.error("Error sending JSONResult message", e);
							}
						}
					}
				}
			};
			muc.addMessageListener(packetListener);
		} else {
			final MessageListener messageListener = new MessageListener() {
				public void processMessage(final Chat chat, final Message message) {
					final JSONRPCResult result = ServerConnection.this.processMessage(chat, message);
					if (result != null) {
						try {
							chat.sendMessage(result.toString());
						} catch (final XMPPException e) {
							log.error("Error sending JSONResult message", e);
						}
					}
				}
			};

			xmppConnection.getChatManager().addChatListener(new ChatManagerListener() {
				public void chatCreated(final Chat chat, final boolean local) {
					// only when this is not a local chat
					// listen to one recipient only?
					if (!local && (uri.getRecipient() == null || chat.getParticipant().equals(uri.getRecipient()))) {
						chat.addMessageListener(messageListener);
					}
				}
			});
		}
	}

	/**
	 * @param context
	 * @param message
	 * @return the JSONRPCResult, or null when this message could not be processed
	 */
	protected JSONRPCResult processMessage(final Object context, final Message message) {
		final String receiveString = message.getBody();
		log.debug("receive: " + receiveString);

		JSONObject jsonRequest;
		try {
			jsonRequest = new JSONObject(receiveString);
		} catch (final JSONException e) {
			// okay
			log.debug("Ignoring non-JSON message");
			return null;
		}

		if (!jsonRequest.has("method")) {
			log.debug("Ignoring JSON message without 'method' parameter");
			return null;
		}

		final JSONRPCBridge bridge = JSONRPCBridge.getGlobalBridge();
		final JSONRPCResult jsonResult = bridge.call(new Object[] { context, message }, jsonRequest);

		return jsonResult;
	}

	/**
	 * Gets the underlying XMPPConnection
	 * @return the xmppConnection
	 */
	public XMPPConnection getXmppConnection() {
		return xmppConnection;
	}
}
