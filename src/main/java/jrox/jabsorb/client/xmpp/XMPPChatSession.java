/**
 *
 */
package jrox.jabsorb.client.xmpp;

import jrox.util.xmpp.PacketListenerMessageListener;
import jrox.util.xmpp.XMPPURI;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthijs
 */
public class XMPPChatSession extends XMPPSession {
	private static final Logger log = LoggerFactory.getLogger(XMPPChatSession.class);

	private final Chat chat;

	/**
	 * Opens and connects a new XMPPChatSession.
	 * @throws XMPPException
	 */
	public XMPPChatSession(final XMPPURI uri) throws XMPPException {
		super(uri);
		this.chat = getXmppConnection().getChatManager().createChat(uri.getRecipient(), null);

		log.debug("Created chat with recipient " + uri.getRecipient());
	}

	@Override
	protected void sendMessage(final String message) throws XMPPException {
		chat.sendMessage(message);
	}

	@Override
	protected void addMessageListener(final PacketListener listener) {
		chat.addMessageListener(new PacketListenerMessageListener(listener));

	}

	@Override
	protected void removeMessageListener(final PacketListener listener) {
		for (final MessageListener messageListener : chat.getListeners()) {
			if (messageListener instanceof PacketListenerMessageListener &&
					((PacketListenerMessageListener)messageListener).getPacketListener() == listener) {
				chat.removeMessageListener(messageListener);
			}
		}

	}

	/**
	 * @return the chat
	 */
	public Chat getChat() {
		return chat;
	}
}
