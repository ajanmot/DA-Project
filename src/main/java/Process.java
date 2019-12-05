import Messages.*;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Process extends UntypedAbstractActor{

    // Logger attached to actor
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    // Actor reference
    private ArrayList<ActorRef> actorRef;
    private int local_value = 0;
    // Local seq_number
    private int local_seq = 0;
    // Trehsold
    private int f;
    // Number of responses recieved for the current operation
    private int nb_resp_recieved;
    private int stop_get = 0;
    private int stop_put = 0;
    private int max_seq = 0;
    private int max_value = 0;
    private int is_crashed = 0;
    private int p_id;
    private int M;


    // Array storing the execution history
    // 0 for get
    // 1 for put
    // private int[] execution_array = {0, 1, 0, 1, 1, 0};
    // PUT GET PUT GET PUT GET PUT GET PUT GET PUT GET PUT
    private int index_current_operation = 0;
    private ArrayList<Integer> execution_array;

    private int current_index_put = 0;

    public Process() {}

    // Print the history of the process
    private void print_history() {
        String res = "A" + this.p_id + " - History  for process A" + this.p_id + "is : " ;
        for (int i = 0; i < this.execution_array.size(); i++) {
            if (this.execution_array.get(i) == 0) {
                res += " GET";
            }
            else
                res += " PUT";
        }
        log.info(res);
    }

    // Constructor
    public Process(int p_id, int M, boolean IS_RANDOM_HISTORY) {
        this.p_id = p_id;
        this.M = M;
        execution_array = new ArrayList<Integer>();
        for (int i = 0; i < M * 2; i++) {
            this.execution_array.add(i % 2);
        }
        if (IS_RANDOM_HISTORY) {
            Collections.shuffle(this.execution_array);
        }
        this.print_history();
    }


    // Static function creating actor Props
    public static Props createActor(int p_id, int M, boolean IS_RANDOM_HISTORY) {
        return Props.create(Process.class, () -> {
            return new Process(p_id, M, IS_RANDOM_HISTORY);
        });
    }

    // Define put function
    private void put(int v) {
        this.local_seq++;
        this.nb_resp_recieved = 0;
        this.stop_put = 0;
        PutRequestMessage m = new PutRequestMessage(this.local_seq, v);
        for (ActorRef p : this.actorRef) {
            // Envoie du message send(seq, v)
            p.tell(m, getSelf());
        }
    }

    // Define get function
    private void get() {
        this.max_seq = this.local_seq;
        this.max_value = this.local_value;
        this.local_seq++;
        this.nb_resp_recieved = 0;
        this.stop_get = 0;
        GetRequestMessage m = new GetRequestMessage(this.local_seq);
        for (ActorRef p : this.actorRef) {
            p.tell(m, getSelf());
        }
    }

    // Performing the next operation in the history
    private void nextMessage(){
        if (this.index_current_operation == this.M * 2) {
            String diff = Main.seconds.format((new Date()).getTime() - Main.ExecutionStartDate.getTime());
            log.info("A{} - End of process - Execution time: " + diff + " secs.", this.p_id);
        }
        else {
            if (this.execution_array.get(this.index_current_operation) == 0) {
                // We perform a get
                log.info("A{} - Get message requested", this.p_id);
                this.get();
            }
            else {
                // We perform a put
                this.put((this.p_id * this.M) + this.current_index_put);
                log.info("A{} - Put message requested for value {}.", this.p_id, (this.p_id * this.M) + this.current_index_put);
                this.current_index_put++;
            }
            this.index_current_operation++;
        }
    }

    @Override
    public void onReceive(Object message) throws Throwable {
        if (this.is_crashed != 1)  { // If the process does not crashed
            // Reception of a crash message
            if (message instanceof CrashMessage) {
                this.is_crashed = 1;
                log.info("A{} - Process crash", this.p_id);
            }
            // Begining of the execution
            else if (message instanceof MainToProcessStartMessage) {
                this.nextMessage();
            }
            // Reception of a basic text message
            else if(message instanceof StringMessage){
                StringMessage m = (StringMessage) message;
                log.info("["+getSelf().path().name()+"] received message from ["+ getSender().path().name() +"] with data: ["+m.data+"]");
            }
            // Receive declaration of the actor list
            else if (message instanceof ProcessListMessage) {
                this.actorRef = ((ProcessListMessage) message).getActorRefs();
                this.f = ((this.actorRef.size() + 1) / 2);
            }
            /***
             * Put functions
             ***/
            // Receive put request from actor
            else if (message instanceof PutRequestMessage) {
                int recieved_seq = ((PutRequestMessage) message).getMessage_seq();
                int recieved_value = ((PutRequestMessage) message).getMessage_value();

                // Check if we are up to date
                if (recieved_seq >= this.local_seq) {
                    this.local_value = recieved_value;
                    this.local_seq = recieved_seq;

                    // Send the validation to the sender
                    PutRespMessage m = new PutRespMessage(recieved_seq, recieved_value);
                    getSender().tell(m, getSelf());
                }
            }
            // Recieve validation from actors
            else if (message instanceof PutRespMessage) {
                // Count the number of responses
                if (((PutRespMessage) message).getMessage_seq() == this.local_seq)
                    this.nb_resp_recieved++;
                // Majority of the actors responded
                if (this.nb_resp_recieved >= this.f && this.stop_put == 0) {
                    this.local_value = ((PutRespMessage) message).getMessage_value();
                    log.info("  A{} PUT OPERATION COMPLETED ! For value: " + this.local_value, this.p_id);
                    this.stop_put = 1;
                    this.nextMessage();
                }
            }
            /**
             * Get functions
             **/
            // Reception of a get request
            else if (message instanceof GetRequestMessage) {
                int received_seq = ((GetRequestMessage) message).getMessage_seq();

                // Send a resp message to the sender
                GetRespMessage m = new GetRespMessage(received_seq, this.local_value, this.local_seq);
                getSender().tell(m, getSelf());
            }
            // Recieve validation from actors
            else if (message instanceof GetRespMessage) {
                if (((GetRespMessage) message).getMessage_request_seq() == this.local_seq) {
                    this.nb_resp_recieved++; // Count the number of valdations

                    // Get the value from the highest sequence number
                    int received_seq = ((GetRespMessage) message).getMessage_resp_seq();
                    if (this.max_seq < received_seq) {
                        this.max_seq = received_seq;
                        this.max_value = ((GetRespMessage) message).getMessage_resp_value();
                    }
                }
                // If we receive enough validation
                if (this.nb_resp_recieved >= this.f && this.stop_get == 0) {
                    log.info("  A{} GET OPERATION COMPLETED ! For value: " + this.max_value, this.p_id);
                    this.stop_get = 1;
                    this.nextMessage();
                }
            }
        }
        else {
        }

    }
}
