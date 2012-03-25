USE ${dbname.mysql};

LOAD DATA LOCAL INFILE 'C:/Users/Nhat/eclipse-git/swe/src/test/resources/mysql/kunde.csv'
INTO TABLE kunde
FIELDS TERMINATED BY ';'
OPTIONALLY ENCLOSED BY '"'
IGNORE 1 LINES;

LOAD DATA LOCAL INFILE 'C:/Users/Nhat/eclipse-git/swe/src/test/resources/mysql/adresse.csv'
INTO TABLE adresse
FIELDS TERMINATED BY ';'
OPTIONALLY ENCLOSED BY '"'
IGNORE 1 LINES;

LOAD DATA LOCAL INFILE 'C:/Users/Nhat/eclipse-git/swe/src/test/resources/mysql/bestellposition.csv'
INTO TABLE bestellposition
FIELDS TERMINATED BY ';'
OPTIONALLY ENCLOSED BY '"'
IGNORE 1 LINES;

LOAD DATA LOCAL INFILE 'C:/Users/Nhat/eclipse-git/swe/src/test/resources/mysql/bestellung.csv'
INTO TABLE bestellung
FIELDS TERMINATED BY ';'
OPTIONALLY ENCLOSED BY '"'
IGNORE 1 LINES;

LOAD DATA LOCAL INFILE 'C:/Users/Nhat/eclipse-git/swe/src/test/resources/mysql/autohersteller.csv'
INTO TABLE autohersteller
FIELDS TERMINATED BY ';'
OPTIONALLY ENCLOSED BY '"'
IGNORE 1 LINES;

LOAD DATA LOCAL INFILE 'C:/Users/Nhat/eclipse-git/swe/src/test/resources/mysql/fahrzeug.csv'
INTO TABLE fahrzeug
FIELDS TERMINATED BY ';'
OPTIONALLY ENCLOSED BY '"'
IGNORE 1 LINES;