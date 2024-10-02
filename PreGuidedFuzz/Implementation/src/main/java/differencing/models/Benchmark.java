package differencing.models;

public class Benchmark {
    // Index
    public String benchmark;

    // Non-Index
    public String expected;

    public Benchmark(String benchmark, String expected) {
        this.benchmark = benchmark;
        this.expected = expected;
    }
}
