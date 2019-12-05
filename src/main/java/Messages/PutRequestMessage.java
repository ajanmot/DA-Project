package Messages;

public class PutRequestMessage {
    private int message_value = 0;
    private int message_seq = 0;

    public PutRequestMessage(int input_seq, int input_value) {
        this.message_value = input_value;
        this.message_seq = input_seq;
    }

    public int getMessage_value() {
        return message_value;
    }

    public int getMessage_seq() {
        return message_seq;
    }
}