import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
//import java.util.HashSet;
// one node of DFA used in determinisation with state existence check
// Here the node records (Current Pattern ) only the last reached itemset as a BitSet
public class Node extends DfaState <BitSet> {
    
    int ref;   // reference state to optimize Delta computation

    public Node(int i) {
        Pattern  = new BitSet(); 
        if (i >= 0) Pattern.set(i);     // at each itemset separator (start a new itemset) we initialize the current motif  
        follow = new HashSet<>();
    }

    public int getRef() { return ref;}

    public void setRef(int r) { ref = r;}

    @SuppressWarnings("unchecked")

    public <T> HashMap<Integer,T> extendGlobal() {
        HashMap<Integer,T> resultat = new HashMap<>();
        int i; itemState r;
        HashMap<Integer,Integer> index = new HashMap<>();
        Set<Integer> target = (this.getStates().get(0).getStart() == 0)? WASMA.alphabet:WASMA.DFA.getTransitions(this.getRef()).keySet(); 
        for (State p:this.getStates()) {
            for(int item:p.getFollow()) {
                if (!target.contains(item)) continue; /* extend the state by i iff the root contains a transition by i */
                i = 0;
                T res =  (T) new Node(item);
                if (resultat.get(item) == null) resultat.put(item, res);
                else res = resultat.get(item);
                if (!index.containsKey(item)) index.put(item, 0);
                r = (itemState) WASMA.itemStates.get(item).get(((delimState)WASMA.itemStates.get(WASMA.itemsetDelimiter).get(p.getOrder())).map.get(item));
                while (r.getEnd() <= p.getEnd()) {
                    ((Node)res).states.add(r);
                    ((Node)res).setSupport(((itemState) r).getWeight());
                    ((Node)res).setFollow(((itemState) r).getFollow());
                    i = r.getlEnd() + 1;
                    if (i < WASMA.itemStates.get(item).size()) r = (itemState) WASMA.itemStates.get(item).get(i);
                    else break;
                }
            }
        }
        return resultat;    
    }

    @SuppressWarnings("unchecked")
    public <T> HashMap<Integer,T> extendLocal() {
        HashMap<Integer,T> resultat = new HashMap<>();
        int i, m; itemState r; boolean found;
        HashMap<Integer,Integer> index = new HashMap<>();
        Set<Integer> target = (this.getStates().get(0).getStart() == 0)? WASMA.alphabet:WASMA.DFA.getTransitions(this.getRef()).keySet(); 
        for (State p:this.getStates()) {
            for(int item:p.getFollow()) {
                if (!target.contains(item)) continue; /* extend the state by i iff the root contains a transition by i */
                i = 0;
                T res =  (T) new Node(item);
                if (resultat.get(item) == null) resultat.put(item, res);
                else res = resultat.get(item);
                if (!index.containsKey(item)) index.put(item, 0);
                if (((delimState)WASMA.itemStates.get(WASMA.itemsetDelimiter).get(p.getRoot())).map.get(item) >= index.get(item))
                    i = ((delimState)WASMA.itemStates.get(WASMA.itemsetDelimiter).get(p.getRoot())).map.get(item);
                r = (itemState) WASMA.itemStates.get(item).get(i);
                while (r.getStart() < p.getStart()) {
                    i = r.getlEnd() + 1;
                    r = (itemState) WASMA.itemStates.get(item).get(i);
                }
                while (r.getEnd() <= p.getEnd()) {
                    if (r.getRoot() == p.getRoot()) found = true;
                    else {
                        found = true;
                        for (m = this.getPattern().previousSetBit(this.getPattern().size()); found && m >= 0; found = r.getPrevious().contains(m), m = this.getPattern().previousSetBit(m - 1));
                    } 
                    if (found) {
                        ((Node) res).states.add(r);
                        ((Node)res).setSupport(((itemState) r).getWeight());
                        ((Node)res).setFollow(((itemState) r).getFollow());
                        i = r.getlEnd() + 1;
                    } else  i++;
                    if (i < WASMA.itemStates.get(item).size()) r = (itemState) WASMA.itemStates.get(item).get(i); else break;
                } 
            }
        }
        return resultat;        
    }

    @SuppressWarnings("unchecked")
    public  <T> T terminateSequence(){        //Delta(this,#) terminate a sequence by # same as global alignment (separated to save some tests)
        T res = (T) new Node(WASMA.itemsetDelimiter);
        int i; delimState r;
        for(State s:this.getStates()) {
            r = (delimState) WASMA.itemStates.get(WASMA.itemsetDelimiter).get(((itemState)s).getDelim());
            while (r.getEnd() <= s.getEnd()) {
                if (!r.getFollow().isEmpty()) {
                    ((Node)res).states.add(r);
                    ((Node)res).setFollow(((delimState) r).getFollow());
                }
                i = r.getlEnd() + 1;
                if (i < WASMA.itemStates.get(WASMA.itemsetDelimiter).size()) r = (delimState) WASMA.itemStates.get(WASMA.itemsetDelimiter).get(i);
                else break;
            }  
        }
        return res;         
    }
}