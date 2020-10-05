import java.util.ArrayList;

public class Process {
    private int processId;
    private int size; //KB
    private int incomingTime;
    private int executionTime;
    private int ioBurstTime;
    private int remainExecutionTime;
    private int remainIoTime;
    private int processCounter;
    private ArrayList ioRequests;
    private int startAddress = -1;
    public boolean ioDelay = false; //This variable provides to delay process when it returned to ready queue.


    public Process(String info) {
        String infoArray[] = info.split(" ");
        this.processId = Integer.parseInt(infoArray[0]);
        this.size = Integer.parseInt(infoArray[1]);
        this.incomingTime = Integer.parseInt(infoArray[2]);
        this.executionTime = Integer.parseInt(infoArray[3]);
        this.remainExecutionTime = this.executionTime;
        this.setIoRequests(infoArray[4].split(","));
        this.ioBurstTime = Integer.parseInt(infoArray[5]);
        this.remainIoTime = 0;
        this.processCounter = 0;
    }

    public Process(int processId, int size) {
        this.processId = processId;
        this.size = size;
    }

    public int getProcessId() {
        return processId;
    }

    public int getSize() {
        return size;
    }

    public int getIncomingTime() {
        return incomingTime;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public ArrayList getIoRequests() {
        return this.ioRequests;
    }

    public void setIoRequests(String[] ioRequests) {
        this.ioRequests = new ArrayList();

        for (int i = 0; i < ioRequests.length; i++) {
            this.ioRequests.add(Integer.parseInt(ioRequests[i]));
        }
    }

    public void decreaseIoTime() {
        this.remainIoTime--;
    }

    public int getRemainIoTime() {
        return this.remainIoTime;
    }

    public int getStartAddress() {
        return this.startAddress;
    }

    public int decreaseRemainTime() {
        return --this.remainExecutionTime;
    }

    public int getRemainExecutionTime() {
        return this.remainExecutionTime;
    }

    public int getIoBurstTime() {
        return this.ioBurstTime;
    }

    public  void setRemainIoTime (int time){
        this.remainIoTime = time;
    }

    public void setStartAddress(int address){
        this.startAddress = address;
    }

    public void increaseProcessCounter(){
        this.processCounter++;
    }
    public void resetProcessCounter(){
        this.processCounter=0;
    }
    public int getProcessCounter(){
        return this.processCounter;
    }

    @Override
    public String toString() {
        return
                "[P"+processId+"] Process Id: " + this.processId + "\n" +
                "[P"+processId+"] Size: " + this.size + "\n" +
                "[P"+processId+"] Start Address:" + this.startAddress + "\n" +
                "[P"+processId+"] Incoming Time: " + this.incomingTime + " ms\n" +
                "[P"+processId+"] Remain Execution Time: " + this.remainExecutionTime + " ms\n" +
                "[P"+processId+"] IO Requests: " + this.getIoRequests()+"\n"+
                "[P"+processId+"] IO Burst Time: "+this.ioBurstTime+"\n"+
                "[P"+processId+"] IO Remain Time: "+this.remainIoTime;
    }
}
