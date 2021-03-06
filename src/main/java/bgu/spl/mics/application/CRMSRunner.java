package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.InputParsing.InputFile;
import bgu.spl.mics.application.InputParsing.ModelDeserializer;
import bgu.spl.mics.application.OutputWriting.OutputFile;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */

public class CRMSRunner {

    private static InputFile parseInputFile(String jsonPath) throws FileNotFoundException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Model.class, new ModelDeserializer()).create();
        JsonReader reader = new JsonReader(new FileReader(jsonPath));
        return gson.fromJson(reader, InputFile.class);
    }

    public static void buildOutputFile(Object[] statistics) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        InputFile arrays = (InputFile) statistics[0];
        int gpuTimeUsed = (int) statistics[3];
        int cpuTimeUsed = (int) statistics[4];
        int batchesProcessed = (int) statistics[5];
        String filePath = (String) statistics[6];
        Student[] students = arrays.getStudents();
        ConferenceInformation[] conferences = arrays.getConferences();
        try {
            Writer writer = new FileWriter(filePath);
            gson.toJson(new OutputFile(students, conferences, gpuTimeUsed, cpuTimeUsed, batchesProcessed), writer);
            writer.flush(); //flush data to file
            writer.close(); //close write
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void buildStudentServices(InputFile inputJava) {
        Student[] students = inputJava.getStudents();
        int numOfStudents = students.length;
        Thread[] studentServicesThreads = new Thread[numOfStudents];
        for (int i = 0; i < numOfStudents; i++) {
            Arrays.sort(students[i].getModels(), Comparator.comparingInt(m -> m.getData().getSize()));
            studentServicesThreads[i] = new Thread(new StudentService("Student Service " + i, students[i]));
            studentServicesThreads[i].start();
        }
    }

    private static void buildGPUServices(GPU[] gpus) {
        Thread[] GPUServicesThreads = new Thread[gpus.length];
        int i = 0;
        for (GPU gpu : gpus) {
            GPUServicesThreads[i] = new Thread(new GPUService("GPU Service " + i, gpu));
            GPUServicesThreads[i++].start();
        }
    }

    private static void buildCPUServices(CPU[] cpus) {
        Thread[] CPUServicesThreads = new Thread[cpus.length];
        int i = 0;
        for (CPU cpu : cpus) {
            CPUServicesThreads[i] = new Thread(new CPUService("CPU Service " + i, cpu));
            CPUServicesThreads[i++].start();
        }
    }

    private static void buildConferenceServices(InputFile inputJava) {
        int numOfConferences = inputJava.getNumOfConferences();
        Thread[] conferencesServicesThreads = new Thread[numOfConferences];
        int i = 0;
        for (ConferenceInformation conference : inputJava.getConferences()) {
            conferencesServicesThreads[i] = new Thread(new ConferenceService("Conference Service " + i, conference));
            conferencesServicesThreads[i++].start();
        }
    }

    private static void buildTimeService(InputFile inputJava) {
        int tickTime = inputJava.getTickTime();
        int duration = inputJava.getDuration();
        Thread timeServiceThread = new Thread(new TimeService(tickTime, duration));
        timeServiceThread.start();
    }

    private static GPU[] parseAndConstructGPUS(String[] gpuStrings) {
        GPU[] gpus = new GPU[gpuStrings.length];
        for (int i = 0; i < gpuStrings.length; i++) {
            GPU.Type type = convertStringTypeToEnum(gpuStrings[i]);
            gpus[i] = new GPU(type, i);
        }
        return gpus;

    }

    private static GPU.Type convertStringTypeToEnum(String gpuString) {
        if (gpuString.equals("RTX3090")) return GPU.Type.RTX3090;
        else if (gpuString.equals("RTX2080")) return GPU.Type.RTX2080;
        else return GPU.Type.GTX1080;
    }

    private static CPU[] parseAndConstructCPUS(int[] cpuCores) {
        CPU[] cpus = new CPU[cpuCores.length];
        for (int i = 0; i < cpuCores.length; i++) {
            cpus[i] = new CPU(cpuCores[i]);
        }
        return cpus;
    }

    private static void updateStudentFieldInModels(InputFile inputJava) {
        for (Student student : inputJava.getStudents()) {
            for (Model model : student.getModels()) {
                model.setStudent(student);
            }
        }
    }

    public static void main(String[] args) {
        InputFile inputAsJavaObject;
        try {
            inputAsJavaObject = parseInputFile(args[0]);
        } catch (FileNotFoundException e) {
            System.out.println("File does not exist");
            return;
        }
        MessageBusImpl messageBus = MessageBusImpl.getInstance();
        Cluster cluster = Cluster.getInstance();
        updateStudentFieldInModels(inputAsJavaObject);
        GPU[] gpus = parseAndConstructGPUS(inputAsJavaObject.getGPUS());
        CPU[] cpus = parseAndConstructCPUS(inputAsJavaObject.getCPUS());
        buildGPUServices(gpus);
        buildCPUServices(cpus);
        updateCluster(gpus, cpus);
        buildConferenceServices(inputAsJavaObject);
        buildStudentServices(inputAsJavaObject);
        buildTimeService(inputAsJavaObject);
        cluster.setArrays(inputAsJavaObject, args[1]);
    }

    private static void updateCluster(GPU[] gpus, CPU[] cpus) {
        Cluster cluster = Cluster.getInstance();
        cluster.setCPUS(cpus);
        cluster.setGPUS(gpus);
    }
}
