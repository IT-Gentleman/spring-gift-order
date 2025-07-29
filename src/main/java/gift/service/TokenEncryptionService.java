package gift.service;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class TokenEncryptionService {

    private final StringEncryptor encryptor;

    public TokenEncryptionService(@Qualifier("jasyptStringEncryptor") StringEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public String encrypt(String token) {
        return encryptor.encrypt(token);
    }

    public String decrypt(String encryptedToken) {
        return encryptor.decrypt(encryptedToken);
    }
}
