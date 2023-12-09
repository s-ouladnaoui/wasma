import java.io.*;
import java.util.*;

/* The dataset represented by a Weighted Automaton */

public class WASMA {
    static Automaton<State> NFA;   
    static Automaton<DfaState> DFA;                      
    static ArrayDeque<Integer> DFAqueue;                  /* the set of states of the weighted dfa and the queue of the same objects used during Determinization */
    static int NFAStartState = 0, DFAStartState = 0;      /* initial states of the wnfa/wdfa */
    static BitSet fItems = new BitSet();                  /* the set F1 of frequent items as a bitset*/  
    static final String itemSeparator = " ";              /* separator in the input ds*/
    static final int itemsetDelimiter = -1,transactionDelimiter = -2; /* the endmark of an itemset/a sequence */
    static int min_supp = 1, nbFreqSequences = 0, code = 0, NbTransactions = 0;   /* the support threshold, number of frequent sequences in the dataset,  start code for reachability queries, number of transactions */
    static BufferedWriter writer ;                               /* for output */
    static Stack<Integer> stk = new Stack<>();
    static HashMap<Integer,HashMap<BitSet,Integer>> DFAmap = new HashMap<>();
    static TreeSet<State> reference = new TreeSet<State>(State.BY_DESC );
    static BitSet fringerprint = new BitSet();

    public WASMA (int ms) {
        NFA = new Automaton<State>();  
        NFA.StateMap.put(NFAStartState, new State(true)); 
        DFA = new Automaton<DfaState>(); 
        DFA.StateMap.put(DFAStartState, new DfaState(0));   
        DFAqueue = new ArrayDeque<Integer>();
        min_supp = ms;
    }

