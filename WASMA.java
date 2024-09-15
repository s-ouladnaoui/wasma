import java.io.*;
import java.util.*;

/* The dataset represented by a Weighted Automaton */
public class WASMA {
    static final String itemSeparator = " ";                            /* separator in the input ds*/
    static final int itemsetDelimiter = -1,transactionDelimiter = -2;   /* the endmark of an itemset/a sequence */
    static boolean STATE_EXISTENCE_CHECK, PRINT_PATTERNS;
    static int min_supp = 1, nbFreqSequences = 0, gcode = -1, lcode = 0, NbTransactions = 0; /* the support threshold, number of frequent sequences in the dataset,  start code for reachability queries, number of transactions */
    static BufferedWriter writer;                                       /* for output */
    static Stack<Integer> stk = new Stack<>();                          /* used in printing the result:  */
    static Automaton<State> NFA;                                        // the weighted Automaton representing the dataset at loading 
    static Automaton<Node> DFA;                                      // Deterministic equivalent of the above NFA by subset construction contains the frquent sequential patterns of the dataset
    static ArrayDeque<Integer> DFAqueue;                                 /* the set of states of the weighted dfa and the queue of the same objects used during Determinization */
    static Stack<PNode> Queue;                                          /* the queue for Determinization without state existence Check*/
    static int NFAStartState = 0, DFAStartState = 0;                     /* initial states of the wnfa/wdfa */
    static HashMap<Integer,ArrayList<State>> itemStates = new HashMap<Integer,ArrayList<State>>();   // Vectors of states per item in local order
    static HashMap<Integer,Integer> Order = new HashMap<>();  // local order for each item associated set of state 
    static HashMap<Integer,HashMap<List<State>,Integer>> DFAmap = new HashMap<>();
    static BitSet fItems = new BitSet();

    public WASMA(int ms) {
        NFA = new Automaton<State>();  
        NFA.newState(NFAStartState, new delimState()); 
        DFA = new Automaton<Node>(); 
        DFA.newState(DFAStartState, new Node());   
        DFAqueue = new ArrayDeque<Integer>();
        min_supp = ms;
        Queue = new Stack<PNode>();
    }
    /* Each state of the wNFA has a double integer code (start & end) used for descendence (reachability) check */
    public void encode (int s) {     
        State ss = NFA.State(s);
        int item = ss.getItem();
        if (item != itemsetDelimiter) ((itemState)ss).setDelim((itemStates.get(itemsetDelimiter) == null)?0:itemStates.get(itemsetDelimiter).size()); 
        if (item == itemsetDelimiter || fItems.get(item)) {    
            ss.setStart(++gcode);
            if (itemStates.containsKey(item)) {
                itemStates.get(item).add(ss);
            } else {
                ArrayList<State> r = new ArrayList<State>();
                r.add(ss);
                itemStates.put(item, r);
            }   
            if (!Order.containsKey(item)) {
                Order.put(item,0);
                lcode = 0;
            }
            else {
                lcode = Order.get(item)+1;  // recuperate the current order of the item states 
                Order.put(item, lcode);
            }
            ss.setOrder(lcode);
            ss.setRoot(NFA.State(ss.getRoot()).getOrder());
            if (item != itemsetDelimiter && !((delimState)itemStates.get(itemsetDelimiter).get(ss.getRoot())).map.containsKey(item)) {
                ((delimState)itemStates.get(itemsetDelimiter).get(ss.getRoot())).map.put(item,ss.getOrder());
            }
        }
        for (int q : NFA.getTransitions(s).values())  encode(q);
        if (item == itemsetDelimiter || fItems.get(item)) {
            ss.setEnd(gcode);
            ss.setlEnd(Order.get((ss.getType()?-1:ss.getItem())));
            if (item == itemsetDelimiter && !NFA.getTransitions(s).isEmpty())
                for (int i:((delimState)itemStates.get(itemsetDelimiter).get(ss.getOrder())).map.keySet()) 
                    if (!((delimState)itemStates.get(itemsetDelimiter).get(ss.getRoot())).map.containsKey(i)) 
                        ((delimState)itemStates.get(itemsetDelimiter).get(ss.getRoot())).map.put(i,((delimState)itemStates.get(itemsetDelimiter).get(ss.getOrder())).map.get(i));
            }
    }


