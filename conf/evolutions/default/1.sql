# Contact schema

# --- !Ups
CREATE TABLE CONTACTS
(
  id BIGINT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  name VARCHAR(50) NOT NULL,
  phone VARCHAR(11) NOT NULL
);

INSERT INTO Contacts (name, phone) VALUES ('Rob Bourdon', '79786484021');
INSERT INTO Contacts (name, phone) VALUES ('Brad Delson', '79787215416');
INSERT INTO Contacts (name, phone) VALUES ('Dave Farrell', '79788304776');
INSERT INTO Contacts (name, phone) VALUES ('Joe Hahn', '79789011420');
INSERT INTO Contacts (name, phone) VALUES ('Chester Bennington', '79781975733');
INSERT INTO Contacts (name, phone) VALUES ('Mike Shinoda', '79787964903');
INSERT INTO Contacts (name, phone) VALUES ('John Dolmayan', '79789017393');
INSERT INTO Contacts (name, phone) VALUES ('Shavo Odadjian', '79785923799');
INSERT INTO Contacts (name, phone) VALUES ('Daron Malakian', '79785292309');
INSERT INTO Contacts (name, phone) VALUES ('Serj Tankian', '79783366207');
INSERT INTO Contacts (name, phone) VALUES ('Sam Rivers', '79788265118');
INSERT INTO Contacts (name, phone) VALUES ('Leor Dimand', '79783912498');
INSERT INTO Contacts (name, phone) VALUES ('Wes Borland', '79787416079');
INSERT INTO Contacts (name, phone) VALUES ('John Otto', '79786738912');
INSERT INTO Contacts (name, phone) VALUES ('Fred Durst', '79789837709');
INSERT INTO Contacts (name, phone) VALUES ('Ray Luzier', '79786482318');
INSERT INTO Contacts (name, phone) VALUES ('Reginald Arvizu', '79781249791');
INSERT INTO Contacts (name, phone) VALUES ('James Shuffer', '79787175167');
INSERT INTO Contacts (name, phone) VALUES ('Brian Welch', '79783247314');
INSERT INTO Contacts (name, phone) VALUES ('Jonathan Davis', '79789435747');

# --- !Downs
drop table CONTACTS;