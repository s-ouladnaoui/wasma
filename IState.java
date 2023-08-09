import java.util.BitSet;
public class IState extends State {
    public BitSet previousLocalItems, nextGlobalItems;                 /* The set of items that can reach this state */
    public int weight = 1;

    public IState () {
        super(true);
        previousLocalItems = new BitSet(); 
        nextGlobalItems = new BitSet();
    }
    
    public int getWeight(){
        return weight;
    }

    public void setWeight(int w){
        weight = w;
    }
    
    public BitSet getpreviousLocalItems(){
        return previousLocalItems;
    }

    public void setpreviousLocalItems(BitSet s){
        previousLocalItems.or(s);
    }

    public String toString() {
        return " ( "+ getType()+", "+getStart()+", "+getEnd()+"; w = "+getWeight() +
        " nextItems: "+nextGlobalItems+" Previous: "+previousLocalItems+" )" ;
    }
}