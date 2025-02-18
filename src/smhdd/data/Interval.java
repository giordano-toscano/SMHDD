package smhdd.data;

import java.util.Objects;

public final class Interval extends Item {
    private final double start, end;

    public Interval(int index, double start, double end) {
        super(index);
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean contains(double value) {
        return value >= start && value <= end;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Interval interval = (Interval) obj;
        return super.attributeIndex == interval.attributeIndex && this.start == interval.start && this.end == interval.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.attributeIndex, this.start, this.end);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("(a:")
        .append(super.attributeIndex)
        .append(", v:[")
        .append(this.start)
        .append(", ")
        .append(this.end)
        .append("])");
        return sb.toString();
    }
}
