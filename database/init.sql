CREATE TABLE UserTable (userId SERIAL PRIMARY KEY, email VARCHAR NOT NULL UNIQUE,password VARCHAR NOT NULL);
CREATE TABLE JugTable (id INTEGER PRIMARY KEY,userId INTEGER NOT NULL REFERENCES UserTable(userId));