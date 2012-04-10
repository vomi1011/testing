package de.swe.util.jboss;

import static de.swe.util.Constants.UID;
import static org.jboss.security.auth.spi.Util.createPasswordHash;

import java.io.Serializable;

import javax.inject.Singleton;

// Alternativ:
// org.jboss.crypto.CryptoUtil.createPasswordHash

/**
 * In Anlehnung an org.jboss.test.PasswordHasher von Scott Stark
 */
@Singleton
public class PasswordService implements Serializable {
	private static final long serialVersionUID = UID;

	private static final String HASH_ALGORITHM = "SHA-1";
	private static final String HASH_ENCODING = "base64";
	private static final String HASH_CHARSET = "UTF-8";

	/**
	 */
	public String verschluesseln(String password) {
		if (password == null) {
			return null;
		}
		final String passwordHash = createPasswordHash(HASH_ALGORITHM, HASH_ENCODING, HASH_CHARSET,
				                                       null, password);
		return passwordHash;
	}
}
