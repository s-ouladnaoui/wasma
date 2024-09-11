import java.util.ArrayList;
// one state of the wDFA is a set of states of the wNfa 
// base classe for the DFA classes
import java.util.List;
public class DState {

    List<State> states;        // the set of states composing one state of the DFA

    public DState() { states = new ArrayList<State>();}
    
    public List<State> getStates(){ return states;} 
}
