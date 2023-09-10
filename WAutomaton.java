import java.io.*;
import java.util.*;

/* The dataset represented by a Weighted Automaton */
public class WAutomaton {

    static ArrayList<State> wNFAStates;                   /* the set of states of the weighted nfa */
    static ArrayDeque<DfaState> DFAqueue;                 /* the set of states of the weighted dfa and the queue of the same objects used during Determinization */
    static State wNFAStartState;                         /* the initial state of the wnfa */
    static DfaState wDFAStartState;                       /* the initial state of the wnfa */
    static BitSet fItems = new BitSet();                  /* the set F1 of frequent items */  
    static final String itemSeparator = " ";
    static final int itemsetDelimiter = -1;               /* the endmark of an itemset */
    static final int transactionDelimiter = -2;           /* the endmark of a sequence */
    //static final HashMap<DfaState,HashMap<Integer,DfaState>> delta = new HashMap<DfaState,HashMap<Integer,DfaState>>();

    //static int NbState = 0;                               /* number of states */
    static int min_supp = 1;                              /* the support threshold */

    static int nbFreqSequences = 0;                              /* number of frequent sequences in the dataset*/
    int code = 0;                                         /* start code for reachability queries */
    int NbTransactions = 0;                                /* number of transactions */
    BufferedWriter writer ;                               /* for output */


    public WAutomaton (int ms) {
        wNFAStates = new ArrayList<>();   
        wNFAStates.add(wNFAStartState = new State(true));        // the initial state of the weighted nfa
        wDFAStartState = new DfaState();                     // the initial state of the weighted dfa
        wDFAStartState.getStates().put(wNFAStartState, new TreeSet<State>());
        DFAqueue = new ArrayDeque<DfaState>();
        min_supp = ms;
    }

    public  String toString() {                /* Print the automaton */
        String ch = "";
        int i = 0;
        for (Object p:wNFAStates) {
            ch = ch+i+" : "+p.toString()+"\n";
            i++;
        }
        return ch;
    }

    /* each state of the wNFA has a double integer code (start & end) used for reachability check */
    public void codage (State s) {     
        s.setStart(code++);
        for (State q  : s.getTransitions().values())
            codage(q);
        s.setEnd(code++);
    }

