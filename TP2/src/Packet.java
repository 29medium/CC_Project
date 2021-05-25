import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Packet implements Serializable {
    public static final int MAX_SIZE_DATA = 4096;
    public static final int MAX_SIZE_PACKET = MAX_SIZE_DATA + 28;
    public static final String ENCRIPTION_KEY = "cc14";

    // 1 - Pergunta aos FFs se ficheiro existe | 2 - Responde que possui o ficheiro       | 3 - Responde que ficheiro nao existe
    // 4 - Pede ao FFs um ficheiro que possui  | 5 - Envia o ficheiro requisitado
    // 6 - FFs informa que se pretende ligar   | 7 - FFs informa que se pretende desligar | 8 - Gateway informa que FSs se ligou corretamente
    // 9 - Gateway envia um Keep Alive         | 10- Gateway confirma que conexão foi cancelada | 11 - Erro indeterminado
    private int tipo;
    private String ipOrigem;
    private String ipDestino;
    private int portaOrigem;
    private int portaDestino;
    private int idUser;
    private int chucnkTransferencia;
    private byte[] data;

    public Packet (int tipo, String ipOrigem, String ipDestino, int portaOrigem, int portaDestino, int idUser, int chucnkTransferencia, byte[] data) {
        this.tipo = tipo;
        this.ipOrigem = ipOrigem;
        this.ipDestino = ipDestino;
        this.portaOrigem = portaOrigem;
        this.portaDestino = portaDestino;
        this.idUser = idUser;
        this.chucnkTransferencia = chucnkTransferencia;
        this.data = data;
    }

    public Packet (byte[] conteudo) throws UnknownHostException {
        byte[] arrayBytes;
        arrayBytes = decrypt(conteudo);

        byte[] auxiliar = new byte[4];

        this.tipo = ByteBuffer.wrap(arrayBytes,0,4).getInt();

        System.arraycopy(arrayBytes, 4, auxiliar, 0, 4);
        this.ipOrigem = InetAddress.getByAddress(auxiliar).getHostAddress();

        System.arraycopy(arrayBytes, 8, auxiliar, 0, 4);
        this.ipDestino = InetAddress.getByAddress(auxiliar).getHostAddress();

        this.portaOrigem = ByteBuffer.wrap(arrayBytes,12,4).getInt();
        this.portaDestino = ByteBuffer.wrap(arrayBytes,16,4).getInt();
        this.idUser = ByteBuffer.wrap(arrayBytes,20,4).getInt();
        this.chucnkTransferencia = ByteBuffer.wrap(arrayBytes,24,4).getInt();

        byte[] newData = new byte[arrayBytes.length - 28];
        System.arraycopy(arrayBytes, 28, newData, 0, arrayBytes.length-28);
        this.data = newData;
    }


    byte[] packetToBytes() throws UnknownHostException {

        byte[] pacoteEmBytes = new byte[28 + this.data.length];

        byte[] tipo = convertIntToByteArray(this.tipo);
        System.arraycopy(tipo,0,pacoteEmBytes,0,4);
        byte[] ipOrigem = InetAddress.getByName(this.ipOrigem).getAddress();
        System.arraycopy(ipOrigem,0,pacoteEmBytes,4,4);
        byte[] ipDestino = InetAddress.getByName(this.ipDestino).getAddress();
        System.arraycopy(ipDestino,0,pacoteEmBytes,8,4);
        byte[] portaOrigem = convertIntToByteArray(this.portaOrigem);
        System.arraycopy(portaOrigem,0,pacoteEmBytes,12,4);
        byte[] portaDestino = convertIntToByteArray(this.portaDestino);
        System.arraycopy(portaDestino,0,pacoteEmBytes,16,4);
        byte[] idUser = convertIntToByteArray(this.idUser);
        System.arraycopy(idUser,0,pacoteEmBytes,20,4);
        byte[] chucnkTransferencia = convertIntToByteArray(this.chucnkTransferencia);
        System.arraycopy(chucnkTransferencia,0,pacoteEmBytes,24,4);
        System.arraycopy(this.data,0,pacoteEmBytes,28,this.data.length);

        System.out.println(Arrays.toString(encrypt(pacoteEmBytes)));
        return encrypt(pacoteEmBytes);
    }


    private byte[] convertIntToByteArray (int inteiro) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(inteiro);
        return byteBuffer.array();
    }


    @Override
    public String toString() {
        return "Packet{" +
                "tipo=" + tipo +
                ", ipOrigem='" + ipOrigem + '\'' +
                ", ipDestino='" + ipDestino + '\'' +
                ", portaOrigem=" + portaOrigem +
                ", portaDestino=" + portaDestino +
                ", idUser=" + idUser +
                ", chucnkTransferencia=" + chucnkTransferencia +
                ", data=" + new String(data, StandardCharsets.UTF_8) + 
                '}';
    }

    public int getTipo() {
        return tipo;
    }

    public String getIpOrigem() {
        return ipOrigem;
    }

    public String getIpDestino() {
        return ipDestino;
    }

    public int getPortaOrigem() {
        return portaOrigem;
    }

    public int getPortaDestino() {
        return portaDestino;
    }

    public int getIdUser() {
        return idUser;
    }

    public int getChucnkTransferencia() {
        return chucnkTransferencia;
    }

    public byte[] getData() {
        return data;
    }

    public String getDataString() {
        return new String(data, StandardCharsets.UTF_8);
    }

    private byte[] encrypt(byte[] message) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKey = new SecretKeySpec(ENCRIPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(message, 0, message.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] decrypt(byte[] message) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKeySpec secretKey = new SecretKeySpec(ENCRIPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return cipher.doFinal(message, 0, message.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
