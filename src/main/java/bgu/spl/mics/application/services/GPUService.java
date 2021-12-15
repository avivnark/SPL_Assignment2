package bgu.spl.mics.application.services;

import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.messages.DataPreProcessEvent;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * in addition to sending the {@link DataPreProcessEvent}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;
    private MessageBusImpl messageBus;
    /*
    flow:
    GPUService calls to messageBus.complete(Event<T> e, T result)
    inside meesageBus.complete we need to invoke studentService.complete(Event<T> e, T result)
    inside studentService.complete we need to invoke future.resolve(T result)
    inside future.resolve we need to change the result that future holds.
     */

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
        this.messageBus = MessageBusImpl.getInstance();
    }

    private void createAndSendBatches(Data data) {
        Cluster cluster = Cluster.getInstance();
        for (int i = 0; i < data.getSize(); i += 1000) {
            DataBatch batch = new DataBatch(data, i);
            cluster.sendUnprocessedBatch(batch);
        }

    }

    @Override
    protected void initialize() {
        messageBus.register(this);
        subscribeBroadcast(TickBroadcast.class, c -> {
            gpu.advanceTick();
        });
        subscribeEvent(TrainModelEvent.class, c -> {
            Model model = c.getModel();
            createAndSendBatches(model.getData());

            /*
            create batches
            send to cpu through cluster
            receive from cpu through cluster
            use processed batches to train the model
            set it trained
             */

        });
        subscribeEvent(TestModelEvent.class, c -> {
            Model model = c.getModelToTest();
            if (model.getStudent().isMsc()) {
                if (Math.random() < 0.6) {
                    model.setGoodResult();
                } else {
                    model.setBadResult();
                }
            } else {
                if (Math.random() < 0.8) {
                    model.setGoodResult();
                } else {
                    model.setBadResult();
                }
            }
            messageBus.complete(c, model);
        });
    }

    private void getMessages() {
        try {
            Message message = messageBus.awaitMessage(this);
        } catch (InterruptedException ignored) {
        }

    }
}
