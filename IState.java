import java.util.BitSet;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
public class IState extends State {
    public HashMap<Integer,Set<State>> nextMap;            /* List of the first non-immediate transitions by item */
    public BitSet previousMap;                  /* The set of items that can reach this state */
    public int weight = 1;

    public IState () {
        super(true);
        nextMap = new HashMap<>();
        previousMap = new BitSet();
    }
    
    public int getWeight(){
        return weight;
    }

    public void setWeight(int w){
        weight = w;
    }
    
    public BitSet getpreviousMap(){
        return previousMap;
    }

    public void setpreviousMap(BitSet s){
        previousMap.or(s);
    }

    public HashMap<Integer,Set<State>> getItransitions(){
        return nextMap;
    }

    public DState Delta(int a){
        DState r = new DState();
        r.getEtats().addAll(nextMap.get(a));
        return r;
    }

    public void addItem(int i, State etat) {
        Set<State> ss = new TreeSet<State>();
        if (nextMap.containsKey(i)) ss = nextMap.get(i); 
        ss.add(etat);
        this.nextMap.put(i,ss);
    }

    public String toString() {
        return " ( "+ getType()+", "+getStart()+", "+getEnd()+"; w = "+getWeight() +
        " trans: "+transitions+" Map: "+nextMap+" Previous: "+previousMap+" )" ;
    }
}