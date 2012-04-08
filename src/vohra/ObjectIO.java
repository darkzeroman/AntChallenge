package vohra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/*
 * Used for converting the objects to/from byte array. Standard code
 */
class ObjectIO<T> {
	byte[] toByteArray(T obj) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(stream);
			oos.writeObject(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stream.toByteArray();
	}

	@SuppressWarnings("unchecked")
	T fromByteArray(byte[] data) {
		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		T t = null;
		try {
			final ObjectInputStream ois = new ObjectInputStream(stream);
			t = (T) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

}
