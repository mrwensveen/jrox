package jrox.util.xmpp;

public class XMPPUtil {
	public static String getNickname(final XMPPURI uri) {
		return uri.getUser().replaceFirst("@.*", "");
	}
}
