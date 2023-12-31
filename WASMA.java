import java.io.*;
import java.util.*;
/* The dataset represented by a Weighted Automaton */

public class WASMA {
    static Automaton<State> NFA;                          // the weighted Automaton representing the dataset at loading 
    static Automaton<DfaState> DFA;                       // Deterministic equivalent of the above NFA by subset construction contains the frquent sequential patterns of the dataset
    static ArrayDeque<Integer> DFAqueue;                  /* the set of states of the weighted dfa and the queue of the same objects used during Determinization */
    static int NFAStartState = 0, DFAStartState = 0;      /* initial states of the wnfa/wdfa */
    static final String itemSeparator = " ";              /* separator in the input ds*/
    static final int itemsetDelimiter = -1,transactionDelimiter = -2; /* the endmark of an itemset/a sequence */
    static int min_supp = 1, nbFreqSequences = 0, code = 0, NbTransactions = 0;   /* the support threshold, number of frequent sequences in the dataset,  start code for reachability queries, number of transactions */
    static BufferedWriter writer;                               /* for output */
    static Stack<Integer> stk = new Stack<>();                  /* used in printing the result:  */
    static HashMap<Integer,HashMap<BitSet,Integer>> DFAmap = new HashMap<>();
    static BitSet fringerprint;
    static TreeSet<State> reference;
    static boolean global;
    public WASMA (int ms) {
        NFA = new Automaton<State>();  
        NFA.newState(NFAStartState, new State(true)); 
        DFA = new Automaton<DfaState>(); 
        DFA.newState(DFAStartState, new DfaState());   
        DFAqueue = new ArrayDeque<Integer>();
        min_supp = ms;
    }

    /* Each state of the wNFA has a double integer code (start & end) used for descendence (reachability) check */
    public void codage (int s) {     
        NFA.State(s).setStart(code++);
        for (int q : NFA.getTransitions(s).values())  
            codage(q);
        NFA.State(s).setEnd(code++);
    }

