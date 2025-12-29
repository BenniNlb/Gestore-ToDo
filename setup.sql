-- Script configurazione Database

DROP TABLE IF EXISTS todo_links CASCADE;
DROP TABLE IF EXISTS todo_condivisione CASCADE;
DROP TABLE IF EXISTS todo CASCADE;
DROP TABLE IF EXISTS bacheca CASCADE;
DROP TABLE IF EXISTS utente CASCADE;

-- 1. Tabella UTENTE
CREATE TABLE utente (
                        id_utente SERIAL PRIMARY KEY,
                        username VARCHAR(50) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL
);

-- 2. Tabella BACHECA
CREATE TABLE bacheca (
                         id_bacheca SERIAL PRIMARY KEY,
                         titolo VARCHAR(20) NOT NULL,
                         descrizione VARCHAR(50),
                         posizioneB INTEGER DEFAULT 0,
                         id_utente INTEGER NOT NULL,

                         CONSTRAINT fk_utente
                             FOREIGN KEY(id_utente)
                                 REFERENCES utente(id_utente)
                                 ON DELETE CASCADE
);

-- 3. Tabella TODO
CREATE TABLE todo (
                      id_todo SERIAL PRIMARY KEY,
                      titolo VARCHAR(35) NOT NULL,
                      descrizione VARCHAR(350),
                      data_scadenza DATE,
                      colore_sfondo VARCHAR(7),
                      immagine BYTEA,
                      stato BOOLEAN NOT NULL DEFAULT false,
                      posizione INTEGER NOT NULL DEFAULT 0,
                      id_bacheca INTEGER NOT NULL,
                      id_utente_creatore INTEGER NOT NULL,

                      CONSTRAINT fk_bacheca
                          FOREIGN KEY(id_bacheca)
                              REFERENCES bacheca(id_bacheca)
                              ON DELETE CASCADE,

                      CONSTRAINT fk_utente_creatore
                          FOREIGN KEY(id_utente_creatore)
                              REFERENCES utente(id_utente)
                              ON DELETE CASCADE
);

-- 4. Tabella TODO_LINKS
CREATE TABLE todo_links (
                            id_link SERIAL PRIMARY KEY,
                            id_todo INTEGER NOT NULL,
                            url TEXT NOT NULL,

                            CONSTRAINT fk_todo_link
                                FOREIGN KEY(id_todo)
                                    REFERENCES todo(id_todo)
                                    ON DELETE CASCADE
);

-- 5. Tabella TODO_CONDIVISIONE
CREATE TABLE todo_condivisione (
                                   id_condivisione SERIAL PRIMARY KEY,
                                   id_todo INTEGER NOT NULL,
                                   id_utente INTEGER NOT NULL,
                                   permesso VARCHAR(20) NOT NULL DEFAULT 'SOLO_LETTURA',

                                   CONSTRAINT fk_todo_condivisione
                                       FOREIGN KEY(id_todo)
                                           REFERENCES todo(id_todo)
                                           ON DELETE CASCADE,

                                   CONSTRAINT fk_utente_condivisione
                                       FOREIGN KEY(id_utente)
                                           REFERENCES utente(id_utente)
                                           ON DELETE CASCADE,

                                   UNIQUE(id_todo, id_utente)
);

-- Creazione INDICI
CREATE INDEX idx_bacheca_utente ON bacheca(id_utente);
CREATE INDEX idx_todo_bacheca ON todo(id_bacheca);
CREATE INDEX idx_links_todo ON todo_links(id_todo);
CREATE INDEX idx_condivisione_todo ON todo_condivisione(id_todo);
CREATE INDEX idx_condivisione_utente ON todo_condivisione(id_utente);