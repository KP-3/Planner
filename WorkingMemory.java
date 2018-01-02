public class WorkingMemory {
    private String mState;
    private int mTimetag;

    WorkingMemory(){
        mState = new String();
        mTimetag = 0;
    }

    WorkingMemory(String state,int timetag){
        mState = state;
        mTimetag = timetag;
    }

    public int getmTimetag() {
        return mTimetag;
    }

    public String getmState() {
        return mState;
    }

    public void setmTimetag(int mTimetag) {
        this.mTimetag = mTimetag;
    }

    public void setmState(String mState) {
        this.mState = mState;
    }
    public WorkingMemory clone(){
        return new WorkingMemory(mState,mTimetag);
    }
}
