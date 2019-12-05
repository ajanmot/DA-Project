package Messages;

import akka.actor.ActorRef;
import java.util.ArrayList;

public class ProcessListMessage {

    private ArrayList<ActorRef> actorRefs;

    public ProcessListMessage(ArrayList<ActorRef> data) {
        this.actorRefs = data;
    }

    public ArrayList<ActorRef> getActorRefs() {
        return actorRefs;
    }
}