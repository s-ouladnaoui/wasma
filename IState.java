import java.util.BitSet;
import java.util.HashMap;
import java.util.TreeSet;

public class IState extends State  implements Comparable<State> {
    public HashMap<Integer, TreeSet<State>> itransitions;     /* List of the set of the first non-immediate transitions by item */
    public BitSet follow;                                         /* The set of items reachable from the state */
    public IState () {
        type = true;
        itransitions = new HashMap<>();
        follow = new BitSet();
    }
    public BitSet getFollow()
    {
        return follow;
    }

    public void setFollow(BitSet s)
    {
        follow.or(s);
    }

    public HashMap<Integer, TreeSet<State>> getItransitions() {
        return itransitions;
    }

    public void additransition(int item, State etat) {
        if (itransitions == null) {
            this.itransitions = new HashMap<>();
            TreeSet<State> ss = new TreeSet<>();
            ss.add(etat);
            this.itransitions.put(item,ss);
        }
        else if(this.itransitions.containsKey(item) && !this.itransitions.get(item).contains(etat)) {
            this.itransitions.get(item).add(etat);
        } else {
            TreeSet<State> ss = new TreeSet<>();
            ss.add(etat);
            this.itransitions.put(item,ss);
        }
    }

    public String toString() {
        return " ( "+ getType()+", "+getStart()+", "+getEnd()+"; w = "+getWeight()+"; follow: ="+getFollow() +
        " trans: "+transitions+" Map: "+itransitions+")" ;
    }
}