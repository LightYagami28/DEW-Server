package me.pari.connection;

public class Response extends Packet {

    private final int status;
    private final String desc;

    /*
    * Server response to the client request.
    *
    * */
    public Response(int id, int status, String desc) {
        super(id);
        this.status = status;
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
