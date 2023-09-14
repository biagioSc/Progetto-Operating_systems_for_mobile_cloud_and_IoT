
#include "imports.h"
#include "auxiliary.h"

/**
 * @brief Controlla se esiste un utente sul database registrato con la stessa email.
 * @param conn Il gestore della connessione al database.
 * @param email L'email dell'utente da verificare.
 * @return int Il numero di utenti che hanno l'email inserita.
 */
int check_email_already_used(PGconn *conn, char *email)
{
    char checkEmailQuery[BUFFER_SIZE * 2];
    sprintf(checkEmailQuery, "SELECT COUNT(*) FROM users WHERE email='%s';", email);
    PGresult *resCheckEmail = PQexec(conn, checkEmailQuery);

    if (PQresultStatus(resCheckEmail) != PGRES_TUPLES_OK)
    {
        fprintf(stderr, "Query failed: %s", PQerrorMessage(conn));
        PQclear(resCheckEmail);
        return -1;
    }

    int counterEmailCheck = atoi(PQgetvalue(resCheckEmail, 0, 0));
    PQclear(resCheckEmail);

    return counterEmailCheck;
}


/**
 * @brief Suggerisce un drink tra i preferiti dell'utente
 * @param conn Il gestore della connessione con il database.
 * @return char* Un drink randomico tra la lista dei drink preferiti.
 */
char *suggest_drink(PGconn *conn, const char *email) {
    if (!conn || PQstatus(conn) == CONNECTION_BAD) {
        printf("Connessione al database non valida o persa.\n");
        return NULL;
    }

    char query[BUFFER_SIZE];
    sprintf(query, "SELECT favdrink FROM users WHERE email='%s'", email);
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("Query execution failed: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    // Estraggo la lista dei drink preferiti
    char *favDrinksStr = strdup(PQgetvalue(res, 0, 0));
    char *drinks[15];
    int numDrinks = 0;

    char *token = strtok(favDrinksStr, ",");
    while (token) {
        drinks[numDrinks++] = strdup(token);
        token = strtok(NULL, ",");
    }

    // Stampiamo i drink preferiti
    printf("[Sugg-Drink] Drink preferiti dell'utente %s: ",email);
    for (int i = 0; i < numDrinks; i++) {
        printf("%s,", drinks[i]);
    }
    printf("\n");

    char *suggestedDrink = NULL;
    if (numDrinks > 0) {
        int randomIndex = rand() % numDrinks;
        suggestedDrink = strdup(drinks[randomIndex]);
        printf("[Sugg-Drink] Drink preferito randomico dell'utente %s: %s\n", email,suggestedDrink);
    }

    for (int i = 0; i < numDrinks; i++) {
        free(drinks[i]);
    }
    free(favDrinksStr);
    PQclear(res);

    return suggestedDrink;
}


/**
 * @brief Unisce le stringhe dei drink separandole con virgole.
 * @param drinks Array di stringhe contenenti i nomi dei drink.
 * @return char* Stringa con i nomi dei drink separati da virgole.
 */
char *join_drinks(char **drinks) {

    // Calcola la lunghezza totale necessaria per la stringa risultante.
    int total_length = 0;
    for (int i = 0; drinks[i] != NULL; i++) {
        total_length += strlen(drinks[i]) + 1;
    }
    total_length -= 1; // Rimuove l'ultima virgola.

    // Alloca memoria per la stringa risultante.
    char *result = malloc(total_length);
    if (!result) {
        perror("Errore nell'allocazione della memoria\n");
        return NULL;
    }

    // Puntatore alla posizione corrente nella stringa risultante.
    char *current_position = result;

    // Unisce i nomi dei drink nella stringa risultante.
    for (int i = 0; drinks[i] != NULL; i++) {
        strcpy(current_position, drinks[i]);
        current_position += strlen(drinks[i]);
        if (drinks[i + 1] != NULL) {
            *current_position = ',';
            current_position++;
        }
    }
    *current_position = '\0'; // Assicura che la stringa sia terminata.

    return result;
}

/**
 * @brief Estrae tutti i nomi dei drink dal database.
 * @param conn Il gestore della connessione al database.
 * @return char* Stringa con i nomi dei drink separati da virgole.
 */
char *get_drinks_name(PGconn *conn) {

    // Query per ottenere la lista dei drink.
    char query[BUFFER_SIZE];
    sprintf(query, "SELECT name FROM drinks;");
    PGresult *res = PQexec(conn, query);

    // Controlla se l'esecuzione della query ha avuto successo.
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("Errore nell'esecuzione della query: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    // Numero di drink estratti dal database.
    int num_drinks = PQntuples(res);

    // Alloca memoria per i nomi dei drink.
    char **drinks = malloc(sizeof(char *) * (num_drinks + 1));

    // Estrae i nomi dei drink e li copia nell'array.
    for (int i = 0; i < num_drinks; i++) {
        char *drink_name = PQgetvalue(res, i, 0);
        drinks[i] = strdup(drink_name);
    }
    drinks[num_drinks] = NULL; // Termina l'array con NULL.

    PQclear(res);

    // Utilizza la funzione per unire i nomi dei drink in una singola stringa.
    char *drinks_unificati = join_drinks(drinks);

    // Libera la memoria allocata per i nomi dei drink.
    for (int i = 0; i < num_drinks; i++) {
        free(drinks[i]);
    }
    free(drinks);

    return drinks_unificati;
}



/**
 * @brief Ottiene tutti i topics preferiti di un utente
 * @param conn Il gestore di connessione al database.
 * @param email L'email dell'utente.
 * @return char* La stringa con tutti gli argomenti preferiti separati da ",".
 */
char *get_topics(PGconn *conn, char *email)
{
    if (!conn || !email) {
        printf("[Get-Topics] Connessione al database o email non fornita.\n");
        return NULL;
    }

    char query[BUFFER_SIZE];
    char *favTopicsStr = NULL;

    // Costruisce la query SQL per estrarre i topics preferiti in base all'email fornita.
    snprintf(query, sizeof(query), "SELECT favtopics FROM users WHERE email='%s'", email); // snprintf previene overflow
    PGresult *res = PQexec(conn, query);

    // Controlla se l'esecuzione della query ha avuto successo.
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("[Get-Topics]L'esecuzione della query non è riuscita: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    // Verifica se PQgetvalue restituisce un valore valido.
    if (PQgetvalue(res, 0, 0) != NULL) {
        favTopicsStr = strdup(PQgetvalue(res, 0, 0));
    } else {
        printf("Nessun argomento trovato per l'email fornita.\n");
    }

    PQclear(res);
    return favTopicsStr;
}



// Funzione di chiusura dedicata
void cleanup_and_exit(int sockfd) {
    printf("Ricevuto SIGINT o terminazione del programma. Chiudo la socket...\n");
    close(sockfd);
    exit(0);
}

void sigint_handler(int signum)
{
    interrupted = 1;
}
