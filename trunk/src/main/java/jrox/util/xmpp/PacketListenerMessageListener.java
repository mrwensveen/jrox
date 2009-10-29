/**
 *
 */
package jrox.util.xmpp;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;

public class PacketListenerMessageListener implements MessageListener {
	private final PacketListener packetListener;

	/**
	 * @param packetListener
	 */
	public PacketListenerMessageListener(final PacketListener packetListener) {
		this.packetListener = packetListener;
	}

	public void processMessage(final Chat chat, final Message message) {
		packetListener.processPacket(message);
	}

	public PacketListener getPacketListener() {
		return packetListener;
	}
}