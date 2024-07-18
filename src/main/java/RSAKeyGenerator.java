import java.security.*;

public class RSAKeyGenerator {

    public static void main(String[] args) throws Exception {
        // Generate RSA key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // Key size
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Get the encoded forms of the keys
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

        // Encode the keys in Base64
        String privateKeyBase64 = java.util.Base64.getEncoder().encodeToString(privateKeyBytes);
        String publicKeyBase64 = java.util.Base64.getEncoder().encodeToString(publicKeyBytes);

        // Print the Base64 encoded keys
        System.out.println("Private Key (Base64): " + privateKeyBase64);
        System.out.println("Public Key (Base64): " + publicKeyBase64);
    }
}