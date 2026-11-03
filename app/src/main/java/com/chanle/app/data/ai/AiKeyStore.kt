package com.chanle.app.data.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * The API key is encrypted with a hardware-backed key in Android Keystore.
 * It never leaves the device and is never written into backups or logs.
 */
class AiKeyStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("ai_keys", Context.MODE_PRIVATE)

    fun save(apiKey: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.doFinal(apiKey.trim().toByteArray())
        val payload = Base64.encodeToString(cipher.iv, Base64.NO_WRAP) +
            ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP)
        prefs.edit().putString(SLOT, payload).apply()
    }

    fun read(): String? {
        val payload = prefs.getString(SLOT, null) ?: return null
        val parts = payload.split(":")
        if (parts.size != 2) return null
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey(),
                GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)),
            )
            String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)))
        }.getOrNull()
    }

    fun has(): Boolean = prefs.contains(SLOT)

    fun clear() {
        prefs.edit().remove(SLOT).apply()
    }

    fun masked(): String? {
        val key = read() ?: return null
        if (key.length <= 8) return "已保存"
        return key.take(4) + "…" + key.takeLast(4)
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "chanle-ai-key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val SLOT = "custom"
    }
}
