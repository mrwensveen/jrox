package jrox.util.xmpp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class to simplify the creation of RFC5122 URI's
 * @author matthijs
 *
 */
public class XMPPURI {
	private String host;
	private int port;
	private String user;
	private String userPassword;
	private String recipient;
	private String room;
	private String roomPassword;

	public XMPPURI(final String uri) {
		this(URI.create(uri));
	}

	public XMPPURI(final URI uri) {
		this.host = uri.getHost();
		this.port = uri.getPort();

		final String userInfo = uri.getUserInfo();
		if (userInfo != null) {
			final String[] userPass = userInfo.split(":", 2);
			this.user = userPass[0];
			if (userPass.length == 2) {
				this.userPassword = userPass[1];
			}
		}

		final String path = uri.getPath().replaceFirst("/", "");
		final String query = uri.getQuery();
		if (query != null) {
			final List<String> parameters = Arrays.asList(query.split(";"));
			if (parameters.contains("join")) {
				// http://xmpp.org/extensions/xep-0045.html#registrar-querytypes
				this.room = path;

				for (final String param : parameters) {
					if (param.startsWith("password=")) {
						this.roomPassword = param.replaceFirst("password=", "");
					}
				}
			}
		}

		if (this.room == null) {
			if (path != null) {
				this.recipient = path;
			}
		}
	}

	public XMPPURI() {
	}

	public URI toURI() throws URISyntaxException {
		final StringBuilder userInfo = new StringBuilder();
		if (user != null) {
			userInfo.append(user);
			if (userPassword != null) {
				userInfo.append(":").append(userPassword);
			}
		}

		String path;
		final StringBuilder queryBuilder = new StringBuilder();
		if (recipient != null) {
			path = recipient;
		} else if (room != null) {
			path = room;
			queryBuilder.append("join");
			if (roomPassword != null) {
				queryBuilder.append(";password=").append(roomPassword);
			}
		} else {
			path = null;
		}

		final String query = queryBuilder.length() > 0 ? queryBuilder.toString() : null;
		return new URI("xmpp", userInfo.toString(), host, port, path, query, null);
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(final String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(final int port) {
		this.port = port;
	}

	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(final String user) {
		this.user = user;
	}

	/**
	 * @return the userPassword
	 */
	public String getUserPassword() {
		return userPassword;
	}

	/**
	 * @param userPassword the userPassword to set
	 */
	public void setUserPassword(final String userPassword) {
		this.userPassword = userPassword;
	}

	/**
	 * @return the recipient
	 */
	public String getRecipient() {
		return recipient;
	}

	/**
	 * @param recipient the recipient to set
	 */
	public void setRecipient(final String recipient) {
		this.recipient = recipient;
	}

	/**
	 * @return the room
	 */
	public String getRoom() {
		return room;
	}

	/**
	 * @param room the room to set
	 */
	public void setRoom(final String room) {
		this.room = room;
	}

	/**
	 * @return the roomPassword
	 */
	public String getRoomPassword() {
		return roomPassword;
	}

	/**
	 * @param roomPassword the roomPassword to set
	 */
	public void setRoomPassword(final String roomPassword) {
		this.roomPassword = roomPassword;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof XMPPURI)) {
			return false;
		}
		final XMPPURI other = (XMPPURI) obj;
		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		if (recipient == null) {
			if (other.recipient != null) {
				return false;
			}
		} else if (!recipient.equals(other.recipient)) {
			return false;
		}
		if (room == null) {
			if (other.room != null) {
				return false;
			}
		} else if (!room.equals(other.room)) {
			return false;
		}
		if (roomPassword == null) {
			if (other.roomPassword != null) {
				return false;
			}
		} else if (!roomPassword.equals(other.roomPassword)) {
			return false;
		}
		if (user == null) {
			if (other.user != null) {
				return false;
			}
		} else if (!user.equals(other.user)) {
			return false;
		}
		if (userPassword == null) {
			if (other.userPassword != null) {
				return false;
			}
		} else if (!userPassword.equals(other.userPassword)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + port;
		result = prime * result
				+ ((recipient == null) ? 0 : recipient.hashCode());
		result = prime * result + ((room == null) ? 0 : room.hashCode());
		result = prime * result
				+ ((roomPassword == null) ? 0 : roomPassword.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result
				+ ((userPassword == null) ? 0 : userPassword.hashCode());
		return result;
	}

	@Override
	public String toString() {
		try {
			return toURI().toString();
		} catch (final URISyntaxException e) {
			return super.toString();
		}
	}
}