    /* each state of the wNFA has a double integer code (start & end) used for reachability check */
    public void codage (int s) {     
        NFA.State(s).setStart(code++);
        for (int q : NFA.getTransitions(s).values())  codage(q);
        NFA.State(s).setEnd(code++);
    }
    /* ====================================== dataset loader ========================================================*/
    public void loadData(String inputfile) throws IOException {
        int  q, p, current_root = NFAStartState;
        p = current_root;
        BufferedReader in = new BufferedReader(new FileReader(inputfile));
        Stack<Integer> StateStack = new Stack<>();            // to track the follow items (as a bitsets)
        StateStack.push(0);
        HashMap<Integer, Integer> alphabet = new HashMap<>();    /* The items of the dataset and the associated supports */
        HashSet<Integer> members = new HashSet<>();
        BitSet currentItems = new BitSet();  /* bitset of the frequent items (the set F1) */
        HashMap<Integer,ArrayList<Integer>> lStates = new HashMap<Integer,ArrayList<Integer>>();
        String transaction;
        long startTime = System.nanoTime();
        int x,y;
        while ((transaction = in.readLine()) != null)
        {
            String[] items = transaction.split(itemSeparator);
            for (String ch : items) {
                int item = Integer.parseInt(ch);
                switch (item) {
                    case transactionDelimiter:
                        NbTransactions++;
                        // This loop collect next items for each State in a bottom up fashion 
                        y = StateStack.pop();               // the last state in the current sequence
                        BitSet  lastAlphabet = new BitSet(),
                                currentAlphabet = new BitSet();
                        while (!StateStack.isEmpty()) {
                            x = StateStack.pop();
                            for (int k:NFA.getTransitions(y).keySet()) {
                                if (k >= 0) currentAlphabet.set(k);
                            }
                            for (int k:NFA.getTransitions(x).keySet()) {
                                if (k >= 0) currentAlphabet.set(k);
                            }
                            if (NFA.State(x).getType()) {
                                lastAlphabet.or(currentAlphabet);
                                NFA.State(x).setFollow(lastAlphabet);
                                currentAlphabet.clear();
                            } else{
                                if (!NFA.State(y).getType()) NFA.State(x).setFollow(NFA.State(y).getFollow());
                            }
                            y = x;
                        }
                        for (Integer k : members) {
                            Integer f = alphabet.get(k);
                            if (f == null) {
                                alphabet.put(k, 1);
                                if (min_supp == 1) fItems.set(k);  
                            } else {
                                alphabet.put(k, f + 1);
                                if (!fItems.get(k) && f+1 >= min_supp) fItems.set(k);
                            }
                        }
                        p = current_root =  NFAStartState;
                        StateStack.push(p);
                        members.clear();
                        break;
                    case itemsetDelimiter :
                        if (NFA.getTransitions(p).containsKey(item)) {
                            q = NFA.getTransitions(p).get(item);
                        } else {
                            q = NFA.newTransition(p,item);
                            NFA.newState(q, new State(true));
                            NFA.State(q).setRoot(current_root);
                            if (lStates.containsKey(item)) lStates.get(item).add(q);
                            else {
                                ArrayList<Integer> r = new ArrayList<Integer>();
                                r.add(q);
                                lStates.put(item, r);
                            }
                        }
                        NFA.State(q).setNum(q);
                        NFA.State(q).setWeight(1);
                        StateStack.push(q);
                        currentItems.clear();
                        current_root = q;
                        p = q;
                        break;
                    default:
                        members.add(item);              // add the item to the alphabet
                        currentItems.set(item);         // set the item bit of the item 
                        if (NFA.getTransitions(p).containsKey(item)) {
                            q = NFA.getTransitions(p).get(item);
                        } else {
                            q = NFA.newTransition(p,item);
                            NFA.newState(q, new State(false));
                            NFA.State(q).setRoot(current_root);
                            NFA.State(p).follow.set(item);
                            if (lStates.containsKey(item)) lStates.get(item).add(q);
                            else {
                                ArrayList<Integer> r = new ArrayList<Integer>();
                                r.add(q);
                                lStates.put(item, r);
                            }
                        }
                        NFA.State(q).setNum(q);
                        NFA.State(q).setWeight(1);
                        StateStack.push(q);
                        p = q;
                    }
                }
            }
            in.close();
            long endTime = System.nanoTime();
            writer.write("Database: " + inputfile + "; Alphabet size: " + alphabet.size() + "; Database size: " + NbTransactions + "\n");     
            writer.write("Loading time: " + (endTime-startTime)/1000000 + " ms\n");
            System.out.println("Database: " + inputfile + "; Alphabet size: " + alphabet.size() + "; Database size: " + NbTransactions);
            System.out.println("Loading time: " + (endTime-startTime)/1000000 + " ms");
/* ==================== Preparation of the Determinization: creation of the first states of the DFA =============================================================*/       
            // Set the start and end code values for the states of the used in reachability cheking
            codage(NFAStartState);
            int item_state; DfaState s1, s2;
            NFA.State(DFAStartState).setRoot(DFAStartState);
            int delim_state = DFA.newTransition(DFAStartState, itemsetDelimiter);
            DFA.StateMap.put(delim_state, s1 = new DfaState(itemsetDelimiter));
            // add the transition from wDFAStartState by the itemsetdelimiter  (-1 here)
            int order = 0;
            for (int s:lStates.get(itemsetDelimiter)){
                s1.addState(s,false);
                NFA.State(s).setOrder(order++);
            }
            DFAmap.put(itemsetDelimiter, new HashMap<>());
            // Prepare the first states of the DFA: the set of transitions from the initial state of the DFA by the frequent items (the F1 set) 
            for ( int i = 0; i <= fItems.size(); i++) {
                if (fItems.get(i)){
                    item_state = DFA.newTransition(DFAStartState, i); 
                    DFA.StateMap.put(item_state, s1 = new DfaState(i));
                    s1.setSupport(alphabet.get(i));
                    s1.setRoot(DFAStartState);
                    order = 0;
                    for (int d:lStates.get(i)){
                        s1.addState(d,false);
                        NFA.State(d).setOrder(order++);
                    }
                    DFAmap.put(i, new HashMap<>());
                    DFAqueue.add(item_state);
                    int delim_state1 = DFA.newTransition(item_state, itemsetDelimiter); 
                    DFA.StateMap.put(delim_state1, s2 = new DfaState(itemsetDelimiter));
                    DFA.State(delim_state1).setSupport(alphabet.get(i));
                    s2.setRoot(DFAStartState);
                    for (State m :s1.getRoots()) {
                        s2.Align(s1, DFA.State(DFA.getTransitions(s1.getRoot()).get(itemsetDelimiter)),m.getNum());      
                    }
                    DFAqueue.add(delim_state1); 
                } else if (lStates.containsKey(i)) {        // remove data associated to infrequent items
                    for (int j:lStates.get(i)) NFA.StateMap.remove(j); 
                    lStates.remove(i);
                    alphabet.remove(i);
                }
        }
            WASMA.NFA.adjList = null; // we don't need the NFA adj list all the required information are in the first states of the DFA
        }

