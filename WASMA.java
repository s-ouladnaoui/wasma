import java.io.*;
import java.util.*;
/* The dataset represented by a Weighted Automaton */
public class WASMA {
    static final String itemSeparator = " ";                            /* separator in the input ds*/
    static final int itemsetDelimiter = -1,transactionDelimiter = -2;   /* the endmark of an itemset/a sequence */
    static int min_supp = 1, nbFreqSequences = 0, gcode = -1, lcode = 0, NbTransactions = 0;   /* the support threshold, number of frequent sequences in the dataset,  start code for reachability queries, number of transactions */
    static BufferedWriter writer;                                       /* for output */
    static Stack<Integer> stk = new Stack<>();                          /* used in printing the result:  */
    static Automaton<State> NFA;                                        // the weighted Automaton representing the dataset at loading 
    static Automaton<DfaState> DFA;                                      // Deterministic equivalent of the above NFA by subset construction contains the frquent sequential patterns of the dataset
    static ArrayDeque<Integer> DFAqueue;                                 /* the set of states of the weighted dfa and the queue of the same objects used during Determinization */
    static int NFAStartState = 0, DFAStartState = 0;                     /* initial states of the wnfa/wdfa */
    static HashMap<Integer,ArrayList<State>> itemStates = new HashMap<Integer,ArrayList<State>>();   // Vectors of states per item in local order
    static HashMap<Integer,HashMap<Integer,Integer>> itemsetDelimStates = new HashMap<Integer,HashMap<Integer,Integer>>();
    static HashMap<Integer,Integer> Order = new HashMap<>();  // local order for each item associated set of state 
    static HashMap<Integer,HashMap<BitSet,Integer>> DFAmap = new HashMap<>();
    static BitSet fItems = new BitSet();                           /* the set F1 of frequent items as a bitset*/  
    static BitSet fingerprint = new BitSet();               /* encode the set of states of a DFA state as bitset for existance check in subset constructiob algo */

    
    public WASMA (int ms) {
        NFA = new Automaton<State>();  
        NFA.newState(NFAStartState, new State(true,itemsetDelimiter)); 
        DFA = new Automaton<DfaState>(); 
        DFA.newState(DFAStartState, new DfaState(itemsetDelimiter));   
        DFAqueue = new ArrayDeque<Integer>();
        min_supp = ms;
    }
    /* Each state of the wNFA has a double integer code (start & end) used for descendence (reachability) check */
    public void encode (int s) {     
        State ss = NFA.State(s);
        int item = ss.getItem();
        if (item != itemsetDelimiter) ss.setDelim(itemStates.get(itemsetDelimiter) == null? 0: itemStates.get(itemsetDelimiter).size()); 
        if (item == itemsetDelimiter || fItems.get(item)) {
            ss.setStart(++gcode);
            if (itemStates.containsKey(item)) {
                itemStates.get(item).add(ss);
            }
            else if (s != 0) {
                ArrayList<State> r = new ArrayList<State>();
                r.add(ss);
                itemStates.put(item, r);
            }   
            if (!Order.containsKey(item)) {
                lcode = (item == itemsetDelimiter)? -1 : 0;
                Order.put(item,lcode);
            }
            else {
                lcode = Order.get(item)+1;  // recuperate the current order of the item states 
                Order.put(item, lcode);
            }
            ss.setOrder(lcode);
            if (s != 0 && item == itemsetDelimiter && !itemsetDelimStates.containsKey(ss.getStart()) && !NFA.getTransitions(s).isEmpty()) 
            itemsetDelimStates.put(ss.getStart(), new HashMap<>());
            ss.setRoot(NFA.State(ss.getRoot()).getStart());
            if (ss.getRoot() != 0 && item != itemsetDelimiter && !itemsetDelimStates.get(ss.getRoot()).containsKey(item)) 
            itemsetDelimStates.get(ss.getRoot()).put(item,ss.getOrder());
        }
        for (int q : NFA.getTransitions(s).values())  encode(q);
        if (item == itemsetDelimiter || fItems.get(item)) {
            ss.setEnd(gcode);
            ss.setlEnd(Order.get(ss.item)+1);
            if (!NFA.getTransitions(s).isEmpty() &&  item == itemsetDelimiter && ss.getRoot() != 0) 
                for (int i:itemsetDelimStates.get(ss.getStart()).keySet()) {
                    if (!itemsetDelimStates.get(ss.getRoot()).containsKey(i))
                        itemsetDelimStates.get(ss.getRoot()).put(i,itemsetDelimStates.get(ss.getStart()).get(i));
                }
        }
    }
    
