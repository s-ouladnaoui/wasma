import java.util.ArrayList;
import java.util.BitSet;
// one node of DFA used in determinisation without state existance check
// Note that the node records the current sequential pattern
public class PNode extends DfaState {
    ArrayList<Integer> Pattern;

    public PNode() {Pattern = new ArrayList<Integer>();}

    public ArrayList<Integer> getPattern() {return Pattern;}
    
    public PNode AlignLocal(int item) {
        PNode res = new PNode();
        int i = 0, m; State r; boolean found;
        for (State p:this.getStates()){
            if (!p.getFollow().get(item)) continue;
            if (p.getRoot() == 0) i = 0;
            else if (WASMA.itemsetDelimStates.get(p.getRoot()).get(item) >= i)
                    i = WASMA.itemsetDelimStates.get(p.getRoot()).get(item);
            r = WASMA.itemStates.get(item).get(i);
            while (r.getStart() < p.getStart()) {
                i = r.getlEnd() + 1;
                if (i < WASMA.itemStates.get(item).size()) r = WASMA.itemStates.get(item).get(i); else break;
            }
            while (r.getEnd() <= p.getEnd()) {
                if (r.getRoot() == p.getRoot()) found = true;
                else {
                    found = true;
                    for(m = this.getPattern().size() - 1;found && m >= 0 && this.getPattern().get(m) >= 0;found = r.getFollow().get(this.getPattern().get(m--)));
                }
                if (found) {
                    res.addState(r, true);
                    i = r.getlEnd() + 1;
                } else i++;
                if (i < WASMA.itemStates.get(item).size())  r = WASMA.itemStates.get(item).get(i);
                else break;
            }
        }
        return res;
    }

    public PNode AlignGlobal(int item) { 
        PNode res = new PNode();
        int i = 0; State r;
        for (State p:this.getStates()) {
            if (item >= 0 && !p.getFollow().get(item)) continue;
            i = (p.getStart() == 0) ? 0 : (item == WASMA.itemsetDelimiter) ? 
                            WASMA.itemStates.get(item).get(p.getDelim()).getOrder(): 
                            WASMA.itemsetDelimStates.get(p.getStart()).get(item);
            r = WASMA.itemStates.get(item).get(i);
            while (r.getEnd() <= p.getEnd()) {
                res.addState(r,true);
                i = r.getlEnd() + 1;
                if (i < WASMA.itemStates.get(item).size()) r = WASMA.itemStates.get(item).get(i);
                else break;
            }  
        }
        return res;         
    }
    public PNode Terminate_Sequence() {     //Delta(this,#) terminate a sequence by # same as global alignment (separated to save some tests)
        WASMA.fingerprint = new BitSet();
        PNode res = new PNode();
        int i; State r;
        for (State p:this.getStates()) {        
            i = WASMA.itemStates.get(WASMA.itemsetDelimiter).get(p.getDelim()).getOrder();
            r = WASMA.itemStates.get(WASMA.itemsetDelimiter).get(i);
            while (r.getEnd() <= p.getEnd()) {
                res.addState(r,false);
                i = r.getlEnd() + 1;
                if (i < WASMA.itemStates.get(WASMA.itemsetDelimiter).size()) r = WASMA.itemStates.get(WASMA.itemsetDelimiter).get(i);
                else break;
            }  
        }
        return res;         
    }

    public PNode Delta(int item) {          //res = this.Deltat(i)
        WASMA.fingerprint = new BitSet();
        return (this.IsDelimiterState())? 
            AlignGlobal(item):       // global alignment   item(this) == #  
            AlignLocal(item);    // local alignment (item != #) or  terminate the sequence item = # 
    }  
}