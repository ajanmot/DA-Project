import Messages.CrashMessage;
import Messages.MainToProcessStartMessage;
import Messages.ProcessListMessage;
import Messages.StringMessage;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Main {

	private static final int NB_ACTORS = 3;
	private static final int M = 100;
	private static final boolean IS_RANDOM_HISTORY = false;

	public static Date ExecutionStartDate;
	public static SimpleDateFormat seconds = new SimpleDateFormat("s.SSS");


	public static void main(String[] args) {
		// Create a system where all the actors are going to act
		// Creating the environment
		final ActorSystem system = ActorSystem.create("system");
		// Create an array list of actors
		ArrayList<ActorRef> process = new ArrayList<ActorRef>();
		Main.ExecutionStartDate = new Date();
		for (int i = 0; i < NB_ACTORS; i++) {
			process.add(system.actorOf(Process.createActor(i, M, IS_RANDOM_HISTORY), "a" + i));
		}

		// Test purpose. Send a text message to all the actors.
		for (int i = 0; i < NB_ACTORS; i++) {
			StringMessage m = new StringMessage("Salut acteur : " + i);
			process.get(i).tell(m, ActorRef.noSender());
		}

		// Give the actor list to all actors.
		ProcessListMessage m = new ProcessListMessage(process);
		for (int i = 0; i < NB_ACTORS; i++) {
			process.get(i).tell(m, ActorRef.noSender());
		}
		// Making actors faulty

		Collections.shuffle(process);
		CrashMessage cm = new CrashMessage();
		int nb_silent;
		if (NB_ACTORS == 3)
			nb_silent = 1;
		else
			nb_silent = (NB_ACTORS / 2) - 1;
		for (int i = 0; i < nb_silent; i++) {
			process.get(i).tell(cm, ActorRef.noSender());
		}

		MainToProcessStartMessage lm = new MainToProcessStartMessage();
		for (int i = 0; i < NB_ACTORS; i++) {
			process.get(i).tell(lm, ActorRef.noSender());
		}

	    try {
			waitBeforeTerminate();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			system.terminate();
		}
	}

	public static void waitBeforeTerminate() throws InterruptedException {
		Thread.sleep(5000);
	}
}
