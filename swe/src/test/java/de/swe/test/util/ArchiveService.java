package de.swe.test.util;

import java.io.File;
import java.nio.file.Paths;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;

public enum ArchiveService {
	INSTANCE;

	private static final String WEB_PROJEKT = "swe";
	private static final String TEST_WAR = WEB_PROJEKT + ".war";
	
	private static final String CLASSES_DIR = "target/classes";
	
	private static final String WEBINF_DIR = "src/main/webapp/WEB-INF/";
	private static final String BEANS_XML = WEBINF_DIR + "beans.xml";
	private static final String EJBJAR_XML = WEBINF_DIR + "ejb-jar.xml";
	private static final String JBOSSEJB3_XML = WEBINF_DIR + "jboss-ejb3.xml";
	private static final String JBOSSWEB_XML = WEBINF_DIR + "jboss-web.xml";
	private static final String JBOSS_DEPLOYMENT_STRUCTURE_XML = WEBINF_DIR + "jboss-deployment-structure.xml";
	
	private static final String SOLDER_VERSION = "3.1.0.Final";
	
	private static final String INFINISPAN_CDI_VERSION = "5.1.2.FINAL";
	private static final String CACHE_API_VERSION = "0.4";
	
	private static final String SEAM_VERSION = "3.1.0.Final";
	private static final String PICKETLINK_VERSION = "1.5.0.Alpha02";
	private static final String PRETTYFACES_VERSION = "3.3.2";
	private static final String DROOLS_VERSION = "5.1.1";
	
	private static final String RICHFACES_VERSION = "4.2.0.Final";

	private static final String DBUNIT_VERSION = "2.4.8";
	
	private final WebArchive archive = ShrinkWrap.create(WebArchive.class, TEST_WAR);

	/**
	 */
	private ArchiveService() {
		addKlassen();
		
		addSolder();
		addInfinispanCdi();
		addSeam();
		addRichFaces();
		
		addWebInf();

		addTestklassen();
		addDbUnit();
		
//		final Path arquillianPath = Paths.get("target/arquillian");
//		try {
//			Files.createDirectories(arquillianPath);
//		}
//		catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		final File warFile = Paths.get(arquillianPath.toString(), "shop.war").toFile();
//		archive.as(ZipExporter.class).exportTo(warFile, true); 
	}
	
	private void addKlassen() {
		final JavaArchive tmp = ShrinkWrap.create(JavaArchive.class);
		tmp.as(ExplodedImporter.class).importDirectory(CLASSES_DIR);
		archive.merge(tmp, "WEB-INF/classes");
	}
	
	private void addWebInf() {
		// Gibt es WEB-INF\beans.xml? Ansonsten als leere Datei dem Web-Archiv hinzufuegen
		final File beansXml = Paths.get(BEANS_XML).toFile();
		if (beansXml.exists()) {
			archive.addAsWebInfResource(beansXml);
		}
		else {
			archive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		}
		
		// Gibt es WEB-INF\ejb-jar.xml fuer EJBs?
		final File ejbJarXml = Paths.get(EJBJAR_XML).toFile();
		if (ejbJarXml.exists()) {
			archive.addAsWebInfResource(ejbJarXml);
		}
		
		// Gibt es WEB-INF\jboss-ejb3.xml fuer Security?
		final File jbossEjb3Xml = Paths.get(JBOSSEJB3_XML).toFile();
		if (jbossEjb3Xml.exists()) {
			archive.addAsWebInfResource(jbossEjb3Xml);
		}

		// Gibt es WEB-INF\jboss-web.xml fuer Security?
		final File jbossWebXml = Paths.get(JBOSSWEB_XML).toFile();
		if (jbossWebXml.exists()) {
			archive.addAsWebInfResource(jbossWebXml);
		}
		
		// Gibt es WEB-INF\jboss-deployment-structure.xml fuer die Erweiterung des Classpath?
		final File jbossDeploymentStructureXml = Paths.get(JBOSS_DEPLOYMENT_STRUCTURE_XML).toFile();
		if (jbossDeploymentStructureXml.exists()) {
			archive.addAsWebInfResource(jbossDeploymentStructureXml);
		}
		else {
			// TODO addAsManifestResource() funktioniert hier nicht bei Web-Archiv
			archive.addAsManifestResource(new StringAsset("Manifest-Version: 1.0\n"
					                                      + "Dependencies: org.infinispan,org.infinispan.client.hotrod,org.jboss.as\n"
					                                      + " .controller-client,org.jboss.dmr,com.google.guava,org.hibernate,org.j\n"
					                                      + " oda.time,org.slf4j"),
					                                      "MANIFEST.MF");
		}
	}
	
