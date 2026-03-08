package smhdd.data;

import java.util.Objects;

public final class NumericalItem {
    
    private final int attributeIndex;
    private final Interval interval;

    public NumericalItem(int attributeIndex, Interval interval) {
        this.attributeIndex = attributeIndex;
        this.interval = interval;
    }

    public boolean contains(double value) {
        return this.interval.contains(value);
    }

    public int getAttributeIndex(){
        return this.attributeIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumericalItem)) return false;
        NumericalItem other = (NumericalItem) o;
        return this.attributeIndex == other.attributeIndex
            && java.util.Objects.equals(this.interval, other.interval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.attributeIndex, this.interval);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.interval);
        return sb.toString();
    }

    public String display(){
        StringBuilder sb = new StringBuilder();
        sb.append("(a:")
        .append(this.attributeIndex)
        .append(", v: ")
        .append(this.interval)
        .append(")");
        return sb.toString();
    }

    public static final class Interval {
        public final double lowerValue;
        public final double upperValue;
        public final boolean lowerBound; // true = closed, false = open
        public final boolean upperBound; // true = closed, false = open

        public Interval(double lowerValue, double upperValue, boolean lowerBound, boolean upperBound) {
        this.lowerValue = lowerValue;
        this.upperValue = upperValue;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        }

        private static double normalizeZero(double x) {
        return (x == 0.0d) ? 0.0d : x;       // x == 0.0 is true for both +0.0 and -0.0; return canonical +0.0
        }

        @Override
        public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Interval)) return false;
        Interval other = (Interval) o;

        return Double.compare(normalizeZero(this.lowerValue), normalizeZero(other.lowerValue)) == 0
            && Double.compare(normalizeZero(this.upperValue), normalizeZero(other.upperValue)) == 0
            && this.lowerBound == other.lowerBound
            && this.upperBound == other.upperBound;
        }

        @Override
        public int hashCode() {
        return Objects.hash(normalizeZero(lowerValue), normalizeZero(upperValue), lowerBound, upperBound);
        }

        public boolean contains(double value) {
        if (lowerValue > upperValue) return false;
        boolean lowerOk = lowerBound ? (value >= lowerValue) : (value > lowerValue);
        boolean upperOk = upperBound ? (value <= upperValue) : (value < upperValue);
        return lowerOk && upperOk;
        }

        @Override
        public String toString() {
        return (lowerBound ? "[" : "(") + lowerValue + ", " + upperValue + (upperBound ? "]" : ")");
        }
    }

    // public static void main(String[] args) {
    //     // Example 1: mixed values + duplicates (already sorted)
    //     double[] data1 = {
    //         1, 1, 1,
    //         2, 2,
    //         3,
    //         4, 4, 4, 4,
    //         5,
    //         6,
    //         7, 7,
    //         8,
    //         9,
    //         10, 10, 10, 10
    //     };

    //     // Example 2: heavy duplicates at the beginning (already sorted)
    //     double[] data2 = {
    //         0, 0, 0, 0, 0, 0, 0,
    //         1, 1, 1,
    //         2, 2,
    //         3,
    //         4, 4,
    //         5
    //     };

    //     // Example 3: all equal values (already sorted)
    //     double[] data3 = { 5, 5, 5, 5, 5, 5, 5 };

    //     int k1 = 4;
    //     int k2 = 4;

    //     runTest("DATASET 1", data1, k1);
    //     runTest("DATASET 2", data2, k2);
    //     runTest("DATASET 3 (ALL EQUAL)", data3, k1);

    //     System.out.println("# Testes com intervals");
    //     Interval i1 = new Interval(+0.0, 567.1, false, false);
    //     Interval i2 = new Interval(-0.0, 567.1, false, false);
    //     System.out.println("São iguais ? "+ i1.equals(i2));
    //     System.out.println("Hash do primeiro: "+ i1.hashCode());
    //     System.out.println("Hash do segundo:  "+ i2.hashCode());

    //     System.out.println("# Testes com numerical items");
    //     Interval i3 = new Interval(+0.0, 567.1, false, false);
    //     Interval i4 = new Interval(-0.0, 567.1, false, false);

    //     NumericalItem n1 = new NumericalItem(1, i2);
    //     NumericalItem n2 = new NumericalItem(1, i1);

    //     System.out.println("São iguais ? "+ n1.equals(n2));
    //     System.out.println("Hash do primeiro: "+ n1.hashCode());
    //     System.out.print("Hash do segundo:  "+ n2.hashCode());



    // }

    // private static void runTest(String name, double[] data, int numIntervals) {
    //     System.out.println("==================================================");
    //     System.out.println(name);
    //     System.out.println("n = " + data.length + ", numIntervals = " + numIntervals);
    //     System.out.println("min = " + data[0] + ", max = " + data[data.length - 1]);

    //     Interval[] ew = LocalDiscretization.equalWidthDiscretization(data, numIntervals);
    //     Interval[] ef = LocalDiscretization.equalFrequencyDiscretization(data, numIntervals);

    //     System.out.println("\nEqual-width intervals (" + ew.length + "):");
    //     printIntervals(ew);

    //     System.out.println("\nEqual-frequency intervals (" + ef.length + "):");
    //     printIntervals(ef);

    //     System.out.println();
    // }

    // private static void printIntervals(Interval[] intervals) {
    //     for (int i = 0; i < intervals.length; i++) {
    //         System.out.println("  " + i + ": " + intervals[i]);
    //     }
    // }

}
