import java.util.ArrayList;

public class Cpu {
    private ArrayList<Process> cpu = new ArrayList<>();
    private ArrayList<Process> io = new ArrayList<>();
    private Process currentProcess;
    private Process currentIo;
    private int timeQuantum;

    private int numOfFinishedProcesses = 0;

    //Queue that contains processes which are waiting for execution
    private ArrayList<Process> readyQueue = new ArrayList<>();
    private ArrayList<Process> ioReadyQueue = new ArrayList<>();
    private int clock = 0;
    private Memory memory;

    public Cpu(Memory memory, int timeQuantum) {
        this.currentProcess = null;
        this.currentIo = null;
        this.timeQuantum = timeQuantum;
        this.memory = memory;
    }

    public void implementFirstInFirstOut() {
        //Get process from ready queue
        if (currentProcess == null) {
            if (!readyQueue.isEmpty()) {
                Process candidateProcess = readyQueue.get(0);
                if (!candidateProcess.ioDelay) {
                    currentProcess = readyQueue.get(0);
                    readyQueue.remove(0);
                }
            }
        }
        executeProcess();
        checkIoBurst();
    }

    public void implementShortestJobFirst() {
        //Get process from ready queue
        if (currentProcess == null) {
            if (!readyQueue.isEmpty()) {
                Process candidateProcess = null;
                for (int i = 0; i < readyQueue.size(); i++) {
                    if (candidateProcess == null && !readyQueue.get(i).ioDelay) {
                        candidateProcess = readyQueue.get(i);
                        continue;
                    }
                    if (!readyQueue.get(i).ioDelay &&
                            readyQueue.get(i).getRemainExecutionTime() < candidateProcess.getRemainExecutionTime()) {
                        candidateProcess = readyQueue.get(i);
                    }
                }
                if (candidateProcess != null) {
                    currentProcess = candidateProcess;
                    readyQueue.remove(candidateProcess);
                }
            }
        }
        executeProcess();
        checkIoBurst();
    }

    public void implementRoundRobin() {
        //Checking the time quantum for preemption
        if (currentProcess != null &&
                currentProcess.getProcessCounter() == timeQuantum) {
            readyQueue.add(currentProcess);
            currentProcess.resetProcessCounter();
            currentProcess = null;
        }

        if (currentProcess == null) {
            if (!readyQueue.isEmpty()) {
                Process candidateProcess = readyQueue.get(0);
                if (!candidateProcess.ioDelay) {
                    currentProcess = readyQueue.get(0);
                    readyQueue.remove(0);
                }
            }
        }

        executeProcess();
        checkIoBurst();
    }

    private void executeProcess() {
        if (currentProcess != null) {
            int remainTime = currentProcess.decreaseRemainTime();
            currentProcess.increaseProcessCounter();
            cpu.add(currentProcess);
            if (remainTime == 0) {
                memory.removeProcess(currentProcess);
                numOfFinishedProcesses++;
                currentProcess = null;
            }
        } else {
            cpu.add(null);
        }

        for (Process p : readyQueue) p.ioDelay = false;  //CLEAR ALL IO DELAYS
    }

    private void checkIoBurst() {
        //Check the is the time for IO burst for current process
        if (currentProcess != null && !currentProcess.getIoRequests().isEmpty()) {
            int ioTime = (int) currentProcess.getIoRequests().get(0);
            if (currentProcess.getExecutionTime() - currentProcess.getRemainExecutionTime() == ioTime - 1) {
                currentProcess.getIoRequests().remove(0);
                currentProcess.setRemainIoTime(currentProcess.getIoBurstTime());
                currentProcess.resetProcessCounter();
                this.ioReadyQueue.add(currentProcess);
                currentProcess = null;
            }
        }
    }

    public void implementIoBurst() {
        if (this.currentIo == null && !this.ioReadyQueue.isEmpty()) {
            this.currentIo = this.ioReadyQueue.get(0);
            this.ioReadyQueue.remove(0);
        }

        if (this.currentIo != null) this.currentIo.decreaseIoTime();
        io.add(currentIo);

        if (currentIo != null && currentIo.getRemainIoTime() == 0) {
            if (currentIo.getRemainExecutionTime() != 0) {
                if (Main.schedulingAlgorithm == Scheduling.FIRST_IN_FIRST_OUT && readyQueue.isEmpty())
                    currentIo.ioDelay = true;
                else if (Main.schedulingAlgorithm == Scheduling.SHORTEST_JOB_FIRST) {
                    currentIo.ioDelay = true;
                }
                else if(Main.schedulingAlgorithm == Scheduling.ROUND_ROBIN && readyQueue.isEmpty()){
                    currentIo.ioDelay = true;
                }
                this.readyQueue.add(currentIo);
            } else {
                memory.removeProcess(currentIo);
                numOfFinishedProcesses++;
            }
            this.currentIo = null;
        }
    }

    public void increaseClock() {
        clock++;
    }

    public int getClock() {
        return clock;
    }

    public ArrayList<Process> getReadyQueue() {
        return this.readyQueue;
    }

    public ArrayList<Process> getIoReadyQueue() {
        return this.ioReadyQueue;
    }

    public int getNumOfFinishedProcesses() {
        return this.numOfFinishedProcesses;
    }

    public Process getCurrentProcess() {
        return this.cpu.get(clock);
    }

    public Process getCurrentIo() {
        return this.io.get(clock);
    }

    @Override
    public String toString() {
        String str = "[MS] [CPU] [IO]\n";
        for (int i = 0; i < this.cpu.size(); i++) {
            Process cpuProcess = this.cpu.get(i);
            Process ioProcess = this.io.get(i);
            String cpuId = cpuProcess == null ? "N" : "P" + cpuProcess.getProcessId() + "";
            String ioId = ioProcess == null ? "N" : "P" + ioProcess.getProcessId() + "";
            str += i + " " + cpuId + " " + ioId + "\n";
        }

        return str;
    }
}
