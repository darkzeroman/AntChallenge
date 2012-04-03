package vohra;




import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// TODO: Use some kind of custom serializer/deserializer instead of this heavy
// weight
class ObjectIO<V> {
	byte[] toByteArray(V obj) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);

		}
		return baos.toByteArray();
	}

	@SuppressWarnings("unchecked")
	V fromByteArray(byte[] bytes) {
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		V t = null;
		try {
			final ObjectInputStream ois = new ObjectInputStream(bais);
			t = (V) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return t;
	}
}
