import java.util.BitSet;
public class IState extends State {

    public BitSet prior, follow;                 /* The set of local previous and global next items */
    public int weight = 1;

    public IState () {
        super(true);
        prior = new BitSet(); 
        follow = new BitSet();
    }
    
    public int getWeight(){
        return weight;
    }

    public void setWeight(int w){
        weight = w;
    }
    
    public BitSet getprior(){
        return prior;
    }

    public void setprior(BitSet s){
        prior.or(s);
    }

    public BitSet getfollow(){
        return follow;
    }

    public void setfollow(BitSet b){
        follow.or(b);
    }

    public String toString() {
        return " ( "+ getType()+", "+getStart()+", "+getEnd()+"; w = "+getWeight() //+
        //" follow: "+follow+" prior: "+prior
        +" )" ;
    }
}