    public void Determinize() {
        int s;
        while (!DFAqueue.isEmpty()) {
            s = DFAqueue.remove();
            DfaState res, res_delimiter, source_state = DFA.State(s); 
            int item = source_state.getItem(), root = source_state.getRoot(), r1, r2;
            TreeMap<Integer,Integer> root_transitions = DFA.getTransitions(source_state.getRoot());
            for (int i = DFA.State(s).getFollow().nextSetBit(0); i > 0; i =  DFA.State(s).getFollow().nextSetBit(i + 1)) {
                if (root_transitions.containsKey(i)){        /* extend the state by i iff the root contains a transition by i */
/*================================================   d = delta(s,i)  ================================================================*/
                    res = source_state.Delta(i,DFA.State(root_transitions.get(i)));
                    if (res.getSupport() >= min_supp) {           /* r1 is frequent */
                        if (!DFAmap.get(i).containsKey(fringerprint)){
                            DFA.StateMap.put(r1 = DFA.newTransition(s, i), res);   // r1 the id number of the state res = delta(s,i)
                            res.setRoot(item == itemsetDelimiter? s : root);
                            DFAmap.get(i).put(fringerprint,r1);
/*===================================================  dd = delta(d,itemsetDelimiter)  ===============================================================*/
                            DFA.StateMap.put(r2 = DFA.newTransition(r1, itemsetDelimiter),   // r2 the id number of the state res_delimiter = delta(res,#)
                                res_delimiter = res.Delta(itemsetDelimiter,
                                DFA.State(DFA.adjList.get((item == itemsetDelimiter?root_transitions.get(i):s)).get(itemsetDelimiter)))); 
                            res_delimiter.setRoot(res.getRoot());   
                            res_delimiter.setSupport(res.getSupport());   //  d and dd have the same support (patterns sprt(p)= sprt(p#))
                            DFAmap.get(itemsetDelimiter).put(fringerprint,r2);
                            if (!res.getFollow().isEmpty()) DFAqueue.add(r1);
                            if (!res_delimiter.getFollow().isEmpty()) DFAqueue.add(r2);
                        } else DFA.newTransition(s, i,DFAmap.get(i).get(fringerprint));
                    } else res = null;       // mark d so gc can recuperate its memory 
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        WASMA automate = new WASMA(Integer.parseInt(args[0]));              // min support
        writer = new BufferedWriter( new FileWriter(args[2]));              // output file
        automate.loadData(args[1]);                                         // input file
        long beforeUsedMem = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();
        automate.Determinize();
        String endTime = String.format("%.2f ms",(System.nanoTime()-startTime)/1000000d);
        long afterUsedMem =  Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        String mem = String.format("%.2f mb",(afterUsedMem-beforeUsedMem)/1024d/1024d);
        DFA.Print(DFAStartState,writer);
        writer.write("Min Supp: " + min_supp + "\nNb Frequent Sequences : " + nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem+"\n");
        System.out.println("Min Supp: " + min_supp + "\nNb Frequent Sequences: " + nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem+"\n");
        writer.close();
    }
}