import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class Automaton<T> {
    int NbStates;
    ArrayList<TreeMap<Integer,Integer>> adjList;  /* the set of states of the weighted nfa */
    HashMap<Integer,T> StateMap = new HashMap<>();

    public Automaton(){
        adjList = new ArrayList<>();
        adjList.add(new TreeMap<>());
    }

    public T State(int i) {return StateMap.get(i);}


    public TreeMap<Integer,Integer> getTransitions(int state){
        return adjList.get(state);
    }
    
    public int newTransition(int state, int item){
        adjList.add(new TreeMap<>());
        StateMap.put(++NbStates, null);
        adjList.get(state).put(item,NbStates);
        return NbStates;
    }
    
    public void newTransition(int source, int item, int dest){
        adjList.get(source).put(item,dest);
    }

    public void newState(int i, T s) {
        StateMap.put(i, s);
    }

    public void Print(int node, BufferedWriter w) throws IOException {
        for(int e:WASMA.DFA.adjList.get(node).keySet()) {  
            WASMA.stk.push(e);
            if (e == WASMA.itemsetDelimiter && node != WASMA.DFAStartState) {
                WASMA.nbFreqSequences++;              // a new frequent pattern
                w.write(WASMA.stk.toString()+" : "+((DfaState)StateMap.get(adjList.get(node).get(e))).getSupport()+"\n");
            }
            Print(WASMA.DFA.adjList.get(node).get(e),w);
            WASMA.stk.pop();
        }
    } 
}