    /* ====================================== dataset loader ========================================================*/
    public void loadData(String inputfile) throws IOException {
        State  p,q;
        State current_root = wNFAStartState;
        p = current_root;
        BufferedReader in = new BufferedReader(new FileReader(inputfile));
        Stack<State> StateStack = new Stack<>();            // to track the follow items (as a bitsets)
        StateStack.push(wNFAStartState);
        HashMap<Integer, Integer> alphabet = new HashMap<>();    /* The items of the dataset and the associated supports */
        HashSet<Integer> members = new HashSet<>();
        BitSet currentItems = new BitSet();  /* bitset of the frequent items (the set F1) */
        HashMap<Integer,ArrayList<State>> lStates = new HashMap<Integer,ArrayList<State>>();
        String transaction;
        long startTime = System.nanoTime();
        State x,y;
        while ((transaction = in.readLine()) != null)
        {
            String[] items = transaction.split(itemSeparator);
            for (String ch : items) {
                int item = Integer.parseInt(ch);
                switch (item) {
                    case transactionDelimiter:
                        NbTransactions++;
                        // This loop collect next items for each IState in a bottom up fashion 
                        y = StateStack.pop();// the last state in the current sequence
                        BitSet  lastAlphabet = new BitSet(),
                                currentAlphabet = new BitSet();
                        while (!StateStack.isEmpty()) {
                            x = StateStack.pop();
                            for (int k:y.getTransitions().keySet()) {
                                if (k >= 0) currentAlphabet.set(k);
                            }
                            for (int k:x.getTransitions().keySet()) {
                                if (k >= 0) currentAlphabet.set(k);
                            }
                            if (x.getType()) {
                                lastAlphabet.or(currentAlphabet);
                                x.setFollow(lastAlphabet);
                                currentAlphabet.clear();
                            } else{
                                if (!y.getType()) x.setFollow(y.getFollow());
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
                        p = current_root =  wNFAStartState;
                        StateStack.push(p);
                        members.clear();
                        break;
                    case itemsetDelimiter :
                        //IState q; 
                        if (p.getTransitions().containsKey(item)) {
                            q = p.getTransitions().get(item);
                        } else {
                           //++NbState;
                            q = new State(true);
                            p.addTransition(item, q);
                            q.setRoot(current_root);
                            wNFAStates.add(q);
                            if (lStates.containsKey(item)) lStates.get(item).add(q);
                            else {
                                ArrayList<State> r = new ArrayList<State>();
                                r.add(q);
                                lStates.put(item, r);
                            }
                        }
                        q.setWeight(1);
                        StateStack.push(q);
                        currentItems.clear();
                        current_root = q;
                        p = q;
                        break;
                    default:
                        members.add(item);              // add the item to the alphabet
                        currentItems.set(item);         // set the item bit of the item 
                        if (p.getTransitions().containsKey(item)) {
                            q = p.getTransitions().get(item);
                        } else {
                            //++NbState;
                            q = new State(false);
                            p.addTransition(item, q);
                            q.setRoot(current_root);
                            wNFAStates.add(q);
                            if (lStates.containsKey(item)) lStates.get(item).add(q);
                            else {
                                ArrayList<State> r = new ArrayList<State>();
                                r.add(q);
                                lStates.put(item, r);
                            }
                        }
                        q.setWeight(1);
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
            /* ======== Preparation of the Determinization: creation of the first states of the DFA ===================================== */       
            // set the start and end code values for the states of the NFA
            codage(wNFAStartState);
            wDFAStartState.setRoot(wDFAStartState);
            wDFAStartState.extendPattern(itemsetDelimiter);
            // add the transition from wDFAStartState by the itemsetdelimiter  (-1 here)
            DfaState s = new DfaState();
            for (State d:lStates.get(itemsetDelimiter)){
                s.addState(d);
            }
            wDFAStartState.addTransition(itemsetDelimiter, s);
            s.setRoot(wDFAStartState);
            // prepare the first states of the DFA: the set of transitions from the initial state of the DFA by the frequent items
            for (int i = fItems.nextSetBit(0); i > 0; i = fItems.nextSetBit(i + 1)) {
                s = new DfaState();
                s.setSupport(alphabet.get(i));
                for (State d:lStates.get(i)){
                    s.addState(d);
                }
                wDFAStartState.addTransition(i, s);
                s.setRoot(wDFAStartState);
                s.extendPattern(i);
                DFAqueue.add(s);
                DfaState r = new DfaState();
                for (State m: s.getStates().keySet()) {
                    r.Align(s.getStates(m).iterator(), wDFAStartState.getTransitions().get(itemsetDelimiter).getStates(m).iterator(),false);  
                }
                r.setRoot(wDFAStartState);
                r.extendPattern(s.getItem());
                r.extendPattern(itemsetDelimiter);
                s.addTransition(itemsetDelimiter, r);
                System.out.println(nbFreqSequences+" => : "+r.getPattern() +" : "+s.getSupport());
                DFAqueue.add(r);
                nbFreqSequences++;
            }
            wNFAStates = null;
        }    

    public void Determinize() {
        DfaState s;
        while (!DFAqueue.isEmpty()) {
            s = DFAqueue.remove();
            s.getFollow().and(fItems);
            for (int i = s.getFollow().nextSetBit(0); i > 0; i = s.getFollow().nextSetBit(i + 1)) {
                if (s.getRoot().getTransitions().containsKey(i)){  // extend the state by i iff the root contains a transition by i 
                    DfaState r1 = s.delta(i, s.getRoot());
                    if (r1.getSupport() >= min_supp) {
                        s.addTransition(i, r1);
                        r1.pattern = new ArrayList<Integer>(s.getPattern());       // Create new pattern by retrieving the current state pattern
                        r1.extendPattern(i);
                        r1.setRoot(s.getItem() == itemsetDelimiter ? s : s.getRoot());
                        DfaState r2 = r1.delta(itemsetDelimiter, s.getItem() == itemsetDelimiter?s.getRoot().getTransitions().get(i):s);
                        r2.setRoot(r1.getRoot());
                        r2.pattern =  new ArrayList<Integer>(r1.getPattern());                                     // extend it by a 
                        r2.extendPattern(itemsetDelimiter);
                        r1.addTransition(itemsetDelimiter,r2);
                        nbFreqSequences++;
                        System.out.println(nbFreqSequences+" => : "+r2.getPattern() +" : "+r1.getSupport());
                        if (!r1.getFollow().isEmpty()) DFAqueue.add(r1);
                        if (!r2.getFollow().isEmpty()) DFAqueue.add(r2);            
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        WAutomaton automate = new WAutomaton(Integer.parseInt(args[0]));    // min support
        automate.writer = new BufferedWriter( new FileWriter(args[2]));     // output file
        automate.loadData(args[1]);                                         // input file
        long beforeUsedMem = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();
        automate.Determinize(); 
        String endTime = String.format("%.2f ms",(System.nanoTime()-startTime)/1000000d);
        long afterUsedMem =  Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        String mem = String.format("%.2f mb",(afterUsedMem-beforeUsedMem)/1024d/1024d);
        automate.writer.write("Min Supp: " + min_supp + "\nNb Frequent Sequences : " + WAutomaton.nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem);
        System.out.println("Min Supp: " + min_supp + "\nNb Frequent Sequences: " + WAutomaton.nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem);
        automate.writer.close();
    }
}