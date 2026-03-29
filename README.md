# Gestore-ToDo

[![Java](https://img.shields.io/badge/Java-11+-blue)]()
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)]()

Applicativo desktop sviluppato in **Java Swing** per la gestione avanzata e collaborativa delle attività, realizzato per l’esame di **Object Orientation** presso l’Università degli Studi di Napoli Federico II.

---

## Descrizione

Gestore-ToDo è un'applicazione desktop che consente la gestione strutturata di attività personali e condivise.

L’obiettivo del progetto è dimostrare l’applicazione pratica dei principi di programmazione orientata agli oggetti, con particolare attenzione a:

- separazione delle responsabilità
- progettazione modulare
- persistenza dei dati
- sicurezza

---

## Screenshot della Board

<img width="1512" height="948" alt="Screenshot 2026-03-29 alle 19 10 30" src="https://github.com/user-attachments/assets/d8946eb9-31f2-4c5e-8e09-cc8a1b42b9be" />


## Features Principali

Il sistema è stato ingegnerizzato seguendo rigorosamente l'architettura **BCE (Boundary-Control-Entity)** e il pattern **DAO (Data Access Object)**. 

* **Sicurezza e Autenticazione:** Registrazione e Login con password crittografate tramite algoritmo di hashing SHA-256.
* **Collaborazione Multi-Utente:** Condivisione dei singoli task con altri utenti registrati, con gestione granulare dei permessi (`SOLO_LETTURA` o `MODIFICA`).
* **Interattività Drag & Drop:** Riorganizzazione visiva dei ToDo all'interno delle bacheche tramite il trascinamento nativo delle schede (aggiornato in tempo reale sul DB).
* **Regole di Business Temporali:** Implementazione della "24h Rule", che inibisce automaticamente la modifica e il riposizionamento di un task se la scadenza è stata superata da oltre un giorno.
* **UI/UX Dinamica:** Calcolo algoritmico della luminanza per adattare automaticamente il colore del testo (bianco/nero) in base al colore di sfondo personalizzato scelto per il ToDo, garantendo sempre la massima leggibilità.
* **Eliminazione dell'account:** Possibilità di eliminazione definitiva dell'account con cancellazione a cascata (ON DELETE CASCADE) di tutti i dati associati, garantendo la conformità alla privacy e l'integrità del database.
* **Organizzazione Logica:** Suddivisione automatica dei task in 3 bacheche di sistema (`UNIVERSITA`, `LAVORO`, `TEMPO_LIBERO`).

## Requisiti di Sistema

* **Java Development Kit (JDK):** Versione 11+
* **Database:** PostgreSQL 12+
* **IDE usato:** IntelliJ IDEA.

## Installazione e Avvio

1. **Clona il repository:**
   ```bash
   git clone [https://github.com/BenniNlb/Gestore-ToDo.git](https://github.com/BenniNlb/Gestore-ToDo.git)
   ```

2. **Inizializza il Database:**
   Apri il tuo client PostgreSQL (es. pgAdmin) ed esegui lo script fornito nel repository:
   * `setup.sql`
   *(Questo script si occuperà di creare lo schema completo e di impostare tutti i vincoli `ON DELETE CASCADE` necessari per l'integrità referenziale).*

3. **Configura le Credenziali:**
   Apri il file `src/database/DBConnection.java` e aggiorna le costanti di connessione JDBC con i dati del tuo ambiente locale:
   ```java
   private static final String URL = "jdbc:postgresql://localhost:5432/Gestore-ToDo"; //
   private static final String USER = "il_tuo_username";
   private static final String PASSWORD = "la_tua_password";
   ```

4. **Avvia l'Applicazione:**
   Compila il progetto ed esegui la classe principale:
   `main.Main.java`

## Qualità del Codice

Il codice sorgente è stato sottoposto ad analisi statica per garantire l'assenza di *Code Smells*. Le connessioni al database sono gestite in modo sicuro prevenendo *memory leaks* (es. uso del costrutto `try-with-resources`) ed evitando vulnerabilità di *SQL Injection* tramite `PreparedStatement`. Il progetto include inoltre una documentazione tecnica completa generata tramite **Javadoc**.

## Tecnologie Utilizzate

* **Core:** Java
* **GUI:** Java Swing (con Look & Feel di sistema)
* **Database:** PostgreSQL (Driver JDBC)
* **Design Pattern:** MVC, BCE, DAO, Singleton

## Documentazione di Progetto
Nella cartella `documentazione/` sono disponibili:
* **UML dei Casi d'Uso e di Classe** (formato .vpp e .png)
* **Manuale Tecnico** in formato PDF
* **Javadoc completo** (nella cartella `/javadoc`)

## Autori

Progetto sviluppato da:
* **Benedetta Nalbone**
* **Gabriella Scaraglia**

## Licenza

Questo progetto è distribuito con licenza MIT. Sentiti libero di esplorare il codice e utilizzarlo come riferimento.
