import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Automaton<T> {
    private int NbStates;
    private ArrayList<HashMap<Integer,Integer>> adjList;    /* the set of states of the weighted nfa */
    private HashMap<Integer,T> StateMap = new HashMap<>();  /* Map to hash the Object to its id */

    public Automaton(){
        adjList = new ArrayList<>();
        adjList.add(new HashMap<Integer,Integer>());
    }

    public T State(int i) {return StateMap.get(i);}


    public HashMap<Integer,Integer> getTransitions(int state){
        return adjList.get(state);
    }
    
    public int newTransition(int state, int item){      // new transition from state by item to a new state
        adjList.add(new HashMap<>());
        StateMap.put(++NbStates, null);
        adjList.get(state).put(item,NbStates);
        return NbStates;
    }
    
    public void newTransition(int source, int item, int dest){ // new transition by item between two existing states: source and dest 
        adjList.get(source).put(item,dest);
    }

    public void newState(int i, T s) {        // new state in the Map of stateIds
        StateMap.put(i, s);
    }

    public void removeMapState(int i) { StateMap.remove(i);}

    public void DestructAdjList() { adjList = null;}

    public void Print(int node, BufferedWriter w, boolean write) throws IOException {   // print the Automaton and display the set of frequent patterns
        for(int e:WASMA.DFA.adjList.get(node).keySet()) {  
            WASMA.stk.push(e);
            if (e == WASMA.itemsetDelimiter && node != WASMA.DFAStartState) {
                WASMA.nbFreqSequences++;              // a new frequent pattern
                if (write) 
                    w.write(WASMA.stk.toString()+" : "+((DfaState)StateMap.get(adjList.get(node).get(e))).getSupport()+"\n");
            }
            Print(WASMA.DFA.adjList.get(node).get(e),w,write);
            WASMA.stk.pop();
        }
    } 
}
