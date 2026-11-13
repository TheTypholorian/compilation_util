import java.io.*;
import java.lang.annotation.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.math.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

@SuppressWarnings({"unchecked", "rawtypes", "serial"})
public class JavaTest<T extends Number & Comparable<T>>
        implements Serializable, Cloneable, Iterable<T> {

    private static final long serialVersionUID = 1L;

    public static final double GOLDEN_RATIO = 1.6180339887498948482;
    // do comments work?
    public static final String TEXT = """
            A multiline text block containing:
                indentation,
                escapes: \n \t \" \\
                Unicode: λ Ω 漢字 🚀
            """;

    private static volatile int globalCounter;
    private transient T transientValue;// can I put a comment here?
    protected final Map<String, List<? extends T>> values;

    private final class Inner {
        private final T value;

        Inner(T value) {
            this.value = value;
        }

        T get() {
            return value;
            // or a comment here?
        }
    }

    public JavaTest() {
        this(/* or even a comment here? */new HashMap<>());
    }

    public JavaTest(Map<String, List<? extends T>> values) {
        this.values = Objects.requireNonNull(values);
    }

    static {
        globalCounter = 0;
    }

    {
        globalCounter++;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.emptyIterator();
    }

    public static <E extends Comparable<? super E>> E maximum(E first, E second) {
        return first.compareTo(second) >= 0 ? first : second;
    }

    public static <E> List<E> combine(
            E first,
            E second,
            E... remaining
    ) {
        List<E> result = new ArrayList<>();
        result.add(first);
        result.add(second);
        Collections.addAll(result, remaining);
        return result;
    }

    public static <E> E identity(E value) {
        return value;
    }
}