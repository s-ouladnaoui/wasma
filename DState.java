import java.util.ArrayList;
// one state of the weighted DFA is a set of states of the weighed Nfa 
// base classe for the DFA classes
import java.util.List;
public class DState {

    List<State> states;        // the set of states composing one state of the DFA

    public DState() { states = new ArrayList<State>();}
    
    public List<State> getStates(){ return states;} 
}
