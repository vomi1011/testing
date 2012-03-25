USE ${dbname.mysql};
SHOW WARNINGS;

DROP TABLE IF EXISTS kunde;
CREATE TABLE kunde(
	k_id SERIAL PRIMARY KEY,
	nachname VARCHAR(40) NOT NULL,
	vorname VARCHAR(40),
	firma VARCHAR(100),
	geschlecht VARCHAR(1) check('M' or 'W'),
	telefon varchar(20),
	email VARCHAR(100) NOT NULL UNIQUE,
	password VARCHAR(50),
	art VARCHAR(1) NOT NULL,
	erstellt TIMESTAMP NOT NULL DEFAULT 0,
	aktualisiert TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE kunde AUTO_INCREMENT = 1001;

DROP TABLE IF EXISTS adresse;

CREATE TABLE adresse(
	a_id SERIAL PRIMARY KEY,
	strasse VARCHAR(100),
	hausnr VARCHAR(4) NOT NULL,
	plz CHAR(5) NOT NULL,
	ort VARCHAR(50) NOT NULL,
	kunde_fk BIGINT NOT NULL REFERENCES kunde(k_id) ON DELETE CASCADE,
	erstellt TIMESTAMP NOT NULL DEFAULT 0,
	aktualisiert TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE adresse AUTO_INCREMENT = 2001;

DROP TABLE IF EXISTS autohersteller;

CREATE TABLE autohersteller(
	a_id SERIAL PRIMARY KEY,
	name VARCHAR(100) NOT NULL
);

ALTER TABLE autohersteller AUTO_INCREMENT = 7001;

DROP TABLE IF EXISTS fahrzeug;

CREATE TABLE fahrzeug(
	f_id SERIAL PRIMARY KEY,
	hersteller_fk BIGINT NOT NULL REFERENCES autohersteller(a_id),
	modell VARCHAR(50) NOT NULL,
	baujahr SMALLINT,
	beschreibung VARCHAR(200),
	preis INT,
	lieferbar BOOL,
	erstellt TIMESTAMP NOT NULL DEFAULT 0,
	aktualisiert TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE fahrzeug AUTO_INCREMENT = 6001;

DROP TABLE IF EXISTS bestellung;

CREATE TABLE bestellung(
	b_id SERIAL PRIMARY KEY,
	kunde_fk BIGINT NOT NULL REFERENCES kunde(k_id) ON DELETE CASCADE,
	idx SMALLINT NOT NULL,
	status VARCHAR(50),
	bestelldatum TIMESTAMP NOT NULL DEFAULT 0,
	aktualisiert TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

ALTER TABLE bestellung AUTO_INCREMENT = 5001;

CREATE INDEX bestellung_kunde_index ON bestellung(kunde_fk);

DROP TABLE IF EXISTS bestellposition;

CREATE TABLE bestellposition(
	bp_id SERIAL PRIMARY KEY,
	bestellung_fk BIGINT NOT NULL REFERENCES bestellung(b_id) ON DELETE CASCADE,
	fahrzeug_fk BIGINT NOT NULL REFERENCES fahrzeug(f_id) ON DELETE CASCADE,
	anzahl SMALLINT NOT NULL,
	idx SMALLINT NOT NULL
);

ALTER TABLE bestellposition AUTO_INCREMENT = 9001;

CREATE INDEX bestellposition_index ON bestellposition(bp_id);