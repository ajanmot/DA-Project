package Messages;

public class PutRespMessage {
    private int message_seq;
    private int message_value;

    public PutRespMessage(int input_seq, int input_value) {
        this.message_seq = input_seq;
        this.message_value = input_value;
    }

    public int getMessage_seq() {
        return message_seq;
    }
    public int getMessage_value() {
        return message_value;
    }
}