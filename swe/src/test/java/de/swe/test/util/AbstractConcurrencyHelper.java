package de.swe.test.util;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.jboss.logging.Logger;

/**
 */
public abstract class AbstractConcurrencyHelper implements Callable<Void> {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	public enum Cmd {
		UPDATE,
		DELETE
	}
	
	protected Cmd cmd;
	
	protected AbstractConcurrencyHelper(Cmd cmd) {
		this.cmd = cmd;
	}
	
	@Override
	public Void call() {
		LOGGER.debug("BEGINN run");
		
		UserTransaction trans;
		Context ctx = null;
		try {
			try {
				ctx = new InitialContext();
				trans = (UserTransaction) ctx.lookup("java:jboss/UserTransaction");
			}
			finally {
				if (ctx != null) {
					ctx.close();
				}
			}
		}
		catch (NamingException e) {
			throw new RuntimeException(e);
		}

		try {
			trans.begin();
		}
		catch (NotSupportedException | SystemException e) {
			throw new RuntimeException(e);
		}

		if (cmd.equals(Cmd.UPDATE)) {
			update();
		}
		else if (cmd.equals(Cmd.DELETE)) {
			delete();
		}
		
		try {
			trans.commit();
		}
		catch (SecurityException |
			   IllegalStateException |
			   RollbackException |
			   HeuristicMixedException |
			   HeuristicRollbackException |
			   SystemException e) {
			throw new RuntimeException(e);
		}
		
		LOGGER.debug("ENDE run");
		return null;
	}
	
	protected abstract void update();
	protected abstract void delete();
}
