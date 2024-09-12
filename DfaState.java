import java.util.*;
// one state of the wDFA  add the support and a parametrized Pattern (for the 2 versions of the subset construction: with or without stat existence test)
public abstract class DfaState <P> extends DState {
    
    P Pattern;     
    int support;                                                                                                                                                                                                                      
    
    public boolean IsDelimiterState() { return states.get(0).getType(); }

    public int getSupport() { return support;}

    public void setSupport(int sprt) { support += sprt;}
    
    public P getPattern() {return Pattern;}

    public int getItem() { return states.get(0).getItem();}
    
    public String toString() { return states.toString(); }    
    
    public abstract <T> HashMap<Integer,T> extendGlobal();
    
    public abstract <T> HashMap<Integer,T> extendLocal(); 

    public abstract <T> T terminateSequence();

    public <T> HashMap<Integer,T> Delta() {
        return (this.IsDelimiterState()) ? extendGlobal() : extendLocal();
    }
}