import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Classe que implementa o Pacote de envio
 */
public class Packet implements Serializable {
    public static final int MAX_SIZE_DATA = 4096;
    public static final int MAX_SIZE_PACKET = MAX_SIZE_DATA + 28;
    public static final String ENCRIPTION_KEY = "CC21-TP2-Grupo14";

    // 1 - Pergunta aos FFs se ficheiro existe | 2 - Responde que possui o ficheiro       | 3 - Responde que ficheiro nao existe
    // 4 - Pede ao FFs um ficheiro que possui  | 5 - Envia o ficheiro requisitado
    // 6 - FFs informa que se pretende ligar   | 7 - FFs informa que se pretende desligar | 8 - Gateway informa que FSs se ligou corretamente
    // 9 - Gateway envia um Keep Alive         | 10- Gateway confirma que conexão foi cancelada | 11 - Erro indeterminado
    private final int tipo;
    private final String ipOrigem;
    private final String ipDestino;
    private final int portaOrigem;
    private final int portaDestino;
    private final int idUser;
    private final int chucnkTransferencia;
    private final byte[] data;

    /**
     * Construtor da classe Packet
     *
     * @param tipo                  Tipo do pacote
     * @param ipOrigem              IP de origem do pacote
     * @param ipDestino             IP de destino do pacote
     * @param portaOrigem           Porta de origem do pacote
     * @param portaDestino          Porta de destino do pacote
     * @param idUser                ID do Utilizador que fez o request (-1 se nao se refere a um utilizador)
     * @param chucnkTransferencia   Número do chunk da transferência
     * @param data                  Informação do pacote
     */
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

    /**
     * Construtor da classe Packet a parte de um array de bytes encriptado
     *
     * @param conteudo              Array de bytes encriptado com o conteudo do pacote
     * @throws UnknownHostException
     */
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

    /**
     * Método que transforma um pacote num array de bytes encriptado
     *
     * @return                      Array de bytes encriptado com a informação do pacote
     * @throws UnknownHostException
     */
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

        return encrypt(pacoteEmBytes);
    }

    /**
     * Método que converte um inteiro num array de bytes
     *
     * @param inteiro   Inteiro a ser convertido
     * @return          Array de bytes com o inteiro convertido
     */
    private byte[] convertIntToByteArray (int inteiro) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(inteiro);
        return byteBuffer.array();
    }

    /**
     * Método que transforma a classe numa String
     *
     * @return  String transformada
     */
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

    /**
     * Método que obtém o tipo
     *
     * @return  Inteiro com o tipo
     */
    public int getTipo() {
        return tipo;
    }

    /**
     * Método que obtém o IP de origem
     *
     * @return  String com o IP de origem
     */
    public String getIpOrigem() {
        return ipOrigem;
    }

    /**
     * Método que obtém o IP de destino
     *
     * @return  String com o IP de destino
     */
    public String getIpDestino() {
        return ipDestino;
    }

    /**
     * Método que obtém a porta de origem
     *
     * @return  Inteiro com a Porta de origem
     */
    public int getPortaOrigem() {
        return portaOrigem;
    }

    /**
     * Método que obtém a porta de destino
     *
     * @return  Inteiro com a Porta de destino
     */
    public int getPortaDestino() {
        return portaDestino;
    }

    /**
     * Método que obtém o ID do utilizador
     *
     * @return  Inteiro com o ID do utilizador
     */
    public int getIdUser() {
        return idUser;
    }

    /**
     * Método que obtém o chunk da transferencia
     *
     * @return  Inteiro com o chunk da transferência
     */
    public int getChucnkTransferencia() {
        return chucnkTransferencia;
    }

    /**
     * Método que obtém a informação do pacote
     *
     * @return  Array de bytes com a informação do pacote
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Método que obtém a informação do pacote em formato de String
     *
     * @return  String com a informação do pacote
     */
    public String getDataString() {
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * Método que encripta um array de bytes para envio
     *
     * @param message   Array de bytes a ser encriptado
     * @return          Array de bytes encriptado
     */
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

    /**
     * Método que desencripta um array de bytes
     *
     * @param message   Array de bytes a ser desencriptado
     * @return          Array de bytes desencriptado
     */
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
