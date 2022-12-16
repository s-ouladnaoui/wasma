import java.util.*;
/* a State of the Deterministic equivalent of our WA.
 It contains a set of states, a pattern and the associated support */
public class DfaState  extends DState {
    List<Integer> pattern;
    TreeSet<State> delimiters;
    TreeSet<State> rest;
   // BitSet follow;       // to predict the next items

    public DfaState() {
        super();
        pattern = new ArrayList<Integer>();
        delimiters = new TreeSet<State>();
        rest = new TreeSet<>();
        //follow = new BitSet();
    }

    public Set<State> getRest() {
        return rest;
    }

    public TreeSet<State> getDelimiters() {
        return delimiters;
    }

    public void setDelimiters(Set<State> p) {
         delimiters.addAll(p);
    }

    public List<Integer> getPattern() {
        return pattern;
    }

    public void extendPattern(int item)
    {
        pattern.add(item);
    }

    public void setPattern(List<Integer> p)
    {
        pattern.addAll(p);
    }

    //public BitSet getFollow() { return follow;}

    //public void setFollow(BitSet b) { follow.or(b);}

    public String toString() {
        return getPattern()+" : "+getSupport()+"\n";
    }
}