    public long NbAnti_chains() {  // computation of the set of antichains of the dataset
        long r = 0,nb = 1;
        int  j, t;
        State s;
        for (int i:WASMA.itemStates.keySet()){
            //r += Math.pow(2,WASMA.itemStates.get(i).size());
            if (i < 0) continue;
           // System.out.println("Size = "+i+": "+WASMA.itemStates.get(i).size());
            t = -1;
            nb = 1;
            for (j = 0; j < WASMA.itemStates.get(i).size();j++) {
                s = WASMA.itemStates.get(i).get(j);
             if (s.getOrder() == s.getlEnd()) {
                nb = nb*(j-t+1); 
                t = j;
             }
            }
            r += nb;
            //System.out.println(nb);
         }
        // if (nb < 0 || r < 0) STATE_EXISTENCE_CHECK = false;
        return r;
    }  
    
    /* ====================================== The Dataset Loader  (construction of the wNF Automaton) ========================================================*/
    public void loadData(String inputfile) throws IOException {
        int  p, q, current_root = NFAStartState,            // different working variables
        z, currentItemset, item, f;    
        itemState k;
        BufferedReader in = new BufferedReader(new FileReader(inputfile));
        Stack<Integer> StateStack = new Stack<>();              // to track the follow items (as a bitsets)
        HashSet<Integer> members = new HashSet<>();
        HashMap<Integer,Integer> vState;    
        HashMap<Integer, Integer> Alphabet = new HashMap<Integer,Integer>();
        String transaction;
        String[] items;
        p = current_root;
        while ((transaction = in.readLine()) != null) { 
            vState = new HashMap<Integer,Integer>();
            ArrayList<BitSet> sequenceItemList = new ArrayList<BitSet>();
            sequenceItemList.add(new BitSet());
            currentItemset = 0;
            items = transaction.split(itemSeparator);
            for (String ch : items) {
                item = Integer.parseInt(ch);
                switch (item) {
                    case transactionDelimiter:
                        NbTransactions++;
                        // This loop collect follows items in a bottom up fashion 
                        sequenceItemList.remove(currentItemset);
                        while (--currentItemset >= 0) {
                            for(int i = sequenceItemList.get(currentItemset).previousSetBit(sequenceItemList.get(currentItemset).size());i >= 0;i = sequenceItemList.get(currentItemset).previousSetBit(i - 1))
                                if (vState.get(i) == transactionDelimiter) {  // first appearition of i in the transaction
                                    int m = StateStack.pop();
                                    k = (itemState) NFA.State(m);
                                    vState.put(i,m);
                                    k.setFollow(sequenceItemList.get(currentItemset));
                                    // track the item apprearance per transactiob to build fItems bitset of frequent items (F1)
                                    if (Alphabet.get(i) == null) {
                                        Alphabet.put(i, 1);
                                        if (min_supp <= 1) fItems.set(i);  
                                    } else {
                                        f = Alphabet.get(i)+1;
                                        Alphabet.put(i, f);
                                        if (!fItems.get(i) && f >= min_supp) fItems.set(i);
                                    }
                                }
                                else {
                                    z = StateStack.pop();
                                    k = (itemState) NFA.State(z);
                                    k.setFollow(sequenceItemList.get(currentItemset));
                                    BitSet bs = new BitSet();
                                    bs.set(Math.min(i+1,((itemState) NFA.State(vState.get(i))).getFollow().length()), ((itemState) NFA.State(vState.get(i))).getFollow().length());
                                    bs.and(((itemState) NFA.State(vState.get(i))).getFollow());
                                    k.setFollow(bs); // the next follow items 
                                    vState.put(i,z);
                                }
                        }
                        p = current_root = NFAStartState;
                        members.clear();
                        break;
                    case itemsetDelimiter : 
                    sequenceItemList.add(new BitSet());
                        if (NFA.getTransitions(p).containsKey(item)) q = NFA.getTransitions(p).get(item);
                        else {
                            NFA.newState(q = NFA.newTransition(p,item), new delimState());
                            NFA.State(q).setRoot(current_root);
                        }
                        p = current_root = q;
                        currentItemset++;
                        break;
                    default :
                        members.add(item);
                        sequenceItemList.get(currentItemset).set(item); 
                        if (!vState.containsKey(item)) vState.put(item, transactionDelimiter);
                        if (NFA.getTransitions(p).containsKey(item))  q = NFA.getTransitions(p).get(item);
                        else {
                            NFA.newState(q = NFA.newTransition(p,item), new itemState(item));
                            NFA.State(q).setRoot(current_root);
                        }
                        ((itemState)NFA.State(q)).setWeight(1);
                        StateStack.push(q);
                        p = q;
                    }
                }
            }
            in.close();
            // Set the global/local start and end codes for the NFA states they will be used in DELTA Computation (reachability checking)
            encode(NFAStartState);
            /* ==================== Preparation of the Determinization: creation of the first states of the DFA =============================================================*/       
            NFA.State(DFAStartState).setRoot(DFAStartState);
            if (STATE_EXISTENCE_CHECK) { // Creation of the first states of te DFA
                DFAqueue.add(DFAStartState);
                DFA.State(DFAStartState).states.add(NFA.State(NFAStartState));
                Node source_state, res, res_delimiter;
                int r1,r2, index;
                DFAqueue.remove();
                source_state = DFA.State(DFAStartState);
                DFAmap.put(itemsetDelimiter, new HashMap<>());    
            /*==============   res = delta(s,i)  ====================================  */         
                for(int it=fItems.nextSetBit(0);it >= 0; it = fItems.nextSetBit(it+1) ) {
                    res = new Node(it); //  res is a new dfa state 
                    itemState r = (itemState) WASMA.itemStates.get(it).get(((delimState)WASMA.itemStates.get(WASMA.itemsetDelimiter).get(NFA.State(NFAStartState).getOrder())).map.get(it));
                    while (r.getEnd() <= NFA.State(NFAStartState).getEnd()) {
                        res.states.add(r);
                        index = r.getlEnd() + 1;
                        if (index < WASMA.itemStates.get(it).size()) r = (itemState) WASMA.itemStates.get(it).get(index);
                        else break;
                    }
                    res.setSupport(Alphabet.get(it));
                    if (res.getSupport() >= min_supp) {             // res = delta(s,i) is frequent 
                        res.getPattern().or(source_state.getPattern());                      
                        DFAmap.put(it, new HashMap<>());             
                        DFA.newState(r1 = DFA.newTransition(DFAStartState, it), res);    // r1 the id number of the state res = delta(s,i)
                        res.setRef(DFAStartState);
                        DFAqueue.add(r1);     
                        DFAmap.get(it).put(res.getStates(),r1);  // add res to the DFA map state using its fingerprint
                        //=============  res_delimiter = delta(res,itemsetDelimiter(#))  ===============================================================
                        res_delimiter = res.terminateSequence();
                        DFA.newState(r2 = DFA.newTransition(r1, itemsetDelimiter),res_delimiter); // r2 the id number of the state res_delimiter = delta(res,#)
                        res_delimiter.setRef(res.getRef());                                   
                        DFAqueue.add(r2); // Add the 2 new states to the determinization queue
                        DFAmap.get(itemsetDelimiter).put(res_delimiter.getStates(),r2);
                    } else itemStates.remove(it);
                }
            } else {
                PNode initial_pnode = new PNode();
                initial_pnode.states.add(NFA.State(NFAStartState)); 
                Queue.add(initial_pnode);
            }
            //System.out.println("NB AC: "+NbAnti_chains());
           // long endTime = System.nanoTime();
           // writer.write("Database: " + inputfile + "; Alphabet size: " + alphabet.size() + "; Database size: " + NbTransactions + "\n");     
            //writer.write("Preprocessing time: " + (endTime-startTime)/1000000 + " ms\nNFA States: "+NFA.NbStates+"\n");
            System.out.println("Database: " + inputfile + "; Alphabet size: " + Alphabet.size() + "; Database size: " + NbTransactions);
           // System.out.println("Preprocessing time: " + (endTime-startTime)/1000000 + " ms\nNFA States: "+NFA.NbStates);
            WASMA.NFA = null; WASMA.Order = null; Alphabet = null; fItems = null; members = null;// we don't need the NFA all the required information are in the itemstate map            
    }

