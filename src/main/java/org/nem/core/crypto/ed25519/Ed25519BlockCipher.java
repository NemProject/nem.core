package org.nem.core.crypto.ed25519;

import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.*;
import org.bouncycastle.crypto.params.*;
import org.nem.core.crypto.BlockCipher;
import org.nem.core.crypto.*;
import org.nem.core.crypto.ed25519.arithmetic.*;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Implementation of the block cipher for Ed25519.
 */
public class Ed25519BlockCipher implements BlockCipher {
	private final KeyPair senderKeyPair;
	private final KeyPair recipientKeyPair;
	private final SecureRandom random;
	private final int keyLength;

	public Ed25519BlockCipher(final KeyPair senderKeyPair, final KeyPair recipientKeyPair) {
		this.senderKeyPair = senderKeyPair;
		this.recipientKeyPair = recipientKeyPair;
		this.random = new SecureRandom();
		this.keyLength = recipientKeyPair.getPublicKey().getRaw().length;
	}

	@Override
	public byte[] encrypt(final byte[] input) {
		// Setup salt.
		final byte[] salt = new byte[this.keyLength];
		this.random.nextBytes(salt);

		// Derive shared key.
		final byte[] sharedKey = this.getSharedKey(this.senderKeyPair.getPrivateKey(), this.recipientKeyPair.getPublicKey(), salt);

		// Setup IV.
		final byte[] ivData = new byte[16];
		this.random.nextBytes(ivData);

		// Setup block cipher.
		final BufferedBlockCipher cipher = this.setupBlockCipher(sharedKey, ivData, true);

		// Encode.
		final byte[] buf = this.transform(cipher, input);
		if (null == buf) {
			return null;
		}

		final byte[] result = new byte[salt.length + ivData.length + buf.length];
		System.arraycopy(salt, 0, result, 0, salt.length);
		System.arraycopy(ivData, 0, result, salt.length, ivData.length);
		System.arraycopy(buf, 0, result, salt.length + ivData.length, buf.length);
		return result;
	}

	@Override
	public byte[] decrypt(final byte[] input) {
		if (input.length < 64) {
			return null;
		}

		final byte[] salt = Arrays.copyOfRange(input, 0, this.keyLength);
		final byte[] ivData = Arrays.copyOfRange(input, this.keyLength, 48);
		final byte[] encData = Arrays.copyOfRange(input, 48, input.length);

		// Derive shared key.
		final byte[] sharedKey = this.getSharedKey(this.recipientKeyPair.getPrivateKey(), this.senderKeyPair.getPublicKey(), salt);

		// Setup block cipher.
		final BufferedBlockCipher cipher = this.setupBlockCipher(sharedKey, ivData, false);

		// Decode.
		return this.transform(cipher, encData);
	}

	private byte[] transform(final BufferedBlockCipher cipher, final byte[] data) {
		final byte[] buf = new byte[cipher.getOutputSize(data.length)];
		int length = cipher.processBytes(data, 0, data.length, buf, 0);
		try {
			length += cipher.doFinal(buf, length);
		} catch (final InvalidCipherTextException e) {
			e.printStackTrace();
			return null;
		}

		return Arrays.copyOf(buf, length);
	}

	private BufferedBlockCipher setupBlockCipher(final byte[] sharedKey, final byte[] ivData, final boolean forEncryption) {
		// Setup cipher parameters with key and IV.
		final KeyParameter keyParam = new KeyParameter(sharedKey);
		final CipherParameters params = new ParametersWithIV(keyParam, ivData);

		// Setup AES cipher in CBC mode with PKCS7 padding.
		final BlockCipherPadding padding = new PKCS7Padding();
		final BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), padding);
		cipher.reset();
		cipher.init(forEncryption, params);
		return cipher;
	}

	private byte[] getSharedKey(final PrivateKey privateKey, final PublicKey publicKey, final byte[] salt) {
		final Ed25519GroupElement senderA = new Ed25519EncodedGroupElement(publicKey.getRaw()).decode();
		senderA.precomputeForScalarMultiplication();
		final byte[] sharedKey = senderA.scalarMultiply(Ed25519Utils.prepareForScalarMultiply(privateKey)).encode().getRaw();
		for (int i = 0; i < this.keyLength; i++) {
			sharedKey[i] ^= salt[i];
		}

		return Hashes.sha3_256(sharedKey);
	}
}
