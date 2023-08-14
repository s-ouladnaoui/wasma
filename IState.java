import java.util.BitSet;
public class IState extends State {

    public BitSet prior, follow;              /* The set of local previous and global next items */
    public int weight;                    /* as usually: the frequency(or weight) of the prefix from the root to this state */

    public IState () {
        super(true);
        prior = new BitSet(); 
        follow = new BitSet();
        weight = 0;
    }
    
    public int getWeight(){
        return weight;
    }

    public void setWeight(int w){
        weight = w;
    }
    
    public BitSet getPrior(){
        return prior;
    }

    public void setPrior(BitSet s){
        prior.or(s);
    }

    public BitSet getFollow(){
        return follow;
    }

    public void setFollow(BitSet b){
        follow.or(b);
    }

    public String toString() {
        return " ( "+ getType()+", "+getStart()+", "+getEnd()+"; w = "+getWeight() //+
        //" follow: "+follow+" prior: "+prior
        +" )" ;
    }
}