import java.io.*;
import java.util.*;

public class Main {
    private static final String inputPath = "./src/processes.txt";

    private static final String cpuOutputPath = "./src/output/cpuOutput.txt";
    private static final String detailedCpuOutputPath = "./src/output/detailedCpuOutput.txt";
    private static final String memoryOutputPath = "./src/output/memoryOutput.txt";

    private static final int MEMORY_SIZE = 1024; //KB
    private static final int OS_SIZE = 350; //KB
    private static final int TIME_QUANTUM = 5;
    private static Memory memory;
    private static Cpu cpu;

    //List that contains processes which are came from the input
    private static ArrayList<Process> processList = new ArrayList<>();

    //Queue that contains processes to allocate memory
    private static ArrayList<Process> processQueue = new ArrayList<>();

    public static Scheduling schedulingAlgorithm;

    private static void init() {
        Process operatingSystem = new Process(0, OS_SIZE); //pID=0 defines the OS
        memory = new Memory(MEMORY_SIZE, operatingSystem);
        cpu = new Cpu(memory, TIME_QUANTUM);
    }

    private static void readInput() {
        File inputFile = new File(inputPath);
        try {
            Scanner scanner = new Scanner(inputFile);
            while (scanner.hasNextLine()) {
                processList.add(new Process(scanner.nextLine()));
            }
        } catch (FileNotFoundException ex) {
            System.out.println("File Not Found");
        }
    }

    private static Placement readMemoryPlacement() {
        String inputString = null;
        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.println("---------------------");
            System.out.print("(1) - BEST FIT\n(2) - FIRST_FIT\n(3) - NEXT FIT\n");

            if (inputString == null)
                System.out.print("Please enter the Memory Placement Algorithm: ");
            else
                System.out.print("Incorrect type, enter the algorithm again: ");

            inputString = input.nextLine();
            if (inputString.equals("1")) return Placement.BEST_FIT;
            if (inputString.equals("2")) return Placement.FIRST_FIT;
            if (inputString.equals("3")) return Placement.NEXT_FIT;
        }
    }

    private static Scheduling readScheduling() {
        String inputString = null;
        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.println("---------------------");
            System.out.print("(1) - FIRST IN FIRST OUT\n(2) - SHORTEST JOB FIRST\n(3) - ROUND ROBIN\n");

            if (inputString == null)
                System.out.print("Please enter the CPU Scheduling Algorithm: ");
            else
                System.out.print("Incorrect type, enter the algorithm again: ");

            inputString = input.nextLine();
            if (inputString.equals("1")) return Scheduling.FIRST_IN_FIRST_OUT;
            if (inputString.equals("2")) return Scheduling.SHORTEST_JOB_FIRST;
            if (inputString.equals("3")) return Scheduling.ROUND_ROBIN;
        }
    }

    private static Process checkIsThereProcess() {
        for (Process process : processList) {
            if (process.getIncomingTime() == cpu.getClock()) return process;
        }
        return null;
    }

    private static void startSimulationLoop(Placement placementAlgorithm, Scheduling schedulingAlgorithm) throws IOException {
        File detailedCpuOutput = new File(detailedCpuOutputPath);
        detailedCpuOutput.createNewFile();
        File memoryOutput = new File(memoryOutputPath);
        memoryOutput.createNewFile();
        StringBuilder outputText = new StringBuilder();
        StringBuilder memoryOutputText = new StringBuilder();

        while (true) {
            Process p = checkIsThereProcess();
            if (p != null) processQueue.add(p);

            if (!processQueue.isEmpty()) {
                Process process = processQueue.get(0);
                boolean isPlaced = false;

                switch (placementAlgorithm) {
                    case FIRST_FIT:
                        isPlaced = memory.implementFirstFit(process);
                        break;
                    case BEST_FIT:
                        isPlaced = memory.implementBestFit(process);
                        break;
                    case NEXT_FIT:
                        isPlaced = memory.implementNextFit(process);
                        break;
                }

                if (isPlaced) {
                    processQueue.remove(0);
                    cpu.getReadyQueue().add(process);
                }
            }

            cpu.implementIoBurst();
            switch (schedulingAlgorithm) {
                case FIRST_IN_FIRST_OUT:
                    cpu.implementFirstInFirstOut();
                    break;
                case SHORTEST_JOB_FIRST:
                    cpu.implementShortestJobFirst();
                    break;
                case ROUND_ROBIN:
                    cpu.implementRoundRobin();
                    break;
            }
            writeInfo(outputText);
            memoryOutputText.append("----- CLOCK: "+cpu.getClock()+" -----\n\n"+memory.toString());
            cpu.increaseClock();

            //Finish condition
            if (cpu.getNumOfFinishedProcesses() == processList.size())
                break;
        }

        FileWriter detailedCpuwriter = new FileWriter(detailedCpuOutput, false);
        BufferedWriter bdetailedCpuWriter = new BufferedWriter(detailedCpuwriter);
        bdetailedCpuWriter.write(outputText.toString());
        bdetailedCpuWriter.close();

        FileWriter memoryWriter = new FileWriter(memoryOutput, false);
        BufferedWriter bMemoryWriter = new BufferedWriter(memoryWriter);
        bMemoryWriter.write(memoryOutputText.toString());
        bMemoryWriter.close();
    }

    private static void writeInfo(StringBuilder outputText) {
        String readyQueueString = "";
        String ioReadyQueueString = "";

        for (Process process : cpu.getReadyQueue()) {
            readyQueueString += "P" + process.getProcessId() + ",";
        }
        for (Process process : cpu.getIoReadyQueue()) {
            ioReadyQueueString += "P" + process.getProcessId() + ",";
        }
        outputText.append("---------------------\n");
        outputText.append("Clock: " + cpu.getClock() + "\n");
        outputText.append("Free Areas: " + memory.getFreeAreas() + "\n");
        outputText.append("Ready Queue: " + readyQueueString + "\n");
        outputText.append("IO Ready Queue: " + ioReadyQueueString + "\n");
        Process currentProcess = cpu.getCurrentProcess();
        String currentPid = currentProcess == null ? "NULL" : "P" + currentProcess.getProcessId();

        Process currentIo = cpu.getCurrentIo();
        String currentIoPid = currentIo == null ? "NULL" : "P" + currentIo.getProcessId();

        outputText.append("Current Process: " + currentPid + "\n");
        outputText.append("Current IO: " + currentIoPid + "\n\n");
        //outputText.append("------PROCESSES IN THE MEMORY------\n");
        for (Process process : processList) {
            if (process.getStartAddress() == -1) continue;
            outputText.append(process.toString() + "\n");
            outputText.append("\n");
        }
    }

    private static void writeOutput(String outputPath,String output) throws IOException {
        File file = new File(outputPath);
        FileWriter writer = new FileWriter(file,false);
        file.createNewFile();
        BufferedWriter bWriter = new BufferedWriter(writer);
        bWriter.write(output);
        bWriter.close();
    }

    public static void main(String[] args) throws IOException {
        readInput();
        Placement placementAlgorithm = readMemoryPlacement();
        schedulingAlgorithm = readScheduling();
        init();
        startSimulationLoop(placementAlgorithm, schedulingAlgorithm);
        writeOutput(cpuOutputPath,cpu.toString());

        System.out.println("\nInfo: The simulation worked well. The outputs are in the output folder of the project");

    }
}
