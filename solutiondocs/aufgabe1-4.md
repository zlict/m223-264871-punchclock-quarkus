# Lösungsvorschlag: Aufgabe 1.4 – Übungsprojekt analysieren

## 2. UML-Anwendungsfalldiagramm
Mermaid unterstützt keine native UML-Anwendungsfalldiagramm-Syntax. Deshalb stellt ein Flowchart den Akteur, die Systemgrenze und die ovalen Anwendungsfälle dar. Die Verbindungen sind ungerichtete UML-Assoziationen.

```mermaid
flowchart LR
    person["«actor»<br/>Benutzer/in"]
    subgraph punchclock["Punch Clock"]
        anzeigen([Zeitbuchungen anzeigen])
        erfassen([Zeitbuchung erfassen])
    end
    person --- anzeigen
    person --- erfassen
```

## 3. UML-Klassendiagramm des Backends

```mermaid
classDiagram
    direction LR

    class EntryController {
        <<REST Controller>>
        ~EntryService entryService
        +index() List~Entry~
        +create(Entry entry) Entry
    }

    class EntryService {
        <<Service>>
        -EntryRepository entryRepository
        +createEntry(Entry entry) Entry
        +findAll() List~Entry~
    }

    class EntryRepository {
        <<Repository>>
    }

    class PanacheRepository {
        <<interface>>
        +persist(Entry entity) void
        +listAll() List~Entry~
    }

    class Entry {
        <<Entity>>
        -Long id
        -LocalDateTime checkIn
        -LocalDateTime checkOut
        +getId() Long
        +setId(Long id) void
        +getCheckIn() LocalDateTime
        +setCheckIn(LocalDateTime checkIn) void
        +getCheckOut() LocalDateTime
        +setCheckOut(LocalDateTime checkOut) void
    }

    EntryController --> EntryService : injiziert
    EntryService --> EntryRepository : injiziert
    PanacheRepository <|.. EntryRepository : implementiert für Entry
    EntryController ..> Entry : Parameter und Rückgabe
    EntryService ..> Entry : Parameter und Rückgabe
    EntryRepository ..> Entry : persistiert und liest
```


## 4. Entity Relationship Diagram als UML-Klassendiagramm

```mermaid
classDiagram
    class entry {
        <<table>>
        bigint id
        timestamp checkin
        timestamp checkout
    }
```

| Java-Attribut | Erwartete PostgreSQL-Spalte | SQL-Typ | Einschränkungen |
| --- | --- | --- | --- |
| `id: Long` | `id` | `bigint` | Primärschlüssel, `NOT NULL`, durch die Datenbank generiert (IDENTITY) |
| `checkIn: LocalDateTime` | `checkin` | `timestamp without time zone` | `NOT NULL` |
| `checkOut: LocalDateTime` | `checkout` | `timestamp without time zone` | `NOT NULL` |


## 5. Sequenzdiagramme


### 5.1 Zeitbuchung erfassen

```mermaid
sequenceDiagram
    participant Browser
    participant EntryController
    participant EntryService
    participant EntryRepository
    participant Datenbank

    Browser->>Browser: Save klicken, Formulardaten aufbereiten
    Browser->>EntryController: POST /entries, JSON mit checkIn und checkOut
    EntryController->>EntryService: createEntry(entry)
    Note over EntryService,Datenbank: Speichern innerhalb einer Transaktion
    EntryService->>EntryRepository: persist(entry)
    EntryRepository->>Datenbank: INSERT in entry (checkin, checkout)
    Datenbank-->>EntryRepository: Generierte ID
    Note over EntryRepository: ID wird in Entry gesetzt
    EntryRepository-->>EntryService: void
    Note over EntryService,Datenbank: Transaktion vor Rückgabe an Controller abschließen
    EntryService-->>EntryController: Entry mit ID
    EntryController-->>Browser: HTTP 200 OK, JSON der gespeicherten Entry
    Browser->>Browser: Buchung ergänzen und Tabelle aktualisieren
```

### 5.2 Zeitbuchungen anzeigen

```mermaid
sequenceDiagram
    participant Browser
    participant EntryController
    participant EntryService
    participant EntryRepository
    participant Datenbank

    Browser->>Browser: Seite öffnen und indexEntries() auslösen
    Browser->>EntryController: GET /entries
    EntryController->>EntryService: findAll()
    EntryService->>EntryRepository: listAll()
    EntryRepository->>Datenbank: SELECT aus entry
    Datenbank-->>EntryRepository: Datenbankzeilen als Entry-Liste
    EntryRepository-->>EntryService: List of Entry
    EntryService-->>EntryController: List of Entry
    EntryController-->>Browser: HTTP 200 OK, JSON-Liste
    Browser->>Browser: Zeitbuchungen in Tabelle anzeigen
```