    /* ====================================== The Dataset Loader  (construction of the wNF Automaton) ========================================================*/
    public void loadData(String inputfile) throws IOException {
        int  p, q, current_root = NFAStartState;
        p = current_root;
        BufferedReader in = new BufferedReader(new FileReader(inputfile));
        Stack<Integer> StateStack = new Stack<>();              // to track the follow items (as a bitsets)
        StateStack.push(0);
        HashMap<Integer, Integer> alphabet = new HashMap<>();   /* The items of the dataset and the associated supports */
        HashSet<Integer> members = new HashSet<>();
        int y, z, j, o, currentItemset, item;    // different working variables
        BitSet bs;
        HashMap<Integer,Integer> vSatate;    
        String transaction;
        String[] itemsets, items;
        long startTime = System.nanoTime();
        while ((transaction = in.readLine()) != null) { 
            vSatate = new HashMap<Integer,Integer>();
            itemsets = transaction.substring(0,transaction.length()-6).split("-1");
            j = 0;
            ArrayList<BitSet> sequenceBitset = new ArrayList<BitSet>();
            for (String itemset:itemsets) {
                sequenceBitset.add(new BitSet());
                for(String i:itemset.trim().split(" ")) { 
                    o = Integer.parseInt(i);
                    sequenceBitset.get(j).set(o); 
                    members.add(o);
                    vSatate.put(o, transactionDelimiter);
                }
                j++;
            }
            currentItemset = 0;
            items = transaction.split(itemSeparator);
            for (String ch : items) {
                item = Integer.parseInt(ch);
                switch (item) {
                    case transactionDelimiter:
                        NbTransactions++;
                        // This loop collect next items for each itemsetDelimiterState in a bottom up fashion 
                        y = StateStack.pop();               // the last stateDelimiter in the current sequence
                        while (--currentItemset >= 0) {
                            for (int i = sequenceBitset.get(currentItemset).length(); (i = sequenceBitset.get(currentItemset).previousSetBit(i-1)) >= 0;) {
                                if (vSatate.get(i) == transactionDelimiter) 
                                    vSatate.put(i,StateStack.pop());
                                else {
                                    z = StateStack.pop();
                                    bs = new BitSet();
                                    //bs.clear(0,i+1);
                                    bs.set(Math.min(i+1,NFA.State(vSatate.get(i)).getFollow().length()), NFA.State(vSatate.get(i)).getFollow().length());
                                    bs.and(NFA.State(vSatate.get(i)).getFollow());
                                    NFA.State(z).setFollow(bs); // the next follow items  
                                    vSatate.put(i,z);
                                }
                            }
                            z = StateStack.pop();               // the last stateDelimiter in the current sequence
                            NFA.State(z).setFollow(sequenceBitset.get(currentItemset)); // the local follow items
                            NFA.State(z).setFollow(NFA.State(y).getFollow()); // the next follow items
                            y = z;
                        }
                        for (Integer k : members) { // truck the current items to set the frequent ones
                            Integer f = alphabet.get(k);
                            if (f == null) {
                                alphabet.put(k, 1);
                                if (min_supp <= 1) fItems.set(k);  
                            } else {
                                alphabet.put(k, f + 1);
                                if (!fItems.get(k) && f+1 >= min_supp) fItems.set(k);
                            }
                        }
                        p = current_root = NFAStartState;
                        StateStack.push(p);
                        members.clear();
                        break;
                    case itemsetDelimiter : 
                        if (NFA.getTransitions(p).containsKey(item)) q = NFA.getTransitions(p).get(item);
                        else {
                            NFA.newState(q = NFA.newTransition(p,item), new State(true,itemsetDelimiter));
                            NFA.State(q).setRoot(current_root);
                        }
                        NFA.State(q).setWeight(1);
                        StateStack.push(q);
                        p = current_root = q;
                        currentItemset++;
                        break;
                    default :
                        //members.add(item);                // add the item to the alphabet
                        if (NFA.getTransitions(p).containsKey(item))  q = NFA.getTransitions(p).get(item);
                        else {
                            NFA.newState(q = NFA.newTransition(p,item), new State(false,item));
                            NFA.State(q).setRoot(current_root);
                        }
                        NFA.State(q).setWeight(1);
                        NFA.State(q).follow.or(sequenceBitset.get(currentItemset)); // assign prior and next items to the state
                        NFA.State(q).follow.clear(item); // exclude the item of the state from the bitmap
                        StateStack.push(q);
                        p = q;
                    }
                }
            }
            in.close();
/* ==================== Preparation of the Determinization: creation of the first state of the DFA =============================================================*/       
            // Set the global/local start and end codes for the NFA states they will be used in DELTA Computation (reachability checking)
            encode(NFAStartState);
            NFA.State(DFAStartState).setRoot(DFAStartState);
            DFAqueue.add(DFAStartState);
            DFA.State(DFAStartState).addState(NFA.State(NFAStartState),false);
            long endTime = System.nanoTime();
            writer.write("Database: " + inputfile + "; Alphabet size: " + alphabet.size() + "; Database size: " + NbTransactions + "\n");     
            writer.write("Preprocessing time: " + (endTime-startTime)/1000000 + " ms\nNFA States: "+NFA.NbStates+"\n");
            System.out.println("Database: " + inputfile + "; Alphabet size: " + alphabet.size() + "; Database size: " + NbTransactions);
            System.out.println("Preprocessing time: " + (endTime-startTime)/1000000 + " ms\nNFA States: "+NFA.NbStates);
            WASMA.NFA = null; // we don't need the NFA all the required information are in the first states of the DFA
    }

