package smhdd.data;

import java.util.Objects;

public final class Index extends Item {

    private final int valueIndex;

    public Index(int attributeIndex, int valueIndex) {
        super(attributeIndex);
        this.valueIndex = valueIndex;
    }

    @Override
    public boolean contains(double valueIndex) {
        return this.valueIndex == valueIndex;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Index index = (Index) obj;
        return super.attributeIndex == index.attributeIndex && this.valueIndex == index.valueIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.attributeIndex, this.valueIndex);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("(a:").append(super.attributeIndex).append(", v:").append(this.valueIndex).append(")");
        return sb.toString();
    }
    
}