    public void Determinize_without_State_Existence_Check() throws IOException {
        PNode source_state, res, res_delimiter;
        HashMap<Integer,PNode> resultat;
        while (!Queue.isEmpty()) {
            source_state = Queue.pop();
            if (source_state.getStates().isEmpty()) continue;     
/*================================================   res = delta(s,i)  ================================================================*/           
            resultat = source_state.Delta();
            for(int i:resultat.keySet()) {
                res = resultat.get(i);
                if (res.getSupport() >= min_supp) {          /* res = delta(s,i) is frequent */
                    res.Pattern = new ArrayList<Integer>(source_state.getPattern());
                    res.getPattern().add(i);
                    Queue.push(res);     // add res to the DFA map state using its fingerprint
/*===================================================  res_delimiter = delta(res,itemsetDelimiter(#))  ===============================================================*/
                    res_delimiter = res.terminateSequence();
                    res_delimiter.Pattern = new ArrayList<>(res.getPattern());
                    res_delimiter.getPattern().add(itemsetDelimiter);
                    res_delimiter.setSupport(res.getSupport());      //  res and res_delimter have the same support (patterns sprt(p)= sprt(p#))
                    Queue.push(res_delimiter); // if the 2 new states are extensibles add them to the queue
                    if (PRINT_PATTERNS) writer.write(res_delimiter.getPattern().toString()+" : "+res.getSupport()+"\n");              
                    nbFreqSequences++;
                } else res = null;
            }
        }
    }

