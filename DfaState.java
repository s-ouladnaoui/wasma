import java.util.BitSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;
public class DfaState {   // one state of the wDFA is a set of states of the nfa 
    int item;
    HashMap<IState,TreeSet<State>> states;   // the set of states categorized by their (sub)roots
    HashMap<Integer,DfaState> transitions;   // the set of DFA transitions from this state by item
    BitSet follow;
    int support;                                                                                                                                                                                                                      

    public DfaState(int i){
        item = i;
        states = new HashMap<IState,TreeSet<State>>();
        transitions = new HashMap<Integer,DfaState>();
        follow = new BitSet(); 
    }

    public int getItem(){
        return item;
    }

    public void setItem(int i){
        item = i;
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

    public String toString(){
        return (listStates()+" Supp: "+getSupport()); 
    }

    public DfaState delta_s(int a){
        System.out.println("delta de "+this+ " par : "+a);
        DfaState res_a = new DfaState(a);                  // res_a = delta(this,a)
        DfaState res_1 = new DfaState(WAutomaton.itemsetDelimiter);                  // res_1 = delta(res_a,itemsetDelimiter) 
        BitSet br = new BitSet();                       // The bitset of the states of res for new DFAState test
        int sprt = 0;
        BitSet pr = new BitSet();                // local next items for subsequent (itemset) extensions

        Iterator<State> xit =  this.listStates().iterator();
        Iterator<State> yit =  WAutomaton.wDFAStartState.getTransitions().get(a).listStates().iterator();
        State x = xit.next();
        State y = yit.next();
        TreeSet<State> list_delimiters = new TreeSet<State>();
        do {
            if (x.getEnd() < y.getStart())  { if (xit.hasNext()) x = xit.next(); else break;}
            else if (y.getEnd() < x.getStart() || y.getStart() < x.getStart() && y.getEnd() > x.getEnd()) { if (yit.hasNext()) y = yit.next(); else break;}
            else {
                res_a.addState(y);
                list_delimiters.addAll(WAutomaton.wDFAStartState.getTransitions().get(WAutomaton.itemsetDelimiter).getStates(y.getRoot())); 
                br.set(WAutomaton.wNFAStates.indexOf(y));
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
        yit = res_a.listStates().iterator();
        Iterator<State> zit = list_delimiters.iterator();
        y = yit.next();
        State z = zit.next();
        do {
            if (y.getEnd() < z.getStart())  { if (yit.hasNext()) y = yit.next(); else break;}
            else if (z.getEnd() < y.getStart() || z.getStart() < y.getStart() && z.getEnd() > y.getEnd() ) { if (zit.hasNext()) z = zit.next(); else break;}
            else {
                if (z.getRoot() == y.getRoot()) res_1.addState(z);
                if (zit.hasNext()) z = zit.next(); else break;
            }
        } while (true);
        //======================
        State ref,s;
        Iterator<State> it = res_1.listStates().iterator();
        ref = it.next();
        pr.or(((IState) ref).getPrior());
        sprt += ((IState) ref).getWeight();
        while (it.hasNext()){
            s = (State) it.next();
            pr.or(((IState) s).getPrior());
            if (s.getStart() > ref.getEnd()) {
                ref = s;
                sprt += ((IState) ref).getWeight();
            }   
        }
        if (sprt >= WAutomaton.min_supp && !WAutomaton.wDFAStateMap.containsKey(br)){
            WAutomaton.wDFAStateMap.put(br, res_a);
            res_a.setSupport(sprt);
            res_1.setSupport(sprt);
            for (int j = pr.nextSetBit(0); j > 0; j = pr.nextSetBit(j + 1)) {
                if (j <= a) pr.clear(j); 
            }
            res_a.setFollow(pr);
        }
        return res_a;
    }

    public DfaState delta(int a){
        System.out.println("delta de "+this+ " par : "+a);
        DfaState res_a = new DfaState(a);                  // res_a = delta(this,a)
        DfaState res_1 = new DfaState(WAutomaton.itemsetDelimiter);                  // res_1 = delta(res_a,itemsetDelimiter) 
        BitSet br = new BitSet();                       // The bitset of the states of res for new DFAState test
        int sprt = 0;
        BitSet pr = new BitSet();                // local next items for subsequent (itemset) extensions
        
            for (IState r: getStates().keySet() ){
                Iterator<State> xit = getStates(r).iterator();
                Iterator<State> yit;
                if (WAutomaton.wDFAStartState.getTransitions().get(a).getStates().containsKey(r)) {
                    yit = WAutomaton.wDFAStartState.getTransitions().get(a).getStates(r).iterator();
                } else continue;
                State x = xit.next();
                State y = yit.next();
                do {
                    if (x.getEnd() < y.getStart())  { if (xit.hasNext()) x = xit.next(); else break;}
                    else if (y.getEnd() < x.getStart()) { if (yit.hasNext()) y = yit.next(); else break;}
                    else {
                        res_a.addState(y);
                        br.set(WAutomaton.wNFAStates.indexOf(y));
                        if (yit.hasNext()) y = yit.next(); else break;
                    }
                } while (true);
            }
        //=======================
        Iterator<State> yit = res_a.listStates().iterator();
        Iterator<State> zit = this.getTransitions().get(WAutomaton.itemsetDelimiter).listStates().iterator();
        State y = yit.next();
        State z = zit.next();
        do {
            if (y.getEnd() < z.getStart())  { if (yit.hasNext()) y = yit.next(); else break;}
            else if (z.getEnd() < y.getStart()) { if (zit.hasNext()) z = zit.next(); else break;}
            else {
                if (z.getRoot() == y.getRoot()) res_1.addState(z);
                if (zit.hasNext()) z = zit.next(); else break;
            }
        } while (true);
        //======================
        State ref,s;
        Iterator<State> it = res_1.listStates().iterator();
        ref = it.next();
        pr.or(((IState) ref).getPrior());
        sprt += ((IState) ref).getWeight();
        while (it.hasNext()){
            s = (State) it.next();
            pr.or(((IState) s).getPrior());
            if (s.getStart() > ref.getEnd()) {
                ref = s;
                sprt += ((IState) ref).getWeight();
            }   
        }
        if (sprt >= WAutomaton.min_supp && !WAutomaton.wDFAStateMap.containsKey(br)){
            WAutomaton.wDFAStateMap.put(br, res_a);
            res_a.setSupport(sprt);
            res_1.setSupport(sprt);
            for (int j = pr.nextSetBit(0); j > 0; j = pr.nextSetBit(j + 1)) {
                if (j <= a) pr.clear(j); 
            }
            res_a.setFollow(pr);
        }
        return res_a;
    }
}

