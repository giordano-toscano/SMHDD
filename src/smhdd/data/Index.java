package smhdd.data;

import java.util.Objects;

public final class Index extends Item {
    private final int value;

    public Index(int index, int value) {
        super(index);
        this.value = value;
    }

    @Override
    public boolean contains(double value) {
        return this.value == value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Index index = (Index) obj;
        return super.attributeIndex == index.attributeIndex && this.value == index.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.attributeIndex, this.value);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("(a:").append(super.attributeIndex).append(", v:").append(this.value).append(")");
        return sb.toString();
    }
    
}
