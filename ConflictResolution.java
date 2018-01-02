import java.lang.reflect.Array;
import java.util.*;

public class ConflictResolution {
    int mUniqueNum;
    String mGoal;
    Vector<Operator> mOperators;
    Vector<Operator> confOperators;
    Vector<WorkingMemory> theCurrentState;
    HashMap<Operator,ArrayList<Integer>> Relation;
    HashMap<Operator,Operator> rename;
    Hashtable mBindings;
    boolean Lexflag;
    Vector<Operator> ConflictHistory ;

    public ConflictResolution(Vector<Operator> operators){
        mOperators = operators;
        Lexflag = true;
        confOperators = new Vector<>();
        Relation = new HashMap<Operator,ArrayList<Integer>>();
        rename = new HashMap<Operator,Operator>();
        ConflictHistory = new Vector<>();
        setConflictHistory();
    }
    void initState(Vector<WorkingMemory> currentstate,Hashtable binds,String goal,int uniq){
        confOperators.clear();
        Relation.clear();
        rename.clear();
        theCurrentState = (Vector<WorkingMemory>)currentstate.clone();
        mBindings = (Hashtable)binds.clone();
        mGoal = goal;
        mUniqueNum = -uniq;
        setConflict();
        setTag();
    }
    private void setConflict(){
        for(Operator op: mOperators){
            Operator newOperator = op.getRenamedOperator(mUniqueNum);
            mUniqueNum--;

            Vector<String> alist = newOperator.getAddList();
            for(int i = 0;i < alist.size();i++){
                if((new Unifier()).unify(mGoal,alist.get(i),mBindings)){
                    Operator cOp = newOperator.instantiate(mBindings);
                    confOperators.addElement(cOp);
                    rename.put(cOp,op);
                    break;
                }
            }
        }
    }
    private  void setTag(){
        for(Operator op: confOperators){
            Vector<String> lhs = op.getIfList();
            ArrayList<Integer> tag = new ArrayList<>();
            for(String rule: lhs){
                boolean flag = true;
                for(int i = 0;i < theCurrentState.size();i++) {
                    if ((new Unifier()).unify(rule, theCurrentState.get(i).getmState(),mBindings)) {
                        tag.add(-theCurrentState.get(i).getmTimetag());
                        flag = false;
                        break;
                    }
                }
                if(flag){
                    tag.add(1);
                }
            }
            Relation.put(op,tag);
        }
    }
private boolean CompareTimetag(ArrayList<Integer> i1,ArrayList<Integer> i2){
    Collections.sort(i1);
    Collections.sort(i2);

    int size = i1.size();
    int k = 0;
    if(i1.size() < i2.size()){
        k = 2;
    }else if(i1.size() > i2.size()){
        size = i2.size();
        k = 1;
    }
    for(int i = 0;i < size;i++){
        if(i1.get(i)==i2.get(i)){

        }else if(i1.get(i)<i2.get(i)){
            k = 1;
            break;
        }else{
            k = 2;
            break;
        }
    }
    System.out.println(i1);
    System.out.println(i2);

    switch (k){
        case 0: System.out.println("Random Choice");
        if(new Random().nextBoolean()){
            return true;
        }else{
            return  false;
        }
        case 1: return true;
        case 2: return false;
    }
    return true;
}

    public Vector<Operator> Lex(){
        if(Relation.size()>1){
            for(int i =0;i<confOperators.size()-1;i++){
                for(int j = i+1;j < confOperators.size();j++){
                    if(!CompareTimetag(Relation.get(confOperators.get(i)),Relation.get(confOperators.get(j)))){
                        Operator op = confOperators.get(i);
                        confOperators.set(i,confOperators.get(j));
                        confOperators.set(j,op);
                    }
                }
            }
            ChangeOperator();
            return RenamedOperators(confOperators);
        }else if(Relation.size()==1){
            return RenamedOperators(confOperators);
        }else{
            return new Vector<>();
        }
    }
    private Vector<Operator> RenamedOperators(Vector<Operator> op){
        Vector<Operator> newop = new Vector<>();
        for(Operator ope: op){
            newop.addElement(rename.get(ope));
        }
        return newop;
    }
    private void setConflictHistory(){
        Vector<Operator> ope = new Vector<>();
        ope.addElement(mOperators.get(0));
        ope.addElement(mOperators.get(3));
        ConflictHistory.addAll(ope);
    }
    private void ChangeOperator(){
        if(!ConflictHistory.isEmpty()){
            Vector<Operator> Op1 = ConflictHistory;
            Vector<Operator> Op2 = RenamedOperators(confOperators);
            boolean flag = true;
            if(Op1.size()==Op2.size()){
                for(int i = 0;i<Op1.size();i++){
                    if((!Op2.get(i).equals(Op1.get(i)))){
                        flag = false;
                    }
                }
                if(flag){
                    Operator temp = confOperators.get(0);
                    confOperators.removeElementAt(0);
                    confOperators.add(temp);

                }
            }

        }
    }
}
