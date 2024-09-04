public class iState extends State {
    int item ;          /* The item of State */
    int weight;         /* the frequency of the prefix from the startstate to this state */ 
    int delimiter;       /* next Delimiter State */

    public iState(int i) { item = i; }
    
    public int getItem() { return item;}
    
    public boolean getType() { return false;}       /* flag: the state is an itemset delimiter when is true*/
    
    public int getDelim() { return delimiter;}

    public void setDelim(int d) { delimiter = d;}

    public int getWeight() { return weight;}

    public void setWeight(int w) { weight += w;}
}
