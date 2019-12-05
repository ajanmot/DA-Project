package Messages;
public class GetRespMessage {
    private int message_request_seq = 0;
    private int message_resp_value = 0;
    private int message_resp_seq = 0;

    public GetRespMessage(int request_seq, int resp_value, int resp_seq) {
        this.message_request_seq = request_seq;
        this.message_resp_value = resp_value;
        this.message_resp_seq = resp_seq;
    }

    public int getMessage_request_seq() {
        return message_request_seq;
    }

    public int getMessage_resp_value() {
        return message_resp_value;
    }

    public int getMessage_resp_seq() {
        return message_resp_seq;
    }
}