    /* ====================================== dataset loader ========================================================*/
    public void loadData(String inputfile) throws IOException {
        int  q, p, current_root = NFAStartState;
        p = current_root;
        BitSet fItems = new BitSet();                           /* the set F1 of frequent items as a bitset*/  
        BufferedReader in = new BufferedReader(new FileReader(inputfile));
        Stack<Integer> StateStack = new Stack<>();              // to track the follow items (as a bitsets)
        StateStack.push(0);
        HashMap<Integer, Integer> alphabet = new HashMap<>();   /* The items of the dataset and the associated supports */
        HashSet<Integer> members = new HashSet<>();
        BitSet currentItems = new BitSet();                     
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
                            for (int k:NFA.getTransitions(y).keySet()) 
                                if (k >= 0) currentAlphabet.set(k);
                            for (int k:NFA.getTransitions(x).keySet()) 
                                if (k >= 0) currentAlphabet.set(k);
                            if (NFA.State(x).getType()) {
                                lastAlphabet.or(currentAlphabet);
                                NFA.State(x).setFollow(lastAlphabet);
                                currentAlphabet.clear();
                            } else
                                if (!NFA.State(y).getType()) NFA.State(x).setFollow(NFA.State(y).getFollow());
                            y = x;
                        }
                        for (Integer k : members) {
                            Integer f = alphabet.get(k);
                            if (f == null) {
                                alphabet.put(k, 1);
                                if (1 >= min_supp) fItems.set(k);  
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
                        if (NFA.getTransitions(p).containsKey(item)) q = NFA.getTransitions(p).get(item);
                        else {
                            NFA.newState(q = NFA.newTransition(p,item), new State(true));
                            NFA.State(q).setRoot(current_root);
                            if (lStates.containsKey(item)) lStates.get(item).add(q);
                            else {
                                ArrayList<Integer> r = new ArrayList<Integer>();
                                r.add(q);
                                lStates.put(item, r);
                            }
                        }
                        NFA.State(q).setWeight(1);
                        StateStack.push(q);
                        currentItems.clear();
                        current_root = q;
                        p = q;
                        break;
                    default:
                        members.add(item);              // add the item to the alphabet
                        currentItems.set(item);         // set the item bit
                        if (NFA.getTransitions(p).containsKey(item)) 
                            q = NFA.getTransitions(p).get(item);
                        else {
                            NFA.newState(q = NFA.newTransition(p,item), new State(false));
                            NFA.State(q).setRoot(current_root);
                            NFA.State(p).follow.set(item);
                            if (lStates.containsKey(item)) 
                                lStates.get(item).add(q);
                            else {
                                ArrayList<Integer> r = new ArrayList<Integer>();
                                r.add(q);
                                lStates.put(item, r);
                            }
                        }
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
/* ==================== Preparation of the Determinization: creation of the first states of the DFA (relative to the set F1 of frequent 1-sequences) =============================================================*/       
            // Set the start and end codes for the NFA states they will be used in reachability checking
            codage(NFAStartState);
            int item_state; DfaState s1, s2;
            NFA.State(DFAStartState).setRoot(DFAStartState);
            DFA.newState(DFA.newTransition(DFAStartState, itemsetDelimiter), s1 = new DfaState());
            // add the transition from wDFAStartState by the itemsetdelimiter  (-1 here)
            fringerprint = new BitSet();
            int order = 0;
            for (int s:lStates.get(itemsetDelimiter)){
                s1.states.add(NFA.State(s));
                NFA.State(s).setOrder(order++);          // used to optimize memory requirment of the Bitset encoding of statsets
            }
            // Prepare the first states of the DFA: the set of transitions from the initial state of the DFA by the frequent items (the F1 set) 
            for (int i = fItems.nextSetBit(0); i >= 0; i = fItems.nextSetBit(i+1)) {             
                    DFA.newState(item_state = DFA.newTransition(DFAStartState, i), s1 = new DfaState());
                    s1.setSupport(alphabet.get(i));
                    s1.setRoot(DFAStartState);
                    order = 0;
                    for (int d:lStates.get(i)){
                        State t = NFA.State(d); // to avoid multiple call to NFA.State(d)
                        s1.states.add(t);
                        t.setOrder(order++);    // used to optimize memory requirment of the Bitset encoding of statsets
                        if (!t.getFollow().isEmpty()) s1.setFollow(t.getFollow()); 
                    }
                    DFAmap.put(i, new HashMap<>());
                    DFAqueue.add(item_state);
                    int delim_state1;       // delim_state1=Delta(item_state,#)
                    DFA.newState(delim_state1 = DFA.newTransition(item_state, itemsetDelimiter),
                                s2 = s1.Delta(itemsetDelimiter, DFA.State(DFA.getTransitions(DFAStartState).get(itemsetDelimiter)), false));
                    s2.setRoot(DFAStartState);
                    s2.setSupport(alphabet.get(i));
                    DFAqueue.add(delim_state1); 
        }
        WASMA.NFA = null; // we don't need the NFA all the required information are in the first states of the DFA
    }

    public void Determinize() {
        int s;
        while (!DFAqueue.isEmpty()) {
            s = DFAqueue.remove();
            DfaState    res, 
                        res_delimiter, 
                        source_state = DFA.State(s); 
            int root = source_state.getRoot(), 
                r1,
                r2;
            HashMap<Integer,Integer> root_transitions = DFA.getTransitions(root);
            for (int i = DFA.State(s).getFollow().nextSetBit(0); i > 0; i =  DFA.State(s).getFollow().nextSetBit(i + 1)) {
                if (root_transitions.containsKey(i)){        /* extend the state by i iff the root contains a transition by i */
/*================================================   res = delta(s,i)  ================================================================*/
                    res = source_state.Delta(i,DFA.State(root_transitions.get(i)),true);
                    if (res.getSupport() >= min_supp) {                         /* res = delta(s,i) is frequent */
                        if (!DFAmap.get(i).containsKey(fringerprint)){          /*  res is a new dfa state */
                            DFA.newState(r1 = DFA.newTransition(s, i), res);    // r1 the id number of the state res = delta(s,i)
                            res.setRoot(source_state.IsDelimiterState()? s : root);    // set the root of res to s if the later is a delimiterState otherwise to the root of s
                            DFAmap.get(i).put(fringerprint,r1);                 // add res to the DFA map state using its fingerprint
/*===================================================  res_delimiter = delta(res,itemsetDelimiter)  ===============================================================*/
                            DFA.newState(r2 = DFA.newTransition(r1, itemsetDelimiter),   // r2 the id number of the state res_delimiter = delta(res,#)
                                res_delimiter = res.Delta(itemsetDelimiter,
                                DFA.State(DFA.getTransitions((source_state.IsDelimiterState()?root_transitions.get(i):s)).get(itemsetDelimiter)),false)); 
                            res_delimiter.setRoot(res.getRoot());   
                            res_delimiter.setSupport(res.getSupport());             //  res and res_delimter have the same support (patterns sprt(p)= sprt(p#))
                            if (!res.getFollow().isEmpty()) DFAqueue.add(r1);
                            if (!res_delimiter.getFollow().isEmpty()) DFAqueue.add(r2); // if the 2 new states are extensibles add them to the queue
                        } else DFA.newTransition(s, i,DFAmap.get(i).get(fringerprint)); // res already exists in the DFA so we do not create it but insert a new transition label i from s to it  
                    } else res = null;       // res is infrequent so mark it. gc can recuperate its memory 
                    WASMA.fringerprint = null;
                    WASMA.reference = null;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        WASMA spm = new WASMA(Integer.parseInt(args[0]));               // min support
        writer = new BufferedWriter( new FileWriter(args[2]));          // output file
        spm.loadData(args[1]);                                          // input file
        boolean print = Boolean.parseBoolean(args[3]);
        long beforeUsedMem = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();
        spm.Determinize();
        String endTime = String.format("%.2f ms",(System.nanoTime()-startTime)/1000000d);
        long afterUsedMem =  Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        String mem = String.format("%.2f mb",(afterUsedMem-beforeUsedMem)/1024d/1024d);
        DFA.Print(DFAStartState,writer,print);
        writer.write("Min Supp: "  + min_supp + " (relative : "+String.format("%.3f ",( (double) min_supp/NbTransactions))+")\nNb Frequent Sequences: " + nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem+"\n");
        System.out.println("Min Supp: "  + min_supp + " (relative : "+String.format("%.3f ",( (double) min_supp/NbTransactions))+")\nNb Frequent Sequences: " + nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem+"\n");
        writer.close();
    }
}