	private void addSolder() {
		archive.addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
	                                              .goOffline()
	                                              .artifact("org.jboss.solder:solder-impl:" + SOLDER_VERSION)
	                                              .resolveAs(JavaArchive.class));
	}
	
	private void addInfinispanCdi() {
		archive.addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.infinispan:infinispan-cdi:" + INFINISPAN_CDI_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
	                                            .goOffline()
                                                .artifact("javax.cache:cache-api:" + CACHE_API_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next());
	}
	
	private void addSeam() {
		archive.addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.jboss.seam.persistence:seam-persistence-api:" + SEAM_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
            		                            .goOffline()
            		                            .artifact("org.jboss.seam.persistence:seam-persistence:" + SEAM_VERSION)
            		                            .resolveAs(JavaArchive.class)
            		                            .iterator()
            		                            .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
            	                          		.goOffline()
            	                          		.artifact("org.jboss.seam.transaction:seam-transaction-api:" + SEAM_VERSION)
            	                          		.resolveAs(JavaArchive.class)
            	                          		.iterator()
            	                          		.next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.jboss.seam.transaction:seam-transaction:" + SEAM_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.jboss.seam.faces:seam-faces-api:" + SEAM_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.jboss.seam.faces:seam-faces:" + SEAM_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.jboss.seam.international:seam-international-api:" + SEAM_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.jboss.seam.international:seam-international:" + SEAM_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                           		.goOffline()
                                           		.artifact("org.jboss.seam.security:seam-security-api:" + SEAM_VERSION)
                                           		.resolveAs(JavaArchive.class)
                                           		.iterator()
                                           		.next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.jboss.seam.security:seam-security:" + SEAM_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("com.ocpsoft:prettyfaces-jsf2:" + PRETTYFACES_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
            		                             // TODO goOffline() auskommentieren, falls com.sun.xml.bind.jaxb-impl:2.1.8 fehlt 
                                                .goOffline()
                                                .artifact("org.picketlink.idm:picketlink-idm-core:" + PICKETLINK_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
            		                            .goOffline()
            		                            .artifact("org.picketlink.idm:picketlink-idm-common:" + PICKETLINK_VERSION)
            		                            .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.picketlink.idm:picketlink-idm-spi:" + PICKETLINK_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.picketlink.idm:picketlink-idm-api:" + PICKETLINK_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next())
               .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.drools:drools-api:" + DROOLS_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next());
	}

	private void addRichFaces() {
		archive.addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
			                                    .goOffline()
			                                    .artifact("org.richfaces.ui:richfaces-components-ui:" + RICHFACES_VERSION)
			                                    .resolveAs(JavaArchive.class)
			                                    .iterator()
			                                    .next())
		       .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
		                                        .goOffline()
		                                        .artifact("org.richfaces.ui:richfaces-components-api:" + RICHFACES_VERSION)
		                                        .resolveAs(JavaArchive.class)
		                                        .iterator()
		                                        .next())
		       .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
		                                        .goOffline()
		                                        .artifact("org.richfaces.core:richfaces-core-impl:" + RICHFACES_VERSION)
		                                        .resolveAs(JavaArchive.class)
		                                        .iterator()
		                                        .next())
		       .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
		                                        .goOffline()
		                                        .artifact("org.richfaces.core:richfaces-core-api:" + RICHFACES_VERSION)
		                                        .resolveAs(JavaArchive.class)
		                                        .iterator()
		                                        .next());
	}

	private void addTestklassen() {
		for (Class<?> clazz : Testklassen.INSTANCE.getTestklassen()) {
			archive.addPackage(clazz.getPackage());
		}
	}
	
	private void addDbUnit() {
		archive.addAsResource(new File("src/test/resources/" + DbService.DATASET_XML), DbService.DATASET_XML)
               .addAsResource(new File("src/test/resources/" + DbService.DATASET_DTD), DbService.DATASET_DTD)
		       .addAsLibrary(DependencyResolvers.use(MavenDependencyResolver.class)
                                                .goOffline()
                                                .artifact("org.dbunit:dbunit:" + DBUNIT_VERSION)
                                                .resolveAs(JavaArchive.class)
                                                .iterator()
                                                .next());
	}
	
	public static ArchiveService getInstance() {
		return INSTANCE;
	}
	
	public Archive<?> getArchive() {
		return archive;
	}
}