    public void Determinize_with_State_Existence_Check() {
        Node source_state, res, res_delimiter;  //   res = delta(source_state,item) ; res_delimiter = delta(res,#)
        int s, r1, r2, ref;                       // i : item of the extension// state ids           s: id of source_state; r1: id of res; r2: id of red_delimiter
        HashMap<Integer,Node> resultat;
        DFAmap.put(itemsetDelimiter, new HashMap<>());
        while (!DFAqueue.isEmpty()) {
            s = DFAqueue.remove();
            source_state = DFA.State(s);         
            if (source_state.getStates().isEmpty()) continue;     
/*================================================  resultat = delta(s) and  res = delta(s,i)  ================================================================*/           
            resultat = source_state.Delta(); // resultat takes all the delta(source_state,i) where i frequent and in foloow of source_state
            ref = source_state.getRef();
            for (int i:resultat.keySet()) {
                res = resultat.get(i);
                if (res.getSupport() >= min_supp) {             /* res = delta(s,i) is frequent */
                    res.getPattern().or(source_state.getPattern());                      
                    if (DFAmap.get(i) == null) DFAmap.put(i, new HashMap<>());             
                    if (!DFAmap.get(i).containsKey(res.getStates())) {          /*  res is a new dfa state */
                        DFA.newState(r1 = DFA.newTransition(s, i), res);    // r1 the id number of the state res = delta(s,i)
                        res.setRef(source_state.IsDelimiterState()? s : ref);
                        DFAmap.get(i).put(res.getStates(),r1);  
                        DFAqueue.add(r1);     // add res to the DFA map state using its fingerprint
/*===================================================  res_delimiter = delta(res,itemsetDelimiter(#))  ===============================================================*/
                        res_delimiter = res.terminateSequence(); // terminate state ie, res_delimiter = delta(res,itemsetDelimiter(#))
                        if(!DFAmap.get(itemsetDelimiter).containsKey(res_delimiter.getStates())) {
                            DFA.newState(r2 = DFA.newTransition(r1, itemsetDelimiter), res_delimiter); // r2 the id number of the state res_delimiter = delta(res,#)
                            res_delimiter.setRef(res.getRef());                                    
                            DFAqueue.add(r2); // if the 2 new states are extensibles add them to the queue
                            DFAmap.get(itemsetDelimiter).put(res_delimiter.getStates(),r2);  
                        } else DFA.newTransition(r1, itemsetDelimiter,DFAmap.get(itemsetDelimiter).get(res_delimiter.getStates()));     
                    } else DFA.newTransition(s, i,DFAmap.get(i).get(res.getStates())); // res already exists in the DFA so we do not create it but insert a new transition labeled i from s to it  
                } else res = null;       // res is infrequent so mark it. gc can recuperate its memory 
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int mins = Integer.parseInt(args[0]);      // min support
        if (mins < 1) {
            System.out.println("Absolute Min supp should be >= 1");
            mins = 1;
        }
        WASMA spm = new WASMA(mins);            // a new instance of the sequential pattern mining algo
        writer = new BufferedWriter(new FileWriter(args[2]));       // the output file
        PRINT_PATTERNS = Boolean.parseBoolean(args[3]);             // print the collection of the frequent patterns 
        STATE_EXISTENCE_CHECK = Boolean.parseBoolean(args[4]);      // subset construction using or not state existence check
        long beforeUsedMem = Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory();
        long startTime = System.nanoTime();
        spm.loadData(args[1]);  
        if (STATE_EXISTENCE_CHECK) {
            System.out.println("with state test");
            spm.Determinize_with_State_Existence_Check();
            String endTime = String.format("%.2f ms",(System.nanoTime()-startTime)/1000000d);       
            long afterUsedMem =  Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
            String mem = String.format("%.2f mb",(afterUsedMem-beforeUsedMem)/1024d/1024d);
            System.out.println("Mining finished...printing in progress !! please wait for the output");
            writer.write("Min Supp: "  + min_supp + " (relative : "+String.format("%.3f",( (double) min_supp/NbTransactions))+")\n"+
                "DFA States: "+DFA.NbStates+"\n" );
            System.out.println("Min Supp: "  + min_supp + " (relative : "+String.format("%.3f",( (double) min_supp/NbTransactions))+")\n"+
                "DFA States: "+DFA.NbStates);                
            DFA.Print(DFAStartState,writer,PRINT_PATTERNS);
            writer.write("Nb Frequent Sequences: " + nbFreqSequences + "\nMining time: " + endTime +"\nMemory requirement: " + mem+"\n");
            System.out.println("Nb Frequent Sequences: "+nbFreqSequences+"\nMining time: " + endTime +"\nMemory requirement: " + mem+"\n");
        } else {
            System.out.println("Sans state test");
            spm.Determinize_without_State_Existence_Check();
            String endTime = String.format("%.2f ms",(System.nanoTime()-startTime)/1000000d);       
            long afterUsedMem =  Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
            String mem = String.format("%.2f mb",(afterUsedMem-beforeUsedMem)/1024d/1024d);
            writer.write("Min Supp: "  + min_supp + " (relative : "+String.format("%.3f",( (double) min_supp/NbTransactions))+")"
            + "\nNb Frequent Sequences: " + nbFreqSequences +"\nMining time: " + endTime +"\nMemory requirement: " + mem+"\n");
            System.out.println("Min Supp: "  + min_supp + " (relative : "+String.format("%.3f",( (double) min_supp/NbTransactions))+")"
            + "\nNb Frequent Sequences: " + nbFreqSequences +"\nMining time: " + endTime +"\nMemory requirement: " + mem+"\n");
        }
        writer.close();
    }
}