    public void Determinize() {
        int s;
        while (!DFAqueue.isEmpty()) {
            s = DFAqueue.remove();
            DfaState source_state = DFA.State(s), res, res_delimiter ; 
            int  r1, r2;
            for (int i = source_state.getFollow().nextSetBit(0); i >= 0; i =  source_state.getFollow().nextSetBit(i + 1)) {
                if (fItems.get(i) && i > source_state.getItem() ) {//&& root_transitions.containsKey(i)){        /* extend the state by i iff the root contains a transition by i */
/*================================================   res = delta(s,i)  ================================================================*/           
                    res = source_state.Delta(i,true);
                    if (res.getSupport() >= min_supp) {   
                        res.getMotif().or(source_state.motif);                      /* res = delta(s,i) is frequent */
                        if (DFAmap.get(i) == null) DFAmap.put(i, new HashMap<>());
                        if (!DFAmap.get(i).containsKey(fingerprint)){          /*  res is a new dfa state */
                            DFA.newState(r1 = DFA.newTransition(s, i), res);    // r1 the id number of the state res = delta(s,i)
                            DFAmap.get(i).put(fingerprint,r1);                 // add res to the DFA map state using its fingerprint
/*===================================================  res_delimiter = delta(res,itemsetDelimiter(#))  ===============================================================*/
                            DFA.newState(r2 = DFA.newTransition(r1, itemsetDelimiter),   // r2 the id number of the state res_delimiter = delta(res,#)
                                res_delimiter = res.Delta(itemsetDelimiter,false)); 
                            res_delimiter.setSupport(res.getSupport());             //  res and res_delimter have the same support (patterns sprt(p)= sprt(p#))
                            if (!res.getFollow().isEmpty()) DFAqueue.add(r1);
                            if (!res_delimiter.getFollow().isEmpty()) DFAqueue.add(r2); // if the 2 new states are extensibles add them to the queue
                        } else DFA.newTransition(s, i,DFAmap.get(i).get(fingerprint)); // res already exists in the DFA so we do not create it but insert a new transition labeled i from s to it  
                    } else res = null;       // res is infrequent so mark it. gc can recuperate its memory 
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        WASMA spm = new WASMA(Integer.parseInt(args[0]));               // min support
        writer = new BufferedWriter( new FileWriter(args[2]));          // output file
        boolean print = Boolean.parseBoolean(args[3]);
        spm.loadData(args[1]);                                          // input file
        long beforeUsedMem = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();
        spm.Determinize();
        String endTime = String.format("%.2f ms",(System.nanoTime()-startTime)/1000000d);
        long afterUsedMem =  Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        String mem = String.format("%.2f mb",(afterUsedMem-beforeUsedMem)/1024d/1024d);
        DFA.Print(DFAStartState,writer,print);
        writer.write("Min Supp: "  + min_supp + " (relative : "+String.format("%.3f",( (double) min_supp/NbTransactions))+")\n"+
                "DFA States: "+DFA.NbStates+ "\nNb Frequent Sequences: " + nbFreqSequences + "\nMining time: " + endTime +"\nMemory requirement: " + mem+"\n");
        System.out.println("Min Supp: "  + min_supp + " (relative : "+String.format("%.3f",( (double) min_supp/NbTransactions))+")\nDFA States: "+DFA.NbStates+"\nNb Frequent Sequences: " + nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem+"\n");
        writer.close();
    }
}