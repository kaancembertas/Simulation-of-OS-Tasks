import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class Memory {
    private Process memory[];
    private int memorySize;
    private LinkedList<Integer> freeAreas = new LinkedList<Integer>();
    private int lastAddress; //For nextFit algorithm

    public Memory(int memorySize, Process operatingSystem) {
        memory = new Process[memorySize];
        this.memorySize = memorySize;
        this.initMemory(operatingSystem);
    }

    private void initMemory(Process operatingSystem) {
        for (int i = 0; i < this.memorySize; i++) {
            memory[i] = null;
        }
        this.placeProcess(0, operatingSystem); //Place the OS at the beginning of the memory
        freeAreas.add(operatingSystem.getSize());
        lastAddress = operatingSystem.getSize();
    }

    private void placeProcess(int startAddress, Process process) {
        process.setStartAddress(startAddress);
        for (int i = startAddress; i < startAddress + process.getSize(); i++) {
            memory[i] = process;
        }

        if (process.getProcessId() == 0) return; //If OS, return

        int index = freeAreas.indexOf(startAddress);
        if (memory[startAddress + process.getSize()] == null) {
            freeAreas.set(index, startAddress + process.getSize());
        } else {
            freeAreas.remove(index);
        }
    }

    public void removeProcess(Process process) {
        for (int i = process.getStartAddress(); i < process.getStartAddress() + process.getSize(); i++) {
            memory[i] = null;
        }
        freeAreas.add(process.getStartAddress());
        correctFreeAreas();

        process.setStartAddress(-1);
    }

    private void correctFreeAreas() {
        int counter = 0;
        freeAreas.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 < o2 ? -1 : 1;
            }
        });
        while (true) {
            if (counter == freeAreas.size() - 1) return;

            if (isRangeAvailable(freeAreas.get(counter), freeAreas.get(counter + 1))) {
                freeAreas.remove(counter + 1);
                counter = -1;
            }
            counter++;
        }
    }

    private boolean isRangeAvailable(int startAddress, int endAddress) {
        if (endAddress >= this.memorySize) return false;

        for (int i = startAddress; i < endAddress; i++) {
            if (memory[i] != null) return false;
        }
        return true;
    }

    public boolean implementFirstFit(Process process) {
        Iterator i = this.freeAreas.iterator();
        while (i.hasNext()) {
            int address = (Integer) i.next();
            if (isRangeAvailable(address, address + process.getSize())) {
                placeProcess(address, process);
                return true;
            }
        }
        return false;
    }

    public boolean implementNextFit(Process process) {
        for (int i = lastAddress; i < this.memorySize; i++) {
            if (memory[i] == null && isRangeAvailable(i, i + process.getSize())) {
                placeProcess(i, process);
                lastAddress = i;
                return true;
            }
        }
        for (int i = 0; i < lastAddress; i++) {
            if (memory[i] == null && isRangeAvailable(i, i + process.getSize())) {
                placeProcess(i, process);
                lastAddress = i;
                return true;
            }
        }
        return false;
    }

    public int getBlockSize(int startAddress) {
        for (int i = startAddress; i < this.memorySize; i++) {
            if (this.memory[i] != null) return i;
        }
        return this.memorySize - startAddress;
    }

    public boolean implementBestFit(Process process) {
        Iterator i = this.freeAreas.iterator();
        int remain = 0;
        int counter = 0;
        int startAddress = -1;

        while (i.hasNext()) {
            int address = (Integer) i.next();
            if (isRangeAvailable(address, address + process.getSize())) {
                int currentRemain = this.getBlockSize(address) - process.getSize();

                if (counter == 0) {
                    remain = currentRemain;
                    startAddress = address;
                    continue;
                }
                if (currentRemain < remain) {
                    remain = currentRemain;
                    startAddress = address;
                }
            }
        }

        if (startAddress != -1) {
            placeProcess(startAddress, process);
            return true;
        }
        return false;
    }

    public int getSize() {
        return memorySize;
    }

    public LinkedList getFreeAreas() {
        return freeAreas;
    }

    @Override
    public String toString() {
        String output = "";
        for (int i = 0; i < memory.length; i++) {
            Process p = memory[i];
            if (p != null) {
                String processString = p.getProcessId() == 0 ? "Operating System" : "P" + p.getProcessId();
                output +=
                        "[P" + p.getProcessId() + "] Process Id:" + processString + "\n" +
                                "[P" + p.getProcessId() + "] Begin Address: " + p.getStartAddress() + "\n" +
                                "[P" + p.getProcessId() + "] End Address: " + (i + p.getSize() - 1) + "\n\n";
                i += p.getSize() - 1;
            }
        }
        return output;
    }
}