import java.util.BitSet;
import java.util.HashMap;
// One node of DFA used in determinisation with state existence check
// Here the node records (the Current Pattern ) only the last reached itemset as a BitSet
public class Node extends DfaState <BitSet> {
    
    int ref;   // reference state to optimize Delta computation
    public Node(int i) {
        Pattern  = new BitSet(); 
        Pattern.set(i);     // at each itemset separator (start a new itemset) we initialize the current motif  
    }
    public Node() { Pattern  = new BitSet(); }

    public int getRef() { return ref;}

    public void setRef(int r) { ref = r;}

    @SuppressWarnings("unchecked")

    public <T> HashMap<Integer,T> extendGlobal() {
        HashMap<Integer,T> resultat = new HashMap<>();
        int i; itemState r;
        for (State p:this.getStates()) {
            for(int item:((delimState)p).getFollow()) {
                if (!WASMA.DFA.getTransitions(this.getRef()).keySet().contains(item)) continue; /* extend the state by i iff the root contains a transition by i */
                T res =  (T) new Node(item);
                if (resultat.get(item) == null) resultat.put(item, res);
                else res = resultat.get(item);
                r = (itemState) WASMA.itemStates.get(item).get(((delimState)WASMA.itemStates.get(WASMA.itemsetDelimiter).get(p.getOrder())).map.get(item));
                int vector_size = WASMA.itemStates.get(item).size();
                while (r.getEnd() <= p.getEnd()) {
                    ((Node)res).states.add(r);
                    ((Node)res).setSupport(((itemState) r).getWeight());
                    i = r.getlEnd() + 1;
                    if (i < vector_size) r = (itemState) WASMA.itemStates.get(item).get(i);
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
        for (State p:this.getStates()) {
            for(int item = ((itemState)p).getFollow().previousSetBit(((itemState)p).getFollow().length());item > p.getItem() ; item =((itemState)p).getFollow().previousSetBit(item-1) ) {
                if (!WASMA.DFA.getTransitions(this.getRef()).keySet().contains(item) ) continue; /* extend the state by i iff the root contains a transition by i */
                i = 0;
                T res = (T) new Node(item);
                if (resultat.get(item) == null) resultat.put(item, res);
                else res = resultat.get(item);
                if (!index.containsKey(item)) index.put(item, 0);
                if (((delimState)WASMA.itemStates.get(WASMA.itemsetDelimiter).get(p.getRoot())).map.get(item) >= index.get(item))
                    i = ((delimState)WASMA.itemStates.get(WASMA.itemsetDelimiter).get(p.getRoot())).map.get(item);
                r = (itemState) WASMA.itemStates.get(item).get(i);
                int vector_size = WASMA.itemStates.get(item).size();
                while (r.getStart() < p.getStart()) {
                    i = r.getlEnd() + 1;
                    if (i < vector_size) r = (itemState) WASMA.itemStates.get(item).get(i);
                }
                while (r.getEnd() <= p.getEnd()) {
                    if (r.getRoot() == p.getRoot()) found = true;
                    else {
                        found = true;
                        for (m = this.getItem(); found && m >= 0; found = r.getFollow().get(m), m = this.getPattern().previousSetBit(m - 1));
                    } 
                    if (found) {
                        ((Node) res).states.add(r);
                        ((Node) res).setSupport(((itemState) r).getWeight());
                        i = r.getlEnd() + 1;
                    } else  i++;
                    if (i < vector_size) r = (itemState) WASMA.itemStates.get(item).get(i); else break;
                } 
            }
        }
        return resultat;        
    }

    @SuppressWarnings("unchecked")
    public  <T> T terminateSequence(){        //Delta(this,#) terminate a sequence by # same as global alignment (separated to save some tests)
        T res = (T) new Node();
        int i; delimState r;
        for(State s:this.getStates()) {
            r = (delimState) WASMA.itemStates.get(WASMA.itemsetDelimiter).get(((itemState)s).getDelim());
            int vector_size = WASMA.itemStates.get(WASMA.itemsetDelimiter).size();
            while (r.getEnd() <= s.getEnd()) {
                if (!r.getFollow().isEmpty())    ((Node)res).states.add(r);
                i = r.getlEnd() + 1;
                if (i < vector_size) r = (delimState) WASMA.itemStates.get(WASMA.itemsetDelimiter).get(i);
                else break;
            }  
        }
        return res;         
    }
}