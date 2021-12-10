package bgu.spl.net.srv;

public class Ack implements Message {
    private short opcode;

    public Ack(short opcode) {
        this.opcode = opcode;
    }

    public short getOpcode(){
        return opcode;
    }

}
