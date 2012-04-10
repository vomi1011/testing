package de.swe.util.jboss;

import static de.swe.util.Constants.SECURITY_DOMAIN;
import static de.swe.util.Constants.UID;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;

import javax.inject.Singleton;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

@Singleton
public class SecurityCache implements Serializable {
	private static final long serialVersionUID = UID;
	private static final String HOST = "localhost";
	private static final int MANAGEMENT_PORT = 9999;

	/**
	 * siehe http://community.jboss.org/thread/169263
	 * siehe https://docs.jboss.org/author/display/AS7/Management+Clients
	 * siehe https://github.com/jbossas/jboss-as/blob/master/controller-client/src/main/java/org/jboss/as/controller/client/ModelControllerClient.java
	 * siehe http://community.jboss.org/wiki/FormatOfADetypedOperationRequest
	 * siehe http://community.jboss.org/wiki/DetypedDescriptionOfTheAS7ManagementModel
	 * 
	 * Gleicher Ablauf mit CLI (= command line interface):
	 * cd %JBOSS_HOME%\bin
	 * jboss-admin.bat
	 *    connect
	 *    /subsystem=security/security-domain=shop:flush-cache(principal=myUserName)
	 */
	public void remove(String username) {
		ModelControllerClient client;
		try {
			client = ModelControllerClient.Factory.create(HOST, MANAGEMENT_PORT);
		}
		catch (UnknownHostException e) {
			// Kann nicht passieren: sonst waere "localhost" nicht bekannt
			throw new IllegalStateException(e);
		}
		
		try {
			final ModelNode address = new ModelNode();
			address.add("subsystem", "security");
			address.add("security-domain", SECURITY_DOMAIN);

			final ModelNode operation = new ModelNode();
			operation.get("address").set(address);
			operation.get("operation").set("flush-cache");
			operation.get("principal").set(username);

			try {
				final ModelNode result = client.execute(operation);
				final String resultString = result.get("outcome").asString();
				if (!"success".equals(resultString)) {
					throw new IllegalStateException("FEHLER bei der Operation \"flush-cache\" fuer den Security-Cache: "
							                        + resultString);
				}
			}
			catch (IOException e) {
				throw new IllegalStateException("FEHLER bei der Operation \"flush-cache\" fuer den Security-Cache", e);
			}

		}
		finally {
			if (client != null) {
				try {
					client.close();
				}
				catch (IOException e) {
					throw new IllegalStateException("FEHLER bei der Methode close() fuer den Management-Client", e);
				}
			}
		}
	}
}
