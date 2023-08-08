import java.util.Set;
import java.util.TreeSet;

public class DState {
    TreeSet<State> etats;
    int support;                                                                                                                                                                                                                      

    public DState() {
        etats = new TreeSet<>();
    }

    public TreeSet<State> getEtats() {
        return etats;
    }
    
    public void setEtats(Set<State> s){
        this.etats.addAll(s);
    }

    public int getSupport() {
        return support;
    }

    public void setSupport(int support) {
        this.support = support;
    }
}