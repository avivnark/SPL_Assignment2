package bgu.spl.mics.application.objects;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int cores;
    private int currentTick;
    private int numOfProcessed;
    private BlockingQueue<DataBatch> unprocessedDataBatches;
    private Cluster cluster;
    private int totalTime;


    public CPU(int cores){
        this.cores = cores;
        this.cluster = Cluster.getInstance();
        this.currentTick = 1;
        this.numOfProcessed = 0;
        this.unprocessedDataBatches = new LinkedBlockingQueue<>();
    }

    public int getTicks() {
        return currentTick;
    }

    public Collection<DataBatch> getDataBatchCollection() {
        return unprocessedDataBatches;
    }

    public int getNumOfBatches(){
        return unprocessedDataBatches.size();
    }

    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @pre getTicks() + 1 == @post getTicks()
     *
     */
    public void advanceTick(){
        System.out.println("advancing tick");
        currentTick++;
        while (!unprocessedDataBatches.isEmpty()){
            DataBatch batch = unprocessedDataBatches.poll();
            if (batch.getDataType() == Data.Type.Tabular){
                processTabular(batch);
            }
            else if (batch.getDataType() == Data.Type.Text){
                processText(batch);
            }
            else {
                processImage(batch);
            }
        }
    }
    private void processTabular(DataBatch batch){
        process(batch, 32 / cores);
    }
    private void processText(DataBatch batch){
        process(batch, 32 / cores * 2);
    }
    private void processImage(DataBatch batch){
        process(batch, 32 / cores * 4);
    }

    private void process(DataBatch batch, int processTimeRequired){

        if (currentTick-batch.getStartingProcessTick() == processTimeRequired){
            System.out.println(Thread.currentThread().getName() + " processing a batch");
            System.out.println("currently processed " + numOfProcessed);
            cluster.sendProcessedBatch(batch);
            numOfProcessed++;
        }
        else {
            unprocessedDataBatches.add(batch);
        }
    }

    /**
     * @return number of cores of this CPU
     */
    public int getNumOfCores(){
        return cores;
    }

    /**
     * @pre this.getNumOfBatches.size() + 1 = @post this.getNumOfBatches()
     * @param batch - the batch of data to be added to the DataBatch collection later to be processed
     */
    public void addDataBatch(DataBatch batch){
        batch.setStartingProcessTick(currentTick);
        unprocessedDataBatches.add(batch);
    }
    public int getNumOfProcessed(){
        return numOfProcessed;
    }
    public int getTotalTime(){ return totalTime;}

}
