package smhdd.data;
//import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class NumericalItemMemory {
    
    // Forward mapping: key -> value.
    private final Map<Integer, NumericalItem> forward;
    // Inverse mapping: value -> key.
    private final Map<NumericalItem, Integer> inverse;

    public NumericalItemMemory(int size) {
        forward = new LinkedHashMap<>(2*size,1.0f);
        inverse = new LinkedHashMap<>(2*size,1.0f);
    }

    /**
     * Associates the specified key with the specified value in this bidirectional map.
     * If the key or value already exists, the old mapping is removed.
     *
     * @param key   the key
     * @param value the value
     * @return the value associated with the key
     */
    public Integer put(Integer key, NumericalItem value) {
        Integer alreadyExistingKey = inverse.putIfAbsent(value, key);
        if(alreadyExistingKey == null){
            forward.put(key, value);
            Integer keyPut = key;
            return keyPut;
        }
        
        return alreadyExistingKey;
    }

    public Integer put(Integer key, NumericalItem value, D dataset) {
        Integer alreadyExistingKey = inverse.putIfAbsent(value, key);
        if(alreadyExistingKey == null){
            forward.put(key, value);
            Integer keyPut = key;
            dataset.addOneToItemCount();
            return keyPut;
        }
        return alreadyExistingKey;
    }

    public void remove(Integer key, D dataset) {
        // 1. Remove do mapa forward e recupera o valor
        NumericalItem value = forward.remove(key);
        
        // 2. Se o valor existia, remove do mapa inverso
        if (value != null) {
            inverse.remove(value);
        }
        
        if (dataset != null) {
            dataset.substractOneFromItemCount();
        }
    }

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key
     * @return the associated value, or null if not found
     */
    public NumericalItem getNumericalItem(int key) {
        return forward.get(key);
    }

    /**
     * Retrieves the attribute index associated with the specified item.
     *
     * @param key the key
     * @return the attribute index, or -1 if not found
     */
    public int getAttributeIndex(int key) {
        return forward.get(key).getAttributeIndex();
    }

    /**
     * Retrieves the key associated with the specified value.
     *
     * @param value the value
     * @return the associated key, or null if not found
     */
    public int getItemIndex(NumericalItem value) {
        return inverse.get(value);
    }

    /**
     * Returns the number of key-value mappings in this bidirectional map.
     *
     * @return the number of mappings
     */
    public int size() {
        return forward.size();
    }

    /**
     * Clears both the forward and inverse maps.
     */
    public void clear() {
        forward.clear();
        inverse.clear();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("NumericalItemMemory {\n");

        sb.append("  forward = {\n");
        for (Map.Entry<Integer, NumericalItem> entry : forward.entrySet()) {
            sb.append("    ")
            .append(entry.getKey())
            .append(" -> ")
            .append(entry.getValue()) // uses NumericalItem.toString()
            .append("\n");
        }
        sb.append("  }\n");

        return sb.toString();
    }

    public Map<Integer, NumericalItem> getForward(){
        return this.forward;
    }

    public Map<Integer, NumericalItem> getInverse(){
        return this.forward;
    }

    public static void main(String[] args) {
        // NumericalItemMemory biMap = new NumericalItemMemory(2000000, 0);
        // System.out.println(biMap.size());
        // biMap.put(new NumericalItem(1,4,6));
        // System.out.println(biMap.size());
        // biMap.put(new NumericalItem(2,4,6));
        // System.out.println(biMap.size());

        // biMap.put(new NumericalItem(1,4,6));
        // System.out.println(biMap.put(new NumericalItem(1,4,6)));

        // System.out.println("BiMap: " + biMap);
        // System.out.println("Key for 'Alice': " + biMap.getItemIndex( new NumericalItem(1,4,6)));
        // System.out.println("Value for 'student2': " + biMap.getNumericalItem(5));
        // System.out.println("BiMap after mapping 'Alice' to student3: " + biMap);
    }

    

}