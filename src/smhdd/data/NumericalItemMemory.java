package smhdd.data;
import java.util.HashMap;
import java.util.Map;

public final class NumericalItemMemory {
    
    // Forward mapping: key -> value.
    private final Map<Integer, NumericalItem> forward;
    // Inverse mapping: value -> key.
    private final Map<NumericalItem, Integer> inverse;

    private static int itemIndex;


    public NumericalItemMemory(int size, int initialItemIndex) {
        forward = new HashMap<>(2*size,1.0f);
        inverse = new HashMap<>(2*size,1.0f);
        NumericalItemMemory.itemIndex = initialItemIndex;
    }

    /**
     * Associates the specified key with the specified value in this bidirectional map.
     * If the key or value already exists, the old mapping is removed.
     *
     * @param key   the key
     * @param value the value
     * @return the value associated with the key
     */
    public Integer put(NumericalItem value) {
        Integer alreadyExistingKey = inverse.putIfAbsent(value, itemIndex);
        if(alreadyExistingKey == null){
            forward.put(itemIndex, value);
            Integer keyPut = itemIndex;
            itemIndex =  itemIndex + 1;
            return keyPut;
        }
        
        return alreadyExistingKey;
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
        return forward.toString();
    }

    public Map<Integer, NumericalItem> getForward(){
        return this.forward;
    }

    public Map<Integer, NumericalItem> getInverse(){
        return this.forward;
    }

    // A simple test for demonstration purposes.
    public static void main(String[] args) {
        NumericalItemMemory biMap = new NumericalItemMemory(2000000, 0);
        System.out.println(biMap.size());
        biMap.put(new NumericalItem(1,4,6));
        System.out.println(biMap.size());
        biMap.put(new NumericalItem(2,4,6));
        System.out.println(biMap.size());

        biMap.put(new NumericalItem(1,4,6));
        System.out.println(biMap.put(new NumericalItem(1,4,6)));

        System.out.println("BiMap: " + biMap);
        System.out.println("Key for 'Alice': " + biMap.getItemIndex( new NumericalItem(1,4,6)));
        System.out.println("Value for 'student2': " + biMap.getNumericalItem(5));
        System.out.println("BiMap after mapping 'Alice' to student3: " + biMap);
    }

    

}
