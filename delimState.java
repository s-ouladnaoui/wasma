import java.util.HashMap;
/* Weighted NFA delimiter State Class */
import java.util.Set;

public class delimState extends State {
    
    HashMap<Integer,Integer> map;
    
    public delimState() {
        map = new HashMap<>();      // per item the position of the first occurrence of a in itemStates_a
    }
    
    public int getItem() { return WASMA.itemsetDelimiter; }  // the item is implicit #
    
    public boolean getType() { return true; }   /* flag: the state is an itemset delimiter when is true*/

    public Set<Integer> getFollow() { return map.keySet();}

}