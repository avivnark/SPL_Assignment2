package bgu.spl.mics.application.InputParsing;

import bgu.spl.mics.application.objects.Student;
import bgu.spl.mics.application.objects.ConferenceInformation;

public class InputFile {
    private Student[] Students;
    private String[] GPUS;
    private int[] CPUS;
    private ConferenceInformation[] Conferences;
    private int TickTime;
    private int Duration;

    public Student[] getStudents() {
        return Students;
    }

    public int getNumOfStudents() {
        return Students.length;
    }

    public String[] getGPUS() {
        return GPUS;
    }

    public int getNumOfGPUS() {
        return GPUS.length;
    }

    public int[] getCPUS() {
        return CPUS;
    }

    public int getNumOfCPUS() {
        return CPUS.length;
    }

    public ConferenceInformation[] getConferences() {
        return Conferences;
    }

    public int getNumOfConferences() {
        return Conferences.length;
    }

    public int getTickTime() {
        return TickTime;
    }

    public int getDuration() {
        return Duration;
    }
}
