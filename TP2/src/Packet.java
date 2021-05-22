import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet implements Serializable {
    private int tipo; // 1-pedir ficheiro | 2-enviar ficherio | 3-ficheiro nao existe | 4-FFS ligou | 5-Gateway informa que ligou | 6-Keep Alive | 7 - Resposta ao keep alive
    private String ipDestino;
    private int porta;
    private int idTransferencia;
    // idUser
    private int chucnkTransferencia;
    private byte[] data;

    public Packet (int tipo, String ipDestino, int porta, int idTransferencia, int chucnkTransferencia, byte[] data) {
        this.tipo = tipo;
        this.ipDestino = ipDestino;
        this.porta = porta;
        this.idTransferencia = idTransferencia;
        this.chucnkTransferencia = chucnkTransferencia;
        this.data = data;
    }

    public Packet (byte[] arrayBytes) throws UnknownHostException {
        byte[] auxiliar = new byte[4];

        this.tipo = ByteBuffer.wrap(arrayBytes,0,4).getInt();

        System.arraycopy(arrayBytes, 4, auxiliar, 0, 4);
        this.ipDestino = InetAddress.getByAddress(auxiliar).getHostAddress();

        this.porta = ByteBuffer.wrap(arrayBytes,8,4).getInt();
        this.idTransferencia = ByteBuffer.wrap(arrayBytes,12,4).getInt();
        this.chucnkTransferencia = ByteBuffer.wrap(arrayBytes,16,4).getInt();

        byte[] newData = new byte[arrayBytes.length - 20];
        System.arraycopy(arrayBytes, 20, newData, 0, arrayBytes.length-20);
        this.data = newData;
    }


    byte[] packetToBytes () throws UnknownHostException {
        byte[] pacoteEmBytes = new byte[20 + this.data.length];

        byte[] tipo = convertIntToByteArray(this.tipo);
        System.arraycopy(tipo,0,pacoteEmBytes,0,4);
        byte[] ipDestino = InetAddress.getByName(this.ipDestino).getAddress();
        System.arraycopy(ipDestino,0,pacoteEmBytes,4,4);
        byte[] porta = convertIntToByteArray(this.porta);
        System.arraycopy(porta,0,pacoteEmBytes,8,4);
        byte[] idTransferencia = convertIntToByteArray(this.idTransferencia);
        System.arraycopy(idTransferencia,0,pacoteEmBytes,12,4);
        byte[] chucnkTransferencia = convertIntToByteArray(this.chucnkTransferencia);
        System.arraycopy(chucnkTransferencia,0,pacoteEmBytes,16,4);
        System.arraycopy(this.data,0,pacoteEmBytes,20,this.data.length);

        return pacoteEmBytes;
    }


    byte[] convertIntToByteArray (int inteiro) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(inteiro);
        return byteBuffer.array();
    }


    @Override
    public String toString() {
        return "Packet{" +
                "tipo=" + tipo +
                ", ipDestino='" + ipDestino + '\'' +
                ", porta=" + porta +
                ", idTransferencia=" + idTransferencia +
                ", chucnkTransferencia=" + chucnkTransferencia +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public int getTipo() {
        return tipo;
    }

    public String getIpDestino() {
        return ipDestino;
    }

    public int getPorta() {
        return porta;
    }

    public int getIdTransferencia() {
        return idTransferencia;
    }

    public int getChucnkTransferencia() {
        return chucnkTransferencia;
    }

    public String getData() {
        return data.toString();
    }
}