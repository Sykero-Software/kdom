/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.util;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import kotlin.jvm.JvmClassMappingKt;

/**
 * The JVM-only parts of jdom2's {@code UnitTestUtil}; the rest moved to the common {@link UnitTestUtil} (Task 8).
 * Used by the Java tests that were not translated: {@code Class} flavoured exception checks (the common helpers take a
 * {@code KClass}) and Java serialization (no kdom class is {@code Serializable} any more; kept as in Task 7).
 * The jdom2 "Android" switch (an alternative way of attaching a cause) had no caller and was dropped.
 */
@SuppressWarnings("javadoc")
public class JvmTestUtil {

    /** Fail because an exception of type {@code expect} was expected but not thrown. */
    public static final void failNoException(Class<? extends Throwable> expect) {
        UnitTestUtil.failNoException(JvmClassMappingKt.getKotlinClass(expect));
    }

    /** Check that {@code got} is an instance of {@code expect}. */
    public static final void checkException(Class<? extends Throwable> expect, Throwable got) {
        if (expect == null) {
            org.junit.Assert.fail("We can't expect a null exception (cryptic enough?)");
        }
        UnitTestUtil.checkException(JvmClassMappingKt.getKotlinClass(expect), got);
    }

    /** Fail with {@code message}, attaching {@code got} as the cause. */
    public static void failException(String message, Throwable got) {
        UnitTestUtil.failException(message, got);
    }

    /**
	 * Serialize, then deserialize an instance object.
	 * @param tclass
	 * @param input
	 * @return
	 */
    public static final <T extends Serializable> T deSerialize(final T input) {
    	if (input == null) {
    		return null;
    	}
    	final Class<?> tclass = input.getClass();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            try {
                objectOutputStream.writeObject(input);
            }
            catch(final IOException ioException) {
                failException("Unable to serialize object.", ioException);
            }
            finally {
                try {
                    objectOutputStream.close();
                }
                catch(final IOException ioException) {
                    failException("failed to close object stream while serializing object", ioException);
                }
            }
        }
        catch(final IOException ioException) {
            failException("unable to serialize object", ioException);
        }
        finally {
            try {
                outputStream.close();
            }
            catch(final IOException ioException) {
                failException("failed to close output stream while serializing object", ioException);
            }
        }

        final byte[] bytes = outputStream.toByteArray();
        
        Object o = null;

        final InputStream inputStream = new ByteArrayInputStream(bytes);
        try {
            final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            try {
                o = objectInputStream.readObject();
            }
            finally {
                try {
                    objectInputStream.close();
                }
                catch(final IOException ioException) {
                    failException("failed to close object stream while deserializing object", ioException);
                }
            }
        }
        catch(final IOException ioException) {
        	failException("unable to deserialize object.", ioException);
        }
        catch(final ClassNotFoundException classNotFoundException) {
            failException("unable to deserialize object.", classNotFoundException);
        }
        finally {
            try{
                inputStream.close();
            }
            catch(final IOException ioException) {
                failException("failed to close output stream while serializing object.", ioException);
            }
        }
        
        assertTrue("Deserialized (non-null) object is null!", o != null);
        
        @SuppressWarnings("unchecked")
		final T t = (T)tclass.cast(o);
        
        assertTrue("Deserialized instance should not be the same instance as the pre-serialization",
        		t != input);
        
        return t;

    }
}
