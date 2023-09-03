import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;
// one state of the wDFA is a set of states of the nfa used in the queue of the deteminizatiob module
public class DfaState {   
    ArrayList<Integer> pattern; 
    HashMap<IState,TreeSet<State>> states;   // the set of states categorized by their (sub)roots
    HashMap<Integer,DfaState> transitions;   // the set of DFA transitions from this state by item
    DfaState root;
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(){
        states = new HashMap<IState,TreeSet<State>>();
        transitions = new HashMap<Integer,DfaState>();
        follow = new BitSet(); 
        pattern =  new ArrayList<Integer>();
        root = null;
    }

    public int getItem(){
        return pattern.get(pattern.size()-1);
    }

    public ArrayList<Integer> getPattern() {
        return pattern;
    }

    public void setPattern(int i) {
        pattern.add(i);
    }

    public HashMap<IState,TreeSet<State>> getStates(){
        return states;
    }

    public TreeSet<State> getStates(IState r){
        return states.get(r);
    }

    public TreeSet<State> listStates(){
        TreeSet<State> r = new TreeSet<State>();
        for (IState rt: this.getStates().keySet() ){       
            r.addAll(getStates(rt));      
        }
        return r;
    }
    
    public void addState(State s){
        if (states.containsKey(s.getRoot())) {
            states.get(s.getRoot()).add(s);
        }else {
            TreeSet<State> ss = new TreeSet<State>();
            ss.add(s);
            states.put(s.getRoot(), ss);
        }
        if (s.getType()) this.setFollow(((IState) s).getFollow());
    }

    public void removeState(State s) {
        if (states.containsKey(s.getRoot())) {
            states.get(s.getRoot()).remove(s);
        }
    }

    public int getSupport(){
        return support;
    }

    public void setSupport(int sprt){
        support += sprt;
    }

    public BitSet getFollow(){
        return follow;
    }

    public void setFollow(BitSet b){
        follow.or(b);
    }
    
    public HashMap<Integer,DfaState> getTransitions(){
        return transitions;
    }

    public void addTransition(int i, DfaState s){
        transitions.put(i, s);
    }

    public DfaState getRoot(){
        return root;
    }

    public void setRoot(DfaState r){
        root = r;
    }

    public String toString(){
        return (listStates().toString());
    }
    
    public DfaState trim() {
        if (listStates().size() <= 1) return this ;
        Iterator<State> it = listStates().iterator();
        State ref = it.next(),s;
        while (it.hasNext()){
            s = (State) it.next();
            if (s.getStart() > ref.getEnd()) {
                ref = s;
            } else removeState(s);      
        }
        return this;
    }

    // for reachability between two sets of states align them and check the descendance relation 

    public void Align(Iterator<State> xit, Iterator<State> yit) {
        State x = xit.next();
        State y = yit.next();
        do {
            if (x.getEnd() < y.getStart())  { if (xit.hasNext()) x = xit.next(); else break;}
            else if (y.getEnd() < x.getStart() ||
                     x.getStart() > y.getStart() && x.getEnd() < y.getEnd()) 
                        { if (yit.hasNext()) y = yit.next(); else break;}
                else {
                    this.addState(y);
                    if (yit.hasNext()) y = yit.next(); else break;
                }
            } while (true);
            //this.getFollow().and(WAutomaton.fItems);
    }

    // compute the support of a set of IStates and collect the local next items in bs. A state that doesn't contribute 
    // to further extension is removed also from the stateset
    private int evalSupport(BitSet bs, boolean target) {
        Iterator<State> it = this.listStates().iterator();
        if (!it.hasNext()) return 0;
        int sprt = 0;
        State ref = it.next(),s;
        if (target) bs.or(((IState) ref).getPrior());
        else sprt += ref.getWeight();
        while (it.hasNext()){
            s = (State) it.next();
            if (target) bs.or(((IState) s).getPrior());
            if (s.getStart() > ref.getEnd()) {
                ref = s;
                if (!target) sprt += ref.getWeight();
            }  
        }
        bs.and(WAutomaton.fItems);
        return sprt;
    } 

    // clear already processed local next items 
    public BitSet clearBs(BitSet bs) {
        int i = this.getItem();
        for (int j = bs.nextSetBit(0); j > 0; j = bs.nextSetBit(j + 1)) {
            if (j <= i) bs.clear(j); 
        }
        return bs;
    }    

    public DfaState delta(int a, DfaState ref) {
        //System.out.print(this+ " "+a+" = ");
        DfaState res = new DfaState();                          // res_a = delta(this,a)
        BitSet pr = new BitSet();                               // local next items for subsequent (itemset) extensions
        if (a == WAutomaton.itemsetDelimiter)  {
            for (IState r: getStates().keySet() ){
                if (!ref.getTransitions().containsKey(WAutomaton.itemsetDelimiter)) 
                    res.Align(this.getStates(r).iterator(), WAutomaton.wDFAStartState.getTransitions().get(a).getStates(r).iterator());            
                else res.Align(this.getStates(r).iterator(), ref.getTransitions().get(a).getStates(r).iterator());            
            }
        res.evalSupport(pr,true);
        if (!pr.isEmpty()) this.setFollow(this.clearBs(pr));
        }
        else {
            if (this.getItem() == WAutomaton.itemsetDelimiter ) {
                if (this.getRoot().getTransitions().containsKey(a)) res.Align(this.trim().listStates().iterator(), this.getRoot().getTransitions().get(a).listStates().iterator());     
            } else {
                for (IState r: getStates().keySet() ){
                    if (!this.getRoot().getTransitions().get(a).getStates().containsKey(r)) continue;
                    res.Align(this.getStates(r).iterator(), this.getRoot().getTransitions().get(a).getStates(r).iterator());
                }  
            }
            res.setSupport(res.evalSupport(pr,false));
        }
        return res;
    }
}

