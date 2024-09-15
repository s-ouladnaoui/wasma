import java.util.ArrayList;
import java.util.HashMap;
// one node of DFA used in determinisation without state existance check
// Note that the node records the current sequential pattern as an ArrayList of Integers (Items)
public class PNode extends DfaState <ArrayList<Integer>> {

    public PNode() { 
        Pattern = new ArrayList<Integer>();
    }

    @SuppressWarnings("unchecked")
    public <T> HashMap<Integer,T> extendGlobal() {
        HashMap<Integer,T> resultat = new HashMap<>();
        int i; itemState r;
        for (State p:this.getStates()) {
            for(int item:((delimState)p).getFollow()) {
                T res =  (T) new PNode();
                if (resultat.get(item) == null) resultat.put(item, res);
                else res = resultat.get(item);
                r = (itemState) WASMA.itemStates.get(item).get(((delimState)WASMA.itemStates.get(WASMA.itemsetDelimiter).get(p.getOrder())).map.get(item));
                while (r.getEnd() <= p.getEnd()) {
                    ((PNode)res).states.add(r);
                    ((PNode)res).setSupport(((itemState) r).getWeight());
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
        for (State p:this.getStates()) {
            for(int item = ((itemState)p).getFollow().previousSetBit(((itemState)p).getFollow().length());item > p.getItem() ; item =((itemState)p).getFollow().previousSetBit(item-1) ) {
                i = 0;
                T res =  (T) new PNode();
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
                        for(m = this.getPattern().size() - 1;found && m >= 0 && this.getPattern().get(m) >= 0;found =((itemState) r).getFollow().get(this.getPattern().get(m--)));
                    } 
                    if (found) {
                        ((PNode) res).states.add(r);
                        ((PNode) res).setSupport(((itemState) r).getWeight());
                        i = r.getlEnd() + 1;
                    } else  i++;
                    if (i < WASMA.itemStates.get(item).size()) r = (itemState) WASMA.itemStates.get(item).get(i); else break;
                } 
                resultat.put(item, (T)res);
            }
        }
        return resultat;        
    }

    @SuppressWarnings("unchecked")
    public  <T> T terminateSequence(){        //Delta(this,#) terminate a sequence by # same as global alignment (separated to save some tests)
        T res = (T) new PNode();
        int i; State r;
        for(State s:this.getStates()) {
            r = WASMA.itemStates.get(WASMA.itemsetDelimiter).get(((itemState)s).getDelim());
            while (r.getEnd() <= s.getEnd()) {
                if (!((delimState)r).getFollow().isEmpty())  ((PNode)res).states.add(r);
                i = r.getlEnd() + 1;
                if (i < WASMA.itemStates.get(WASMA.itemsetDelimiter).size()) r = WASMA.itemStates.get(WASMA.itemsetDelimiter).get(i);
                else break;
            }  
        }
        return res;         
    }
}