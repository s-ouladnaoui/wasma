import java.io.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
/* The dataset represented by a Weighted Automaton */
public class WAutomaton
{
    static ArrayList<State> wnfaStates;                       /* the set of states of the weighted nfa */
    static ArrayList<State> wdfaStates;                       /* the set of states of the weighted dfa */
    static IState startState = null;
    static int NbState = 0;                               /* number of states */
    int Nbtransaction = 1;                                /* number of transactions */
    ArrayList<DfaState> queue = new ArrayList<>();        /* Les états de l'automate déterministe */
    static final String itemSeparator = " ";
    static final int itemsetDelimiter = -1;               /* the endmark of an itemset */
    static final int transactionDelimiter = -2;           /* the endmark of a sequence */
    static int min_supp = 1;                              /* the support threshold */
    int nbFreqSequences = 0;                              /* number of frequent sequences in the dataset*/
    int code = 0;                                         /* start code for reachability queries */
    BufferedWriter writer ;                               /* for output */
    BitSet fItems ;                                       /* bitset of the frequent items (the set F1) */
    HashMap<Integer,ArrayList<State>> FrequentItems = new HashMap<>();

    public WAutomaton (int ms) {
        wnfaStates = new ArrayList<>();            
        wnfaStates.add(startState = new IState());           // the initial state (root) of the weighted automaton
        min_supp = ms;
        fItems = new BitSet();
    }

    /*public static State toState(int i) {       // from a state id to the associated object
        return states.get(i);
    }

    public static Set<State> toState(Set<Integer> S) {   // from state ids to state objects
        Set<State> r = new TreeSet<>();
        for (int s:S) {
            r.add(toState(s));
        }
        return r;
    }

    public static Set<IState> toIState(Set<Integer> S) {   // from state ids to state objects
        Set<IState> r = new TreeSet<>();
        for (int s:S) {
            r.add((IState)toState(s));
        }
        return r;
    }*/
    public Set<State> rootSet(Set<State> P) {            // dustinct/unique roots of a stateset
        TreeSet<State> r = new TreeSet<State>();
        for (State p:P) {
            if (!r.contains(p.getRoot())) r.add(p.getRoot());
        }
        return r;
    }
    public  String toString() {                         /* Print the automaton */
        String ch = "";
        int i = 0;
        for (Object p:wnfaStates) {
            ch = ch+i+" : "+p.toString()+"\n";
            i++;
        }
        return ch;
    }

    // each state has a double integer code (start & end) used for reachability check
    public void codage (State s) {     
        State p = s;
        p.setStart(code++);
        for (State q  : p.getTransitions().values())
            codage(q);
        p.setEnd(code++);
    }

    /* ====================================== dataset loader ========================================================*/
    public void loadData(String inputfile) throws IOException {
        State  p,q;
        BufferedReader in = new BufferedReader(new FileReader(inputfile));
        //Stack<State> S = new Stack<>();            // to track the follow items (as a bitsets)
        IState current_root = startState;
        p = current_root;
        HashMap<Integer, Integer> alphabet = new HashMap<>();    /* The set of items in the dataset and the associated supports */
        HashSet<Integer> members = new HashSet<>();
        BitSet currentMap = new BitSet();
        String transaction;
        long startTime = System.nanoTime();
        while ((transaction = in.readLine()) != null)
        {
            String[] items = transaction.split(itemSeparator);
            for (String ch : items) {
                int item = Integer.parseInt(ch);
                switch (item) {
                    case transactionDelimiter:
                        Nbtransaction++;
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
                        p = current_root = (IState) startState;
                        members.clear();
                        break;
                    case itemsetDelimiter :
                        IState qq; 
                        if (p.getTransitions().containsKey(item)) {
                            qq = (IState) p.getTransitions().get(item);
                        } else {
                            ++NbState;
                            qq = new IState();
                            wnfaStates.add(qq);
                            p.addTransition(item, qq);
                            qq.setRoot(current_root);
                            if (FrequentItems.containsKey(item)) FrequentItems.get(item).add(qq);
                            else {
                                ArrayList<State> ss = new ArrayList<State>();
                                ss.add(qq);
                                FrequentItems.put(item, ss);
                            }
                        }
                        qq.setWeight(qq.getWeight() + 1);
                        qq.setpreviousLocalItems(currentMap);
                        currentMap.clear();
                        //current_root.addItem(item, qq);
                        current_root = qq;
                        p = qq;
                        break;
                    default:
                        members.add(item);          // add the item to the alphabet
                        currentMap.set(item);       // set the bit of the item 
                        if (p.getTransitions().containsKey(item)) {
                            q = p.getTransitions().get(item);
                        } else {
                            ++NbState;
                            q = new State(false);
                            wnfaStates.add(q);
                            p.addTransition(item, q);
                           // if (!p.getType()) current_root.addItem(item, q);
                            q.setRoot(current_root);
                            if (FrequentItems.containsKey(item)) FrequentItems.get(item).add(q);
                            else {
                                ArrayList<State> ss = new ArrayList<State>();
                                ss.add(q);
                                FrequentItems.put(item, ss);
                            }
                        }
                        
                        p = q;
                }
            }
        }
        in.close();
        long endTime = System.nanoTime();
        writer.write("Database: " + inputfile + "; Alphabet size: " + alphabet.size() + "; Database size: " + (Nbtransaction-1) + "\n");
        writer.write("Loading time: " + (endTime-startTime)/1000000 + " ms\n");
        System.out.println("Database: " + inputfile + "; Alphabet size: " + alphabet.size() + "; Database size: " + (Nbtransaction-1));
        System.out.println("Loading time: " + (endTime-startTime)/1000000 + " ms");
        // remove from the hashmap the states of infrequent items
        for (int i = fItems.nextClearBit(0); i > 0; i = fItems.nextClearBit(i + 1)) 
            FrequentItems.remove(i);
    }    

