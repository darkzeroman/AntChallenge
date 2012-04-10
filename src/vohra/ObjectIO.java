package vohra;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/*
 * Used for converting the objects to/from byte array. Pretty much standard code
 */
class ObjectIO<T> {
	public byte[] toByteArray(T obj) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream outputStream = new ObjectOutputStream(stream);
			outputStream.writeObject(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stream.toByteArray();
	}

	@SuppressWarnings("unchecked")
	public T fromByteArray(byte[] data) {
		final ByteArrayInputStream stream = new ByteArrayInputStream(data);
		T t = null;
		try {
			final ObjectInputStream inputStream = new ObjectInputStream(stream);
			t = (T) inputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return t;
	}

}
