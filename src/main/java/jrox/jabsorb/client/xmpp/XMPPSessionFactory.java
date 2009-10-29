/**
 *
 */
package jrox.jabsorb.client.xmpp;

import java.net.URI;

import jrox.util.xmpp.XMPPURI;

import org.jabsorb.client.Session;
import org.jabsorb.client.TransportRegistry;
import org.jabsorb.client.TransportRegistry.SessionFactory;
import org.jabsorb.client.async.AsyncSessionUtil;
import org.jivesoftware.smack.XMPPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthijs
 *
 */
public class XMPPSessionFactory implements SessionFactory {
	private static final Logger log = LoggerFactory.getLogger(XMPPSessionFactory.class);

	public Session newSession(final URI uri) {
		final XMPPURI xmppuri = new XMPPURI(uri);

		if (xmppuri.getRecipient() != null && !"".equals(xmppuri.getRecipient())) {
			try {
				return AsyncSessionUtil.toSyncSession(new XMPPChatSession(xmppuri));
			} catch (final XMPPException e) {
				log.error("Exception while creating XMPPChatSession", e);
			}
		} else if (xmppuri.getRoom() != null && !"".equals(xmppuri.getRoom())) {
			try {
				return AsyncSessionUtil.toSyncSession(new XMPPMultiUserChatSession(xmppuri));
			} catch (final XMPPException e) {
				log.error("Exception while creating XMPPMultiUserChatSession", e);
			}
		}

		log.error("No recipient or chatroom specified. Can't create Session");
		return null;
	}

	/**
	 * Register this transport in 'registry'
	 */
	public static void register(final TransportRegistry registry) {
		registry.registerTransport("xmpp", new XMPPSessionFactory());
	}
}
