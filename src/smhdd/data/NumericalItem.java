package smhdd.data;

import java.util.Objects;

public final class NumericalItem {
    private final int attributeIndex;
    private final double start, end;

    public NumericalItem(int attributeIndex, double start, double end) {
        this.attributeIndex = attributeIndex;
        this.start = start;
        this.end = end;
    }

    public boolean contains(double value) {
        return value >= start && value <= end;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NumericalItem interval = (NumericalItem) obj;
        return this.attributeIndex == interval.attributeIndex && this.start == interval.start && this.end == interval.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.attributeIndex, this.start, this.end);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("(a:")
        .append(this.attributeIndex)
        .append(", v:[")
        .append(this.start)
        .append(", ")
        .append(this.end)
        .append("])");
        return sb.toString();
    }
}
