package bgu.spl.mics.application.services;

import bgu.spl.mics.Message;
import bgu.spl.mics.MessageBus;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.objects.DataBatch;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.messages.DataPreProcessEvent;
import bgu.spl.mics.application.objects.Model;

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
    private MessageBusImpl MessageBus;
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
        this.MessageBus = MessageBusImpl.getInstance();
    }

    @Override
    protected void initialize() {
        MessageBus.register(this);
        getMessages();
    }

    private void getMessages(){
        try {
            Message message = MessageBus.awaitMessage(this);
        }
        catch (InterruptedException ignored){}

    }
    protected Model proccess(Message message){
        return model
    }
    }
