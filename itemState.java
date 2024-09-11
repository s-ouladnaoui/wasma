/* Weighted NFA item State Class */

import java.util.HashSet;
import java.util.Set;
//import java.util.TreeSet;

public class itemState extends State {
    int item ;          /* The item of State */
    int weight;         /* the frequency of the prefix from the startstate to this state */ 
    int delimiter;       /* next Delimiter State */
     Set<Integer> previous, follow;              /* the following items in the NFAutomaton */

    public itemState(int i) { 
        item = i; 
        previous = new HashSet<Integer>();
        follow = new HashSet<Integer>();
        
    }
    
    public int getItem() { return item;}
    
    public boolean getType() { return false;}       /* flag: the state is an itemset delimiter when is true*/
    
    public int getDelim() { return delimiter;}

    public void setDelim(int d) { delimiter = d;}

    public int getWeight() { return weight;}

    public void setWeight(int w) { weight += w;}

    public Set<Integer> getFollow() { return follow;}

    public void setFollow(int b) { follow.add(b);}

    public Set<Integer> getPrevious() { return previous;}

    public void setPrevious(int b) { previous.add(b);}
}