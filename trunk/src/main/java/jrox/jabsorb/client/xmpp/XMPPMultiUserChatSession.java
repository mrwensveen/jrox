/**
 *
 */
package jrox.jabsorb.client.xmpp;

import jrox.util.xmpp.XMPPURI;
import jrox.util.xmpp.XMPPUtil;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthijs
 */
public class XMPPMultiUserChatSession extends XMPPSession {
	/**
	 * The logger for this class
	 */
	private static final Logger log = LoggerFactory.getLogger(XMPPMultiUserChatSession.class);

	private final MultiUserChat multiUserChat;
	/**
	 * Opens and connects a new XMPPChatSession.
	 * @throws XMPPException
	 */
	public XMPPMultiUserChatSession(final XMPPURI uri) throws XMPPException {
		super(uri);

		final MultiUserChat muc = new MultiUserChat(getXmppConnection(), uri.getRoom());
		// XXX: Should MultiUserChat.getHostedRooms / getRoomInfo be used?

		final String nickname = XMPPUtil.getNickname(uri);
		try {
			// we're not interested in any history
			final DiscussionHistory history = new DiscussionHistory();
			history.setMaxChars(0);

			// attemp to join (within 60 seconds)
			muc.join(nickname, uri.getRoomPassword(), history, 60000);

			log.debug("Joined room " + uri.getRoom() + " with nickname " + nickname);
		} catch (final XMPPException xmppException) {
			if (xmppException.getXMPPError() != null && XMPPError.Condition.item_not_found.toString().equals(xmppException.getXMPPError().getCondition())) {
				// attemt to create
				muc.create(nickname);

				log.debug("Created room " + uri.getRoom() + " with nickname " + nickname);
				// configure the room?
			} else {
				throw xmppException;
			}
		}
		this.multiUserChat = muc;
	}

	@Override
	protected void sendMessage(final String message) throws XMPPException {
		multiUserChat.sendMessage(message);
	}

	@Override
	protected void addMessageListener(final PacketListener listener) {
		multiUserChat.addMessageListener(listener);
	}

	@Override
	protected void removeMessageListener(final PacketListener listener) {
		multiUserChat.removeMessageListener(listener);
	}

	/**
	 * @return the multiUserChat
	 */
	public MultiUserChat getMultiUserChat() {
		return multiUserChat;
	}
}
