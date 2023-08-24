import java.io.*;
import java.util.*;

/* The dataset represented by a Weighted Automaton */
public class WAutomaton {

    static ArrayList<State> wNFAStates;                   /* the set of states of the weighted nfa */
    static ArrayList<DfaState> DFAqueue;          /* the set of states of the weighted dfa and the queue of the same objects used during Determinization */
    static HashMap<BitSet,DFAs> DFAStateMap;
    static IState wNFAStartState;                         /* the initial state of the wnfa */
    static DfaState wDFAStartState;                       /* the initial state of the wnfa */

    static final String itemSeparator = " ";
    static final int itemsetDelimiter = -1;               /* the endmark of an itemset */
    static final int transactionDelimiter = -2;           /* the endmark of a sequence */

    static int NbState = 0;                               /* number of states */
    static int min_supp = 1;                              /* the support threshold */

    static int nbFreqSequences = 0;                              /* number of frequent sequences in the dataset*/
    int code = 0;                                         /* start code for reachability queries */
    int NbTransactions = 0;                                /* number of transactions */
    BufferedWriter writer ;                               /* for output */


    public WAutomaton (int ms) {
        wNFAStates = new ArrayList<>();   
        wNFAStates.add(wNFAStartState = new IState());        // the initial state of the weighted nfa
        wDFAStartState = new DfaState(0);      // the initial state of the weighted dfa 0 is the item coressponding to epsilon
        wDFAStartState.getStates().put(wNFAStartState, new TreeSet<State>());
        DFAStateMap = new HashMap<>();
        DFAqueue = new ArrayList<DfaState>();
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
    // for reachability between two sets of states align them and check the descendance relation 
    static public void Align(Iterator<State> xit, Iterator<State> yit, DfaState s, boolean jump) {
        State x = xit.next();
        State y = yit.next();
        do {
            if (x.getEnd() < y.getStart())  { if (xit.hasNext()) x = xit.next(); else break;}
            else if (y.getEnd() < x.getStart() || x.getStart() > y.getStart() && x.getEnd() < y.getEnd()) { if (yit.hasNext()) y = yit.next(); else break;}
            else {
                if (!jump) s.addState(y);
                else if (x.getRoot() == y.getRoot()) s.addState(y);
                if (yit.hasNext()) y = yit.next(); else break;
            }
        } while (true);
    }

    // clear already processed local next items 
    static public BitSet clearBs(BitSet bs, int i) {
        for (int j = bs.nextSetBit(0); j > 0; j = bs.nextSetBit(j + 1)) {
            if (j <= i) bs.clear(j); 
        }
        return bs;
    }

    /* ====================================== dataset loader ========================================================*/

    public void loadData(String inputfile) throws IOException {
        State  p,q;
        IState current_root = wNFAStartState;
        p = current_root;
        BufferedReader in = new BufferedReader(new FileReader(inputfile));
        Stack<IState> StateStack = new Stack<>();            // to track the follow items (as a bitsets)
        StateStack.push(wNFAStartState);
        HashMap<Integer, Integer> alphabet = new HashMap<>();    /* The items of the dataset and the associated supports */
        HashSet<Integer> members = new HashSet<>();
        BitSet currentItems = new BitSet(),fItems = new BitSet();  /* bitset of the frequent items (the set F1) */
        HashMap<Integer,ArrayList<State>> lStates = new HashMap<Integer,ArrayList<State>>();
        String transaction;
        long startTime = System.nanoTime();
        while ((transaction = in.readLine()) != null)
        {
            String[] items = transaction.split(itemSeparator);
            for (String ch : items) {
                int item = Integer.parseInt(ch);
                switch (item) {
                    case transactionDelimiter:
                        NbTransactions++;
                        // This loop collect next items for each IState in a bottom up fashion 
                        IState ss1 = StateStack.pop();
                        while (!StateStack.isEmpty()){
                            IState ss2 = StateStack.pop();
                            ss2.setFollow(ss1.getFollow());
                            ss2.setFollow(ss1.getPrior());
                            ss1 = ss2;
                        }
                        int tmp;
                        for (Integer k : members) {
                            Integer f = alphabet.get(k);
                            if (f == null) {
                                alphabet.put(k, 1);
                            } else {
                                alphabet.put(k, f + 1);
                                tmp = f + 1;
                                if (!fItems.get(k) && tmp >= min_supp) fItems.set(k);
                            }
                        }
                        p = current_root = (IState) wNFAStartState;
                        members.clear();
                        break;
                    case itemsetDelimiter :
                        IState qq; 
                        if (p.getTransitions().containsKey(item)) {
                            qq = (IState) p.getTransitions().get(item);
                        } else {
                            ++NbState;
                            qq = new IState();
                            wNFAStates.add(qq);
                            p.addTransition(item, qq);
                            qq.setRoot(current_root);
                            if (lStates.containsKey(item)) lStates.get(item).add(qq);
                            else {
                                ArrayList<State> r = new ArrayList<State>();
                                r.add(qq);
                                lStates.put(item, r);
                            }
                        }
                        qq.setWeight(1);
                        qq.setPrior(currentItems);
                        StateStack.push(qq);
                        currentItems.clear();
                        current_root = qq;
                        p = qq;
                        break;
                    default:
                        members.add(item);              // add the item to the alphabet
                        currentItems.set(item);         // set the bit of the item 
                        if (p.getTransitions().containsKey(item)) {
                            q = p.getTransitions().get(item);
                        } else {
                            ++NbState;
                            q = new State(false);
                            wNFAStates.add(q);
                            p.addTransition(item, q);
                            q.setRoot(current_root);
                            if (lStates.containsKey(item)) lStates.get(item).add(q);
                            else {
                                ArrayList<State> r = new ArrayList<State>();
                                r.add(q);
                                lStates.put(item, r);
                            }
                        }
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
            /*  ======== Preparation of the Determinization: creation of the first states of the DFA ===================================== */       
            // set the start and end code values for the states of the NFA
            codage(wNFAStartState);
            // add the transition from wDFAStartState by the itemsetdelimiter  (-1 here)
            DfaState s = new DfaState(itemsetDelimiter);
            for (State d:lStates.get(itemsetDelimiter)){
                s.addState(d);
            }
            wDFAStartState.addTransition(itemsetDelimiter, s);
            // prepare the first states of the DFA: the set of transitions from the initial state of the DFA by the frequent items
            for (int i = fItems.nextSetBit(0); i > 0; i = fItems.nextSetBit(i + 1)) {
                s = new DfaState(i);
                s.setSupport(alphabet.get(i));
                for (State d:lStates.get(i)){
                    s.addState(d);
                }
                wDFAStartState.addTransition(i, s);
                s.setPattern(i); 
                DFAqueue.add(s);
                nbFreqSequences++;
                DfaState r = s.delta(itemsetDelimiter);
                DFAqueue.add(r);
                System.out.println(0+" => "+i+" = "+s+": "+r.getPattern() +" fréquent: "+r.getSupport());
            }
        }    

    public void Determinize() {
        DfaState s;
        while (!DFAqueue.isEmpty()) {
            s = DFAqueue.remove(0);
            for (int i = s.getFollow().nextSetBit(0); i > 0; i = s.getFollow().nextSetBit(i + 1)) {
                DfaState r1 = s.delta(i);
                DfaState r2 = r1.delta(itemsetDelimiter);
                int sprt = r2.getSupport();
                if (sprt >= min_supp) {
                    BitSet bs1 = r1.listStateBits();
                    BitSet bs2 = r2.listStateBits();
                    if (!DFAStateMap.containsKey(bs1)){
                        DFAs newDFAState1 = new DFAs(bs1,sprt);
                        DFAs newDFAState2 = new DFAs(bs2,sprt);
                        newDFAState1.next.put(itemsetDelimiter, newDFAState2);
                        DFAStateMap.put(bs1, newDFAState1);
                        DFAStateMap.put(bs2, newDFAState2);
                        DFAStateMap.put(s.listStateBits(), new DFAs(s.listStateBits(),s.getSupport()));
                        DFAStateMap.get(s.listStateBits()).next.put(i, newDFAState1);
                        //System.out.println(s+" => "+i+" = "+r1+": "+r2.getPattern() +"fréquent\n");
                        System.out.println(s+" => "+i+" = "+r1+": "+r2.getPattern() +" fréquent: "+sprt);
                        nbFreqSequences++;

                        if (!r1.getFollow().isEmpty()) DFAqueue.add(r1);
                        if (!r2.getFollow().isEmpty()) DFAqueue.add(r2);
                    } else {
                        DFAStateMap.get(bs1).listFrom(r1.getPattern());                     
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
        //automate.codage(wNFAStartState);
        //System.out.println(wDFAStartState.getTransitions().get(1).delta(3));
        automate.Determinize(); 
        long afterUsedMem =  Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        String mem = String.format("%.2f mb",(afterUsedMem-beforeUsedMem)/1024d/1024d);
        String endTime = String.format("%.2f ms",(System.nanoTime()-startTime)/1000000d);
        automate.writer.write("Min Supp: " + min_supp + "\nNb Frequent Sequences : " + WAutomaton.nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem);
        System.out.println("Min Supp: " + min_supp + "\nNb Frequent Sequences: " + WAutomaton.nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem);
        automate.writer.close();
    }
}