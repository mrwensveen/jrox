/**
 *
 */
package jrox.jabsorb.serializer.impl;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Serializes and Deserializes java.awt.Point values.
 *
 * @author matthijs
 *
 */
public class PointSerializer extends AbstractSerializer {
	private static final long serialVersionUID = 1L;

	public Class<?>[] getSerializableClasses() {
		return new Class<?>[] { Point.class };
	}

	public Class<?>[] getJSONClasses() {
		return new Class[] { JSONObject.class };
	}

	public Object marshall(final SerializerState state, final Object p, final Object o) throws MarshallException {
		if (!(o instanceof Point)) {
			throw new MarshallException("Object to marshall is not a java.awt.Point!");
		}

		final Point point = (Point) o;
		final Map<String, Integer> pointMap = new HashMap<String, Integer>();
		pointMap.put("x", point.x);
		pointMap.put("y", point.y);

		return marshallHints(new JSONObject(pointMap), point);
	}

	public ObjectMatch tryUnmarshall(final SerializerState state, final Class<?> clazz, final Object json) throws UnmarshallException {
	    final JSONObject jsonObject = (JSONObject) json;
	    final ObjectMatch match;
	    try {
			if ((jsonObject.has(JSONSerializer.JAVA_CLASS_FIELD) &&
				jsonObject.getString(JSONSerializer.JAVA_CLASS_FIELD).equals(Point.class.getName())) ||
				(jsonObject.has("x") && jsonObject.has("y") && jsonObject.length() == 2)) {
				match = ObjectMatch.OKAY;
			} else {
				if (jsonObject.has("x") || jsonObject.has("y")) {
					match = ObjectMatch.SIMILAR;
				} else {
					match = ObjectMatch.ROUGHLY_SIMILAR;
				}
			}
		} catch (final JSONException e) {
			throw new UnmarshallException("Could not get javaClass field, even though jsonObject says it has one (should not happen)", e);
		}

	    state.setSerialized(json, match);
	    return match;
	}

	public Object unmarshall(final SerializerState state, final Class<?> clazz, final Object json) throws UnmarshallException {
	    final JSONObject jsonObject = (JSONObject) json;
	    final Point point = new Point();
	    try {
		    if (jsonObject.has("x")) {
		    	point.x = jsonObject.getInt("x");
		    }
		    if (jsonObject.has("y")) {
		    	point.y = jsonObject.getInt("y");
		    }
	    } catch (final JSONException e) {
	    	throw new UnmarshallException("Could not read field", e);
	    }

	    return point;
	}
}
