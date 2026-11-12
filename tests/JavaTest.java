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

    public static <E extends Number> double sum(
            Collection<? extends E> numbers
    ) {
        double result = 0.0;

        for (E number : numbers) {
            result += number.doubleValue();
        }

        return result;
    }

    public static <E> void copy(
            List<? extends E> source,
            List<? super E> destination
    ) {
        destination.addAll(source);
    }

    public static <E> void consume(E... elements) {
        for (E element : elements) {
            Objects.requireNonNull(element);
        }
    }

    public T getValue() {
        return transientValue;
    }

    public void setValue(T value) {
        this.transientValue = value;
    }

    public synchronized int increment() {
        return ++globalCounter;
    }

    public static int overloaded(int value) {
        return value;
    }

    public static long overloaded(long value) {
        return value;
    }

    public static double overloaded(double value) {
        return value;
    }

    public static String overloaded(String value) {
        return value;
    }

    public static String overloaded(Object value) {
        return String.valueOf(value);
    }

    public static void overloaded(Number value) {
        globalCounter += value.intValue();
    }

    public static void overloaded(Integer value) {
        globalCounter += value;
    }

    public static <E> void overloaded(E value, E... others) {
        globalCounter += others.length + 1;
    }

    public static void controlFlow(int value) {

        if (value < 0) {
            throw new IllegalArgumentException("Negative");
        } else if (value == 0) {
            return;
        } else {
            globalCounter += value;
        }

        switch (value % 5) {
            case 0 -> globalCounter++;
            case 1 -> globalCounter += 2;
            case 2, 3 -> globalCounter += 3;
            default -> globalCounter--;
        }

        int result = switch (value) {
            case 1 -> 10;
            case 2 -> 20;
            case 3, 4 -> 30;
            default -> {
                int temporary = value * 2;
                yield temporary;
            }
        };

        globalCounter += result;

        for (int i = 0; i < 10; i++) {
            if (i == 3) continue;
            if (i == 8) break;
            globalCounter += i;
        }

        int j = 0;
        while (j++ < 5) {
            globalCounter++;
        }

        do {
            globalCounter--;
        } while (globalCounter < 0);

        outer:
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                if (x + y > 6) break outer;
            }
        }

        assert globalCounter >= Integer.MIN_VALUE;
    }

    public static String patternMatching(Object object) {
        return switch (object) {
            case null -> "null";
            case String s when !s.isBlank() -> s.toUpperCase();
            case String s -> "empty";
            case Integer i when i > 0 -> "positive:" + i;
            case Integer i -> "non-positive:" + i;
            case Long l -> "long:" + l;
            case Double d -> "double:" + d;
            case List<?> list -> "list:" + list.size();
            case Map<?, ?> map -> "map:" + map.size();
            default -> object.getClass().getName();
        };
    }

    public static void patternInstanceof(Object object) {
        if (object instanceof String text && !text.isEmpty()) {
            System.out.println(text);
        }

        if (object instanceof Number number) {
            System.out.println(number.doubleValue());
        }

        if (!(object instanceof Serializable)) {
            throw new IllegalArgumentException();
        }
    }

    public static void exceptionHandling() {

        try {
            int value = Integer.parseInt("42");
            globalCounter += value;
        } catch (NumberFormatException | NullPointerException exception) {
            globalCounter--;
        } catch (RuntimeException exception) {
            throw exception;
        } finally {
            globalCounter++;
        }

        try (ByteArrayInputStream input =
                     new ByteArrayInputStream(new byte[]{1, 2, 3});
             ByteArrayOutputStream output =
                     new ByteArrayOutputStream()) {

            input.transferTo(output);

        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    public static void resources() throws Exception {
        try (AutoCloseable first = () -> globalCounter++;
             AutoCloseable second = () -> globalCounter += 2) {
            globalCounter += 3;
        }
    }

    public static void lambdaExpressions() {

        Function<Integer, Integer> square = x -> x * x;

        BiFunction<Integer, Integer, Integer> add =
                (left, right) -> left + right;

        Supplier<String> supplier = String::new;

        Consumer<Object> printer = System.out::println;

        Comparator<String> comparator =
                String::compareToIgnoreCase;

        Function<String, Integer> parser =
                Integer::parseInt;

        IntUnaryOperator composed =
                ((IntUnaryOperator) x -> x + 1)
                        .andThen(x -> x * 2)
                        .compose(x -> x - 1);

        int result = square.apply(4) + add.apply(2, 3)
                + composed.applyAsInt(5);

        printer.accept(supplier.get());
        comparator.compare("alpha", "beta");
        parser.apply("123");

        globalCounter += result;
    }

    public static void streams() {

        List<Integer> numbers = IntStream.rangeClosed(1, 100)
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .boxed()
                .sorted(Comparator.reverseOrder())
                .toList();

        Map<Boolean, List<Integer>> partitioned =
                numbers.stream().collect(
                        Collectors.partitioningBy(n -> n > 1000)
                );

        Map<Integer, List<Integer>> grouped =
                numbers.stream().collect(
                        Collectors.groupingBy(n -> n % 10)
                );

        Optional<Integer> maximum =
                numbers.stream().max(Integer::compareTo);

        int total = numbers.stream()
                .mapToInt(Integer::intValue)
                .sum();

        grouped.forEach((key, value) ->
                globalCounter += key + value.size());

        partitioned.values().forEach(List::size);

        maximum.ifPresent(value -> globalCounter += value);

        globalCounter += total;
    }

    public static void reflection() throws ReflectiveOperationException {

        Class<?> type = JavaTest.class;

        Constructor<?> constructor = type.getConstructor();

        Method[] methods = type.getDeclaredMethods();

        Field[] fields = type.getDeclaredFields();

        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                method.getReturnType();
                method.getParameterTypes();
                method.getGenericReturnType();
            }
        }

        for (Field field : fields) {
            field.getType();
            field.isSynthetic();
        }

        constructor.getParameterCount();
    }

    public static void concurrency() throws InterruptedException {

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        CountDownLatch latch = new CountDownLatch(2);

        AtomicInteger counter = new AtomicInteger();

        Runnable task = () -> {
            counter.incrementAndGet();
            latch.countDown();
        };

        try {
            executor.submit(task);
            executor.submit(task);

            latch.await();

        } finally {
            executor.shutdown();
        }

        globalCounter += counter.get();
    }

    public static void generics() {

        List<? extends Number> upper = List.of(1, 2, 3);

        List<? super Integer> lower = new ArrayList<Number>();

        lower.add(10);

        Number number = upper.getFirst();

        Object object = lower.getFirst();

        List<String> strings = new ArrayList<>();

        strings.add("one");

        List<?> unknown = strings;

        unknown.forEach(System.out::println);

        List raw = new ArrayList();

        raw.add("unchecked");

        List<String> unchecked = raw;

        unchecked.add("conversion");

        globalCounter += number.intValue()
                + object.hashCode()
                + unchecked.size();
    }

    public static void arrays() {

        int[] primitive = {1, 2, 3, 4};

        int[][] matrix = {
                {1, 2},
                {3, 4}
        };

        String[] strings = new String[10];

        Object[] covariant = strings;

        try {
            covariant[0] = 42;
        } catch (ArrayStoreException exception) {
            globalCounter++;
        }

        int[][] jagged = new int[3][];

        jagged[0] = new int[]{1};
        jagged[1] = new int[]{2, 3};
        jagged[2] = new int[]{4, 5, 6};

        globalCounter += primitive.length
                + matrix.length
                + jagged[2][2];
    }

    public static void numericConversions() {

        byte b = (byte) 257;
        short s = (short) 65537;
        char c = (char) 65_535;

        int i = b + s + c;
        long l = i * 1_000_000L;

        float f = l / 3.0f;
        double d = f + 0.1;

        double nan = Double.NaN;
        double infinity = Double.POSITIVE_INFINITY;

        boolean comparison = nan != nan
                && infinity > Double.MAX_VALUE;

        globalCounter += comparison ? (int) d : 0;
    }

    public static void strings() {

        String escaped = "quotes: \"  slash: \\  newline: \n";

        String formatted = String.format(
                Locale.ROOT,
                "%1$s = %2$d",
                "value",
                42
        );

        String joined = String.join(
                "::",
                "alpha",
                "beta",
                "gamma"
        );

        String unicode = "\u03BB\u03A9\u4E2D";

        StringBuilder builder = new StringBuilder()
                .append(escaped)
                .append(formatted)
                .append(joined)
                .append(unicode);

        globalCounter += builder.length();
    }

    public static void synchronization(Object lock) {

        synchronized (lock) {
            globalCounter++;
        }

        synchronized (JavaTest.class) {
            globalCounter += 2;
        }
    }

    public static void localClasses() {

        class Local {
            private final int value;

            Local(int value) {
                this.value = value;
            }

            int calculate() {
                return value * value;
            }
        }

        Local local = new Local(12);

        Runnable anonymous = new Runnable() {
            private int count = 0;

            @Override
            public void run() {
                count++;
                globalCounter += count;
            }
        };

        anonymous.run();

        globalCounter += local.calculate();
    }

    public static void nestedTypes() {

        Point point = new Point(10, 20);

        Shape shape = new Circle(5.0);

        if (shape instanceof Circle circle) {
            globalCounter += (int) circle.area();
        }

        point.translate(1, 1);
    }

    public static void functionalComposition() {

        Function<Integer, Integer> first = x -> x + 10;
        Function<Integer, Integer> second = x -> x * 2;

        Function<Integer, Integer> composed =
                first.andThen(second);

        Function<Integer, Integer> reversed =
                first.compose(second);

        Predicate<Integer> positive = x -> x > 0;
        Predicate<Integer> even = x -> x % 2 == 0;

        Predicate<Integer> combined =
                positive.and(even).negate();

        globalCounter += composed.apply(5);
        globalCounter += reversed.apply(5);
        globalCounter += combined.test(3) ? 1 : 0;
    }

    public static void genericInference() {

        var inferred = List.of("alpha", "beta", "gamma");

        var map = Map.of(
                "one", 1,
                "two", 2,
                "three", 3
        );

        var result = inferred.stream()
                .map(String::length)
                .toList();

        var entry = Map.entry("answer", 42);

        globalCounter += result.stream()
                .mapToInt(Integer::intValue)
                .sum();

        globalCounter += map.getOrDefault("four", 4);
        globalCounter += entry.getValue();
    }

    public static void edgeCases() {

        int binary = 0b1010_1010;
        int octal = 01234567;
        int hexadecimal = 0xCAFE_BABE;

        long large = 9_223_372_036_854_775_807L;

        boolean bitwise = (binary & octal) != 0
                | (hexadecimal ^ binary) != 0;

        int shifted = hexadecimal >>> 4;

        boolean logical = true && false || !false;

        int ternary = logical ? 1 : 0;

        globalCounter += shifted + ternary
                + (bitwise ? 1 : 0)
                + Long.hashCode(large);
    }

    public static void main(String[] args) {

        JavaTest<Integer> test =
                new JavaTest<>();

        test.setValue(42);

        controlFlow(10);
        exceptionHandling();
        lambdaExpressions();
        streams();
        generics();
        arrays();
        numericConversions();
        strings();
        localClasses();
        nestedTypes();
        functionalComposition();
        genericInference();
        edgeCases();

        System.out.println("Counter: " + globalCounter);
        System.out.println("Value: " + test.getValue());
        System.out.println("Pattern: " + patternMatching(List.of(1, 2, 3)));
        System.out.println("Text: " + TEXT);
    }

    @Override
    public JavaTest<T> clone() {
        try {
            @SuppressWarnings("unchecked")
            JavaTest<T> cloned =
                    (JavaTest<T>) super.clone();

            return cloned;

        } catch (CloneNotSupportedException exception) {
            throw new AssertionError(exception);
        }
    }

    public sealed interface Shape
            permits Circle, Rectangle, Polygon {

        double area();

        default boolean isValid() {
            return Double.isFinite(area()) && area() >= 0;
        }

        private static double square(double value) {
            return value * value;
        }

        static Shape unitCircle() {
            return new Circle(1.0);
        }
    }

    public static final class Circle implements Shape {
        private final double radius;

        public Circle(double radius) {
            this.radius = radius;
        }

        @Override
        public double area() {
            return Math.PI * Shape.square(radius);
        }
    }

    public static non-sealed class Rectangle implements Shape {
        protected final double width;
        protected final double height;

        public Rectangle(double width, double height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public double area() {
            return width * height;
        }
    }

    public static final class Polygon extends Rectangle {

        public Polygon(double width, double height) {
            super(width, height);
        }

        @Override
        public double area() {
            return super.area() / 2.0;
        }
    }

    public record Point(int x, int y)
            implements Serializable {

        public Point {
            if (x < 0 || y < 0) {
                throw new IllegalArgumentException();
            }
        }

        public Point translate(int dx, int dy) {
            return new Point(x + dx, y + dy);
        }

        public double distance() {
            return Math.hypot(x, y);
        }
    }

    public enum State {

        NEW(0),
        RUNNING(1),
        PAUSED(2),
        FINISHED(3);

        private final int code;

        State(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }

        public static State fromCode(int code) {
            return Arrays.stream(values())
                    .filter(state -> state.code == code)
                    .findFirst()
                    .orElseThrow();
        }
    }

    public static final class Box<E> {

        private E value;

        public Box(E value) {
            this.value = value;
        }

        public E get() {
            return value;
        }

        public void set(E value) {
            this.value = value;
        }

        public <R> Box<R> map(Function<? super E, ? extends R> mapper) {
            return new Box<>(mapper.apply(value));
        }

        public Optional<E> optional() {
            return Optional.ofNullable(value);
        }
    }

    public interface Processor<I, O> {

        O process(I input);

        default <R> Processor<I, R> andThen(
                Function<? super O, ? extends R> next
        ) {
            return input -> next.apply(process(input));
        }

        static <T> Processor<T, T> identity() {
            return input -> input;
        }
    }
}
