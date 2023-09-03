import java.util.BitSet;

// extended version of the State class for itemsetdelimiter states

public class IState extends State {
    BitSet prior, follow;          /* The set of local previous and global next items */
    
    public IState () {
        super(true);
        prior = new BitSet(); 
        follow = new BitSet();
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
}