    /*public Set<State> Aligner(Set<State> P, Set<State> Q) {
        Set<State> r = new TreeSet<State>();
        Iterator<State> pit = P.iterator();
        Iterator<State> qit = Q.iterator();
        State p = pit.next();
        State q = qit.next();
        for (;;) // intresect delimiters with the result
        {
            int z = p.compareTo(q);
            if (z < 0) {
                r.getDelimiters().remove(q);
                r.getRest().add(q);
                if (qit.hasNext()) q = qit.next();
                else break;
            } else if (z == 0) {
                if (qit.hasNext()) q = qit.next();
                else break;
            } else {
                if (pit.hasNext()) p = pit.next();
                else {
                    for (;;) // continue with the rest of the delimiters if it is non-empty
                    {
                        r.getDelimiters().remove(q);
                        r.getRest().add(q);
                        if (qit.hasNext()) q = qit.next();
                        else break;
                    }
                    break;
                }
            }
        }
        return r;
    }

    public DState delta_s(State p, int a) {   // delta from one internal state by an item a
        DState r = new DState();
        Set<Integer> s = p.getTransitions().keySet();
        for (int i : s) {
            if (i >= 0 && i < a) {
                DState z = delta_s(p.getTransitions().get(i),a);
                if (!z.getEtats().isEmpty()) {
                    r.setEtats(z.getEtats());
                    r.setSupport(r.getSupport()+z.getSupport());
                }
            } else if (i == a) {
                r.getEtats().add(p.getTransitions().get(i));
                r.setSupport(r.getSupport()+ p.getTransitions().get(i).getWeight());
            } else break;
        }
        return r;
    }

    public DfaState delta_s(DfaState P, int a) { // delta from internal stateset by an item a
        DfaState r = new DfaState();
        r.setDelimiters(P.getDelimiters());
        for (State p : P.getEtats()) {
            DState z = delta_s(p,a);
            if (!z.getEtats().isEmpty()) {
                r.setEtats(z.getEtats());
                r.setSupport(r.getSupport()+z.getSupport());
            }
        }
        return  Aligner(r);
    }

    public  DState delta_i(State p,  int a) { // delta from one itemset-delimiter state
        DState r = new DState();
        if (p.getTransitions().containsKey(a)) { // Check if direct transitions contain the item a
            State tmp = p.getTransitions().get(a);
            r.getEtats().add(tmp);
            r.setSupport(r.getSupport() + tmp.getWeight());
        }
        if (((IState) p).getItransitions().containsKey(a)) { // Check if the map of the root state (indirect transitions) contains the item a
            for (State q : ((IState) p).getItransitions().get(a)) {
                    r.getEtats().add(q);
                    r.setSupport(r.getSupport() + q.getWeight());
            }
        }
        return r;
    }

    public  DfaState delta_i(DfaState P,  int a) {   // delta from a stateset itemset-delimiter
        DfaState r = new DfaState();
        for (State p : P.getEtats()) {
            if (((IState) p).getFollow().get(a)) {
                r.setDelimiters(((IState) p).getItransitions().get(itemsetDelimiter));
                DState z = delta_i(p, a);
                if (!z.getEtats().isEmpty()) {
                    r.setEtats(z.getEtats());
                    r.setSupport(r.getSupport() + z.getSupport());
                }
            }
        }
        return Aligner(r);
    }

    public DfaState delta(DfaState E, int a) {
       DfaState r = new DfaState(a);
       if (E.getEtats().first().getType()){  //==== delta from itemsetdelimiters (sequence extension) =====
        for (State e:E.getEtats()){ 
            r.getEtats().addAll(((IState) e).Delta(a,true).getEtats());}
        }
        else { //==== delta  inside the itemsets ====
        Set<State> Q = new TreeSet<State>(); 
        for (State p : rootSet(E.getEtats())) 
           Q.addAll(((IState)p).Delta(a,true).getEtats());
        r.getEtats().addAll(Aligner(E.getEtats(), Q));
        }
        return r;
    }
    




       if (E.getEtats().isEmpty() || w.size() == 0) return r;
       else if (w.size() == 1) {
                    //================ delta from itemsetdelimiters ====================
           if (E.getPattern().size() == 0 || E.getPattern().get(E.getPattern().size()-1) == itemsetDelimiter)  // compute delta(P,a) = Q  from the Maps of the states p of P which are itemsetdelimiters
                r = delta_i(E,w.get(0));
           else     //================ delta inside itemsets  =====
                r = delta_s(E,w.get(0));
                //===============  difference between delimiters and result: the next itemsets to begin with but from the start of the last itemset
           if (!r.getRest().isEmpty()) {
               DfaState t = new DfaState();
               for (State e : r.getRest())
                   if (!(((IState)e).getFollow().isEmpty())) {
                       t.getEtats().add(e);
                       t.setFollow(((IState)e).getFollow());
                   }
               if (t.getFollow().get(w.get(0))) {
                   ArrayList<Integer> param = new ArrayList<>();
                   param.addAll(E.getPattern().subList(E.getPattern().lastIndexOf(itemsetDelimiter)+1,E.getPattern().size()));
                   param.add(w.get(0));
                   r.getRest().clear();
                   DfaState z = delta(t,param);
                   if (!z.getEtats().isEmpty()) {
                       r.setEtats(z.getEtats());
                       r.setSupport(r.getSupport() + z.getSupport());
                   }
                   r.setDelimiters(z.getDelimiters());
               }
           }
       } else {
           DfaState z = delta(delta(E, w.subList(0,1)),  w.subList(1, w.size()));
           if (!z.getEtats().isEmpty()) {
               r.setEtats(z.getEtats());
               r.setSupport(r.getSupport() + z.getSupport());
           }
           r.setDelimiters(z.getDelimiters());
       }
       if (!r.getEtats().isEmpty()) {
           r.setPattern(E.getPattern());
           r.extendPattern(w.get(0));
       }
       return r;

    public void extendState(DfaState s,  int i) {
        ArrayList<Integer> motif = new ArrayList<>();
        motif.add(i);
        DfaState ns = delta(s, motif); // ns : new state = delta(s,i:item)
        ns.setFollow(s.getFollow());
        ns.getFollow().clear(i);
        if (ns.getSupport() >= min_supp) {
            DfaState ds = new DfaState();  // ds : delimiter state = delta (ns, itemsetdelimiter)
            for (State p : ns.getDelimiters()) {
                if (!((IState)p).getFollow().isEmpty()) ds.getEtats().add(p);
                ds.setFollow(((IState)p).getFollow());
            }
            ds.setSupport(ns.getSupport());
            ds.setPattern(ns.getPattern());
            ds.extendPattern(itemsetDelimiter);
            queue.add(ns);
            queue.add(ds);
            nbFreqSequences++;
            System.out.println(ds);
            try {
                writer.write(ds.toString());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    public void Determinize() {
        DfaState s = new DfaState();
        s.etats.add(startState);
        s.setFollow(((IState) startState).getFollow());
        queue.add(s);
        while (!queue.isEmpty()) {
            s = queue.remove(0);
            for (int i = s.getFollow().nextSetBit(0); i > 0; i = s.getFollow().nextSetBit(i + 1)) {
                if (fItems.get(i)) {
                    extendState(s,i); 
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
        automate.codage(startState);
        System.out.println(automate);
        automate.Determinize(); 
        long afterUsedMem =  Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        String mem = String.format("%.2f mb",(afterUsedMem-beforeUsedMem)/1024d/1024d);
        String endTime = String.format("%.2f ms",(System.nanoTime()-startTime)/1000000d);
        automate.writer.write("Min supp: " + min_supp + "\nNb Frequent Sequences : " + automate.nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem);
        System.out.println("Min supp: " + min_supp + "\nNb Frequent Sequences: " + automate.nbFreqSequences + "\nMining time: " + endTime + "\nMemory requirement: " + mem);
        automate.writer.close();
    }
}