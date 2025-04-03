package dev.ridill.rivo.core.domain.crypto

import android.security.keystore.KeyProperties
import dev.ridill.rivo.core.domain.util.logD
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class DefaultCryptoManager : CryptoManager {

    private fun getEncryptCipher(password: String, salt: String): Cipher = Cipher
        .getInstance(CryptoManager.TRANSFORMATION)
        .apply {
            init(Cipher.ENCRYPT_MODE, createKey(password, salt))
        }

    private fun getDecryptCipher(password: String, salt: String, iv: ByteArray): Cipher = Cipher
        .getInstance(CryptoManager.TRANSFORMATION)
        .apply {
            init(Cipher.DECRYPT_MODE, createKey(password, salt), IvParameterSpec(iv))
        }

    private fun createKey(password: String, salt: String): SecretKey {
        val factory = SecretKeyFactory.getInstance(CryptoManager.KEY_ALGORITHM)
        val keySpec = PBEKeySpec(
            password.toCharArray(),
            salt.toByteArray(),
            CryptoManager.ITERATION_COUNT,
            CryptoManager.KEY_LENGTH
        )
        val key = factory.generateSecret(keySpec)
        return SecretKeySpec(key.encoded, CryptoManager.ALGORITHM)
    }

    override fun encrypt(rawData: ByteArray, password: String, salt: String): EncryptionResult {
        val cipher = getEncryptCipher(password = password, salt = salt)
        val encryptedData = cipher.doFinal(rawData)
        return EncryptionResult(
            data = encryptedData,
            iv = cipher.iv
        )
    }

    override fun decrypt(
        encryptedData: ByteArray,
        iv: ByteArray,
        password: String,
        salt: String
    ): ByteArray = getDecryptCipher(
        password = password,
        salt = salt,
        iv = iv
    ).doFinal(encryptedData)

    @OptIn(ExperimentalStdlibApi::class)
    override fun generateSalt(): HashSaltString {
        val saltBytes = ByteArray(CryptoManager.SALT_LENGTH)
        SecureRandom().nextBytes(saltBytes)
        return saltBytes.toHexString()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun saltedHash(message: String, salt: String): Pair<HashString, HashSaltString> {
        val saltedMessage = "$salt$message"
        val hashResult = MessageDigest.getInstance(KeyProperties.DIGEST_SHA256)
            .digest(saltedMessage.toByteArray()).toHexString()
        logD("DefaultCryptoManager") { "saltedHash() called with: message = $message, saltString = $salt, saltedMessage = $saltedMessage, resultHash = $hashResult" }
        return hashResult to salt
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun areEqual(value: String?, hash2: String?, commonSalt: HashSaltString?): Boolean {
        val (valueHash, _) = saltedHash(value.orEmpty(), commonSalt ?: generateSalt())
        return MessageDigest.isEqual(valueHash.hexToByteArray(), hash2?.hexToByteArray())
    }
}