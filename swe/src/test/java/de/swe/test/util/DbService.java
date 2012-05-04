package de.swe.test.util;


import static org.dbunit.operation.DatabaseOperation.CLEAN_INSERT;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mysql.MySqlConnection;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.jboss.logging.Logger;

import de.swe.util.Log;


/**
 * ACHTUNG: Diese Klasse sollte unbedingt fuer PostgreSQL und MySQL angepasst werden. Aktuell ermittelt sich dynamisch,
 * es PostgreSQL oder MySQL ist. Das fuehrt aber bei wiederholten Aufrufen zu MEMORY LEAKS, so dass man sehr oft JBoss
 * und Eclipse neu starten muss...
 */
@Singleton
@Log
public class DbService {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	public static final String DATASOURCE = "java:jboss/datasources/SweDS";  // siehe standalone.xml und persistence.xml
	public static final String DATASET_XML = "datasets/db.xml";
	public static final String DATASET_DTD = "datasets/db.dtd";
	
	@Resource(mappedName = DATASOURCE)
	private DataSource datasource;
	
	@Inject
	@org.jboss.solder.resourceLoader.Resource(DATASET_XML)
	private URL datasetUrl;
	
	private boolean dbReloaded = false;
	
	
	public void reload() throws SQLException, DatabaseUnitException {
		if (dbReloaded) {
			LOGGER.tracef("Die Datasource %s ist bereits neu geladen.", DATASOURCE);
			return;
		}
		
		LOGGER.infof("Die Datasource %s wird neu geladen...", DATASOURCE);
		Connection jdbcConn = null;
		IDatabaseConnection dbunitConn = null;
		try {
			jdbcConn = datasource.getConnection();
			
			final DatabaseMetaData metaData = jdbcConn.getMetaData();
			final String dbProdukt = metaData.getDatabaseProductName();
			LOGGER.infof("  Produkt:   %s %s", dbProdukt, metaData.getDatabaseProductVersion());
			LOGGER.infof("  Treiber:   %s", metaData.getDriverVersion());
			LOGGER.infof("  URL:       %s", metaData.getURL());
			LOGGER.infof("  Username:  %s", metaData.getUserName());
			
			boolean caseSensitiveTableNames = false;
			switch (dbProdukt) {
				case "PostgreSQL":
					final String schema = metaData.getUserName();
					LOGGER.infof("  Schema:    %s", schema);
					
					dbunitConn = new DatabaseConnection(jdbcConn, schema);
					dbunitConn.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
							                           new PostgresqlDataTypeFactory());
					caseSensitiveTableNames = true;
					break;
					
				case "MySQL":
					dbunitConn = new MySqlConnection(jdbcConn, null);
					if (System.getProperty("os.name").contains("Linux")) {
						caseSensitiveTableNames = true;
					}
					break;
					
				default:
					throw new IllegalStateException("Das Datenbankprodukt \"" + dbProdukt
							                        + "\" wird fuer " + DATASOURCE
							                        + " nicht unterstuetzt");
			}
			
			final FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
			flatXmlDataSetBuilder.setCaseSensitiveTableNames(caseSensitiveTableNames);
			final IDataSet dataset = flatXmlDataSetBuilder.build(datasetUrl);
			
			CLEAN_INSERT.execute(dbunitConn, dataset);
				
			// TODO Sequenzen fuer PostgreSQL, Oracle und DB2 zuruecksetzen
			
			dbReloaded = true;
			LOGGER.info("... die Datasource wurde neu geladen");
		}
		finally {
			if (dbunitConn != null) {
				dbunitConn.close();
			}
			if (jdbcConn != null) {
				jdbcConn.close();
			}
		}
	}
}
