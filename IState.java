//import java.util.BitSet;
import java.util.Set;
import java.util.TreeSet;
//import java.util.TreeSet;
public class IState extends State {
    public Set<State> delimiters;            /* List of the first non-immediate transitions by item */
    //public BitSet follow;                  /* The set of items reachable from the state */
    public IState () {
        super(true);
        delimiters = new TreeSet<State>();
      //  follow = new BitSet();
    }
    /*public BitSet getFollow()
    {
        return follow;
    }

    public void setFollow(BitSet s)
    {
        follow.or(s);
    }*/

    public Set<State> getItransitions() {
        return delimiters;
    }

    public DState Delta(int a, boolean compute){
        DState r = new DState();
        
        return r;
    }

    public void adddelimiter(State etat) {
        this.delimiters.add(etat);
    }

    public String toString() {
        return " ( "+ getType()+", "+getStart()+", "+getEnd()+"; w = "+getWeight()+"; follow: ="+getFollow() +
        " trans: "+transitions+" Map: "+delimiters+")" ;
    }
}