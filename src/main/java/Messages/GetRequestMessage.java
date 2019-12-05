package Messages;

public class GetRequestMessage {
    private int message_seq = 0;
    public GetRequestMessage(int input_seq) {
        this.message_seq = input_seq;
    }

    public int getMessage_seq() {
        return message_seq;
    }
}
