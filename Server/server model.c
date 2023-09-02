#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include "libpq-fe.h"
#include <signal.h>

#define BUFFER_SIZE 256
#define MAX_TOPICS 100
#define PORT_NUMBER 8080




/**
 * @brief Struttura per mantenere le informazioni di sessione di un utente
 * 
 */
typedef struct User {
    int id;
    char email[BUFFER_SIZE];
    struct User* next;
} User;



User* usersList = NULL; // Variabile globale di riferimento per la lista
static int currentUserId = 0; // Contatore per il sessionID dell'utente




/**
 * @brief Rimuove un utente dalla struttura dati locale in base alla sua email 
 * @param email L'email dell'utente
 * @return void
 */
void removeUserByEmail(const char* email) {
    User *current = usersList;
    User *prev = NULL;

    while(current != NULL) {
        if(strcmp(current->email, email) == 0) {
            if(prev == NULL) {
                usersList = current->next;
            } else {
                prev->next = current->next;
            }
            free(current);
            return;
        }
        prev = current;
        current = current->next;
    }
}

/**
 * @brief Recupera un utente dalla struttura dati locale in base alla sua email 
 * @param email L'email dell'utente
 * @return User* L'utente nella struttura se questo esiste, Null altrimenti
 */
User* findUserByEmail(const char* email) {
    User* currentUser = usersList;
    while (currentUser) {
        if (strcmp(currentUser->email, email) == 0) {
            return currentUser;
        }
        currentUser = currentUser->next;
    }
    return NULL;
}


/**
 * @brief Aggiunge un utente alla struttura dati locale 
 * @param email L'email dell'utente
 * @return void
 */
void addUserToList(const char* email) {

    User* newUser = NULL;

    newUser = findUserByEmail(email);

    // Se l'utente non è presente lo aggiungo
    if(newUser == NULL){
        newUser = (User*) malloc(sizeof(User));
        newUser->id = ++currentUserId;   
        strcpy(newUser->email, email);
        newUser->next = usersList;
        usersList = newUser;
    }
}




/**
 * @brief Recupera l'id di sessione dell'utente in base alla sua email
 * @param email L'email dell'utente
 * @return int l'id di sessione dell'utente, -1 in caso di utente non trovato
 */
int getUserIdByEmail(const char* email) {
    User* currentUser = usersList;
    while (currentUser) {
        if (strcmp(currentUser->email, email) == 0) {
            return currentUser->id;
        }
        currentUser = currentUser->next;
    }

    return -1;
}


/**
 * @brief Recupera l'email di un utente in base al suo session id
 * @param userId Il session id dell'utente
 * @return char* L'email dell'utente, Null altrimenti
 */
const char* getEmailByUserId(int userId) {
    User* currentUser = usersList;
    while (currentUser) {
        if (currentUser->id == userId) {
            return currentUser->email;
        }
        currentUser = currentUser->next;
    }
    return NULL;
}





/**
 * @brief Rimuove un utente dalla struttura dati locale in base al suo session id
 * @param session_id Il session id dell'utente
 * @return void
 */
void removeUserFromList(int session_id) {
    User *current = usersList;
    User *prev = NULL;

    while(current != NULL) {
        if(current->id == session_id) {
            if(prev == NULL) {
                usersList = current->next;
            } else {
                prev->next = current->next;
            }
            free(current);
            return;
        }
        prev = current;
        current = current->next;
    }

}



/**
 * @brief Connessione al database Postgre
 * @return PGconn* Il gestore della connessione al database
 */
PGconn *connect_to_db()
{
    PGconn *conn = PQconnectdb("dbname=robotapp user=postgres password=WalterBalzano01! hostaddr=195.231.38.118 port=5432");
    if (PQstatus(conn) == CONNECTION_BAD)
    {
        fprintf(stderr, "Connessione al database fallita: %s\n", PQerrorMessage(conn));
        PQfinish(conn);
        return NULL;
    }
    return conn;
}

/**
 * @brief Legge i dati dalla socket.
 * @param sockfd Il socket file descriptor.
 * @param buffer Il buffer utilizzato per i dati da leggere.
 * @param bufsize La dimensione del buffer.
 * @return int Il numero di bytes letti, -1 in caso di errore.
 */
int read_from_socket(int sockfd, char *buffer, int bufsize)
{
    bzero(buffer, bufsize);
    int n = read(sockfd, buffer, bufsize - 1);

    if (n < 0)
    {
        perror("Errore nella lettura della socket\n");
        return -1;
    }

    // Remove newline character if present
    buffer[strcspn(buffer, "\n")] = '\0';
    return n;
}

/**
 * @brief Scrive i dati sulla socket e forza l'immediata trasmissione
 * @param sockfd Il socket file descriptor.
 * @param message I dati da inviare.
 * @return int Il numero di bytes scritti, -1 in caso di errore.
 */
int write_to_socket(int sockfd, const char *message)
{
    int message_length = strlen(message);
    char *full_message = malloc(message_length + 2); // Aggiungo spazio per \n e \0
    strcpy(full_message, message);
    strcat(full_message, "\n"); // Aggiungo \n

    int n = send(sockfd, full_message, message_length + 2, 0);
    free(full_message);

    if (n < 0)
    {
        perror("Errore nella scrittura della socket\n");
        return -1;
    }
    fflush(stdout); // Forzo l'invio immediato dei dati sulla socket
    return n;
}

/**
 * @brief Controlla quanti utenti si trovano in stato di waiting. 
 * @param conn Il gestore della connessione al database.
 * @return int Il numero di utenti nello stato di waiting.
 */
int check_state_waiting(PGconn *conn)
{
    char checkWaiting[BUFFER_SIZE];
    sprintf(checkWaiting, "SELECT COUNT(*) FROM users WHERE state='WAITING';");
    PGresult *resWaiting = PQexec(conn, checkWaiting);

    int countWaiting = atoi(PQgetvalue(resWaiting, 0, 0));

    PQclear(resWaiting);

    if(countWaiting >= 0)
        return countWaiting;
    return -1;
}
/**
 * @brief Controlla quanti utenti si trovano in stato di ordering.
 * @param conn Il gestore della connessione al database.
 * @return int Il numero di utenti nello stato di ordering.
 */
int check_state_ordering(PGconn *conn)
{
    char checkOrdering[BUFFER_SIZE];
    sprintf(checkOrdering, "SELECT COUNT(*) FROM users WHERE state='ORDERING';");
    PGresult *resOrdering = PQexec(conn, checkOrdering);

    int count = atoi(PQgetvalue(resOrdering, 0, 0));

    PQclear(resOrdering);

    return count;
}

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
 * @brief Controlla quanti utenti si trovano in stato di serving.
 * @param conn Il gestore della connessione al database.
 * @return int Il numero di utenti nello stato di serving.
 */
int check_state_serving(PGconn *conn){

    char checkServing[BUFFER_SIZE];
    sprintf(checkServing, "SELECT COUNT(*) FROM users WHERE state='SERVING';");
    PGresult *resServing = PQexec(conn, checkServing);

    int count = atoi(PQgetvalue(resServing, 0, 0));

    PQclear(resServing);

    return count;
}

/**
 * @brief Gestisce il processo di login di un utente.
 * @param sockfd Il socket descriptor utilizzato per comunicare con il client.
 * @param conn Il gestore della connessione con il database.
 * @param buffer Il buffer contenente il comando del client e le credenziali.
 * @return void
 */
void handle_login(int sockfd, PGconn *conn, char *buffer)
{
    // Buffer per l'email e la password
    char email[BUFFER_SIZE];
    char password[BUFFER_SIZE];

    // Estrae email e password dal buffer
    sscanf(buffer, "LOG_IN %s %s", email, password);

    // Crea una query SQL per ottenere la password dell'utente con l'email fornita
    char query[BUFFER_SIZE * 2];
    sprintf(query, "SELECT password FROM users WHERE email='%s';", email);

    // Esegue la query
    PGresult *resLogin = PQexec(conn, query);

    // Controlla l'esito della query
    if (PQresultStatus(resLogin) != PGRES_TUPLES_OK || PQntuples(resLogin) != 1)
    {
        printf("[Login] Non risulta nessun utente registrato con email:%s\n",email);
        PQclear(resLogin); // Libera la memoria del risultato
        write_to_socket(sockfd, "LOG_IN_ERROR"); // Informa il client dell'errore
        return;
    }

    // Controlla se la password fornita corrisponde a quella nel database
    if (strcmp(password, PQgetvalue(resLogin, 0, 0)) == 0)
    {
        printf("[Login] L'utente '%s' con password '%s' ha effettuato l'accesso correttamente\n", email,password);

        // Aggiungo l'utente alla lista di utenti online
        addUserToList(email);
        // Recupero l'id di sessione che gli è stato assegnato
        int id = getUserIdByEmail(email);

        // Se l'id è negativo, c'è stato un errore
        if(id < 0){
            printf("[Login] Non è stato possibile assegnare un id valido alla sessione dell'utente\n");
            return;
        }

        // Converte l'ID in una stringa
        char idString[BUFFER_SIZE];
        sprintf(idString,"%d",id);

        // Crea una query per ottenere il nome dell'utente
        sprintf(query,"SELECT name FROM users WHERE email='%s';",email);
        PGresult *resName = PQexec(conn,query);

        // Controlla l'esito della query per il nome
        if (PQresultStatus(resName) != PGRES_TUPLES_OK || PQntuples(resName) != 1)
        {
            printf("[Login] Nessun dato trovato\n");
            PQclear(resName); // Libera la memoria del risultato
            write_to_socket(sockfd, "LOG_IN_ERROR"); // Informa il client dell'errore
            return;
        }

        // Estrae il nome dall'esito della query
        char *name = PQgetvalue(resName, 0, 0);

        // Prepara la risposta da inviare al client nel formato: "MSG ID Name"
        char response[BUFFER_SIZE];
        sprintf(response, "LOG_IN_SUCCESS %s %s", idString, name);

        write_to_socket(sockfd, response); // Invia la risposta al client
        PQclear(resName);  // Libera la memoria del risultato
    }
    else
    {
        printf("[Login] Password errata per l'utente: %s \n", email);
        PQclear(resLogin); // Libera la memoria del risultato
        write_to_socket(sockfd, "LOG_IN_ERROR"); // Informa il client dell'errore
        return;
    }

    PQclear(resLogin); // Libera la memoria del risultato
}



/**
 * @brief Gestisce la procedura di welcome fornendo il numero di utenti totali tra waiting-ordering-serving.
 * @param sockfd Il socket file descriptor.
 * @param conn Il gestore della connessione con il database.
 * @return void
 */
void handle_welcoming(int sockfd, PGconn *conn)
{
    // Controllo utenti in ordering-waiting-serving
    int users_in_ordering_state = check_state_ordering(conn);
    int users_in_waiting_state = check_state_waiting(conn);
    int users_in_serving_state = check_state_serving(conn);
    int total_users = users_in_ordering_state + users_in_serving_state + users_in_waiting_state +1 -2;
    char users_in_ordering_state_string[10];

    printf("[Welcoming] Il numero di utenti in ordering+serving+waiting-2 e': %d\n",total_users);

    //trasformo il numero degli utenti in stringa
    sprintf(users_in_ordering_state_string,"%d",total_users);

    //mando il numero degli utenti al client
    write_to_socket(sockfd,users_in_ordering_state_string);
}

/**
 * @brief Gestisce la procedura di signup
 * @param sockfd Il socket file descriptor.
 * @param conn Il gestore della connessione con il database.
 * @return void
 */
void handle_signup(int sockfd, PGconn *conn)
{
    char email[BUFFER_SIZE], password[BUFFER_SIZE];
    char name[BUFFER_SIZE], surname[BUFFER_SIZE];
    char drinks[BUFFER_SIZE], topics[BUFFER_SIZE];
    char drinksCounterStr[BUFFER_SIZE], topicsCounterStr[BUFFER_SIZE];
    int signUpDone = 1;
    int drinksCounter, topicsCounter;

    printf("[SignUp] Il client ha avviato la fase di registrazione...\n");
    read_from_socket(sockfd, email, sizeof(email));

    // Controllo se esiste un utente già registrato con la stessa email
    if (check_email_already_used(conn, email) != 0)
    {
        printf("[SignUp] L'utente con email: %s e' gia' presente nel sistema\n", email);
        signUpDone = 0;
    }
    else
    {
        read_from_socket(sockfd, name, sizeof(name));
        read_from_socket(sockfd, surname, sizeof(surname));
        read_from_socket(sockfd, password, sizeof(password));
        read_from_socket(sockfd, drinksCounterStr, sizeof(drinksCounterStr));

        drinksCounter = atoi(drinksCounterStr);
        drinks[0] = '\0'; 

        // Recupero i drink inseriti dall'utente
        for (int i = 0; i < drinksCounter; i++)
        {
            char drink[BUFFER_SIZE];
            read_from_socket(sockfd, drink, sizeof(drink));
            strncat(drinks, drink, BUFFER_SIZE - strlen(drinks) - 1);

            if (i < drinksCounter - 1)
            {
                strncat(drinks, ",", BUFFER_SIZE - strlen(drinks) - 1);
            }
        }

        read_from_socket(sockfd, topicsCounterStr, sizeof(topicsCounterStr));
        topicsCounter = atoi(topicsCounterStr);
        topics[0] = '\0'; 

        // Recupero i topics inseriti dall'utente
        for (int i = 0; i < topicsCounter; i++)
        {
            char topic[BUFFER_SIZE];
            read_from_socket(sockfd, topic, sizeof(topic));
            strncat(topics, topic, BUFFER_SIZE - strlen(topics) - 1);

            if (i < topicsCounter - 1)
            {
                strncat(topics, ",", BUFFER_SIZE - strlen(topics) - 1);
            }
        }

        // Preparo la query per inserire i dati forniti
        char query[BUFFER_SIZE * 4];
        sprintf(query, "INSERT INTO users (name, surname, email, password, favdrink , favtopics, state) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', 'IDLE');",
                name, surname, email, password, drinks, topics);
        PGresult *res = PQexec(conn, query);

        // Controllo se la query è andata a buon fine
        if (PQresultStatus(res) != PGRES_COMMAND_OK)
        {
            signUpDone = 0;
            fprintf(stderr, "L'inserimento dei dati personali è fallito: %s", PQerrorMessage(conn));
            PQclear(res);
            PQfinish(conn);
            return;
        }
    }

    // Distinguo le fasi di successo e di errore
    if (signUpDone == 1)
    {
        write_to_socket(sockfd, "SIGN_UP_SUCCESS");
        printf("[SignUp] Registrazione per l'utente %s %s (email: %s | password: %s) andata a buon fine\n", name, surname, email, password);
    }
    else
    {
        printf("[SignUp] Registrazione non andata a buon fine\n");
        write_to_socket(sockfd, "SIGN_UP_ERROR");
    }
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
        printf("%s ", drinks[i]);
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


/**
 * @brief Gestisce la procedura di start-chat inviando i topics preferiti dell'utente.
 * @param sockfd Il socket file descriptor.
 * @param conn Il gestore della connessione con il database.
 * @return void
 */
void handle_chat(int sockfd, PGconn *conn)
{
    char buffer[BUFFER_SIZE];
    char session_id_str[BUFFER_SIZE];
    char message[BUFFER_SIZE];
    
    // Leggi il sessionId dell'utente dal socket.
    if(read_from_socket(sockfd,session_id_str, sizeof(session_id_str)) < 0){
        perror("Errore nella lettura del drink inviato dal client\n");
        return;
    }

    int session_id = atoi(session_id_str);

    // Controllo se il session id è valido
    if(session_id < 0){
        printf("[Chat] Ho ricevuto un session id non valido: %d\n");
        return;
    }
    
    // Ottieni l'email associata all'ID di sessione.
    char *email = getEmailByUserId(session_id);

    // Controllo se l'utente è stato trovato nella struttura degli utenti online
    if (email == NULL) {
        printf("[Chat] Non e' stato possibile trovare l'utente nella struttura degli utenti online\n");
        return;
    }
    
    // Ottieni la stringa dei topics.
    char *unique_string_topics = get_topics(conn,email);

    if (unique_string_topics == NULL) {
        printf("[Chat] Non e' stato possibile recuperare la stringa unica dei topics\n");
        perror("Errore durante il recupero dei topics");
        return;
    }
    
    printf("[Chat] I topics preferiti dell'utente %s sono: %s\n", email,unique_string_topics);

    // Invia la stringa dei topics al client.
    write_to_socket(sockfd, unique_string_topics);
}


/**
 * @brief Recupera la descrizione dei drink in base al nome.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void send_drink_description(PGconn *conn, int sockfd) {
    
    char query[BUFFER_SIZE];
    char drink_name[30];
    PGresult *res;
    char *description;

    // Ricevo il nome del drink dal client
    if (read_from_socket(sockfd, drink_name, sizeof(drink_name)) < 0) {
        perror("Errore nella lettura del drink inviato dal client\n");
        return;
    }

    printf("[Drink-Description] Richiesta descrizione per il drink: %s\n",drink_name);
 
    // Costruisco la query
    snprintf(query, sizeof(query), "SELECT description FROM drinks WHERE name='%s'", drink_name);

    // Eseguo la query
    res = PQexec(conn, query);

    // Verifico se la query ha avuto successo e se è stata restituita almeno una riga
    if (PQresultStatus(res) == PGRES_TUPLES_OK && PQntuples(res) > 0) {
        
        // Estraggo la descrizione dal risultato della query
        description = strdup(PQgetvalue(res, 0, 0));
        int writecode = write_to_socket(sockfd,description);
        
        // Invio la descrizione al client
        if (writecode < 0) {
            // Stampo un messaggio di errore in caso di problemi nell'invio
            perror("Errore nell'invio della descrizione del drink al client\n");
            free(description);
            return;
        }else{
            printf("[Drink-Description] Descrizione inoltrata con successo\n");
            // Libero la memoria allocata da strdup
            free(description);
        }
        
    } else {
        // Se non è stata trovata una descrizione, print di debug
        printf("[Drink-Description] Non e' stato possibile trovare la descrizione del drink: %s\n",drink_name);
    }

    PQclear(res);
}


/**
 * @brief Invia la lista dei drink al client.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void send_drinks_list(int sockfd, PGconn *conn) {
    // Ottieni la lista dei drink dal database
    char *drinks_string = get_drinks_name(conn);

    if (drinks_string != NULL) {

        printf("[Drinks-List] Inoltro lista dei drink: %s\n",drinks_string);

        // Invia la lista dei drink al client
        if (write_to_socket(sockfd, drinks_string) < 0) {
            perror("Errore nell'invio della lista dei drink al client\n");
        }

        // Libera la memoria della stringa dei drink
        free(drinks_string);
    }else{
        printf("[Drinks-List] Errore nella stringa unificata dei drink\n");
    }
    
}

/**
 * @brief Gestisce la procedura di suggerimento del drink in ordering.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_suggest_drink_ordering(int sockfd, PGconn *conn) {
    char session_id_str[BUFFER_SIZE];

    // Ricevo il sessionID dal client
    if(read_from_socket(sockfd, session_id_str, sizeof(session_id_str)) < 0) {
        perror("Errore nella lettura del sessionID del client\n");
        return;
    }

    // Trasformo il session id in un numero intero
    int session_id = atoi(session_id_str);

    // Controllo se il session id è valido o se atoi ha fallito
    if(session_id <= 0) {
        printf("[Sugg-Drink] Ho ricevuto un session id non valido: %d\n", session_id);
        return;
    }

    // Effettuo la ricerca dell'email dell'utente in base al suo session ID
    char *email = getEmailByUserId(session_id);

    // Controllo se è stata trovata l'email dell'utente nella struttura
    if(email == NULL) {
        printf("[Sugg-Drink] Non e' stato possibile trovare l'utente nella struttura degli utenti online\n");
        return;
    }

    // Prendo il drink da suggerire
    char *drink_to_suggest = suggest_drink(conn, email);
    if(drink_to_suggest) {

        // Invio il drink suggerito in base alle sue preferenze e controllo eventuali errori
        if(write_to_socket(sockfd, drink_to_suggest) < 0) {
            // Stampo un messaggio di errore in caso di problemi nell'invio
            perror("Errore nell'invio del drink suggerito al client\n");
        }

        free(drink_to_suggest); // Libera la memoria allocata in suggest_drink
    }
}


/**
 * @brief Gestisce la disconnessione di un client, rimuovendolo dalla lista degli utenti online.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_gone(int sockfd, PGconn *conn) {
    char user_session_id[BUFFER_SIZE];

    // Ricevi l'ID della sessione dal client
    if(read_from_socket(sockfd, user_session_id, sizeof(user_session_id)) < 0) {
        perror("Errore nella lettura dell'ID della sessione dal client\n");
        return;
    }

    // Converti l'ID della sessione in un numero intero
    int session_id = atoi(user_session_id);

    // Controllo se l'utente si è scollegato senza loggarsi
    if(session_id == -1){
        printf("[Gone] Utente non loggato disconnesso correttamente\n");
        return;
    }

    // Controllo se il session id è valido
    if(session_id < 0){
        printf("[Gone] Ho ricevuto un session id non valido: %d\n",session_id);
        return;
    }

    // Prendo l'email dell'utente in base al suo session id
    const char *email = getEmailByUserId(session_id);
    char email_buffer[BUFFER_SIZE];

   // Controllo se è stato trovato l'utente nella struttura
   if(email == NULL){
    printf("[Gone] Non e' stato possibile trovare l'utente nella struttura per gli utenti online\n");
    return;
   }else{
    strcpy(email_buffer,email);
   }

    // Trova e rimuovo l'utente dalla lista degli utenti connessi
    removeUserFromList(session_id);

    // Metto in IDLE lo stato dell'utente sul database
    char setIdle[BUFFER_SIZE];
    sprintf(setIdle, "UPDATE users SET state='IDLE' WHERE email='%s';", email_buffer);
    PQexec(conn, setIdle);
    printf("[Gone] L'utente %s è uscito dall'applicazione\n", email_buffer);
}


/**
 * @brief Aggiunge un utente nello stato di ordering sul database.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_add_user_ordering(int sockfd,PGconn *conn){

    char user_session_id[BUFFER_SIZE];

    // Ricevi l'ID della sessione dal client
    if(read_from_socket(sockfd, user_session_id, sizeof(user_session_id)) < 0) {
        perror("Errore nella lettura dell'ID della sessione dal client\n");
        return;
    }

    // Converti l'ID della sessione in un numero intero
    int session_id = atoi(user_session_id);

    // Controllo se il session id è valido
    if(session_id < 0){
        printf("[Add-Ordering] Ho ricevuto un session id non valido: %d\n",session_id);
        return;
    }

    char *email = getEmailByUserId(session_id);

    // Gestisco il caso in cui non viene trovato l'utente nella struttura
    if(email == NULL){
        printf("[Add-Ordering] Non e' stato possibile trovare l'utente nella struttura dati\n");
        return;
    }

    char setOrdering[BUFFER_SIZE];
    sprintf(setOrdering, "UPDATE users SET state='ORDERING' WHERE email='%s';", email);
    PQexec(conn, setOrdering);
    printf("[Add-Ordering] L'utente %s e' ora nello stato di ordering\n", email);

}

/**
 * @brief Aggiunge un utente nello stato di serving sul database.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @param email L'email dell'utente
 * @return void
 */
void handle_add_user_serving(int sockfd, PGconn *conn, char *email){

    char setServing[BUFFER_SIZE];
    sprintf(setServing, "UPDATE users SET state='SERVING' WHERE email='%s';", email);
    PQexec(conn, setServing);
    printf("[Add-Serving] L'utente %s e' ora nello stato di serving\n", email);
}


/**
 * @brief Aggiunge un utente nello stato di waiting sul database.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_add_user_waiting(int sockfd,PGconn *conn){
    char user_session_id[BUFFER_SIZE];

    // Ricevi l'ID della sessione dal client
    if(read_from_socket(sockfd, user_session_id, sizeof(user_session_id)) < 0) {
        perror("Errore nella lettura dell'ID della sessione dal client\n");
        return;
    }

    // Converti l'ID della sessione in un numero intero
    int session_id = atoi(user_session_id);

    // Controllo se il session id è valido
    if(session_id < 0){
        printf("[Add-Waiting] Ho ricevuto un session id non valido: %d\n",session_id);
        return;
    }

    char *email = getEmailByUserId(session_id);

    // Controllo se è stata trovata l'email dell'utente
    if(email == NULL){
        printf("[Add-Waiting] Non e' stato possibile recuperare l'utente nella struttura di utenti online\n");
        return;
    }

    char setWaiting[BUFFER_SIZE];
    sprintf(setWaiting, "UPDATE users SET state='WAITING' WHERE email='%s';", email);
    PQexec(conn, setWaiting);
    printf("[Add-Waiting] L'utente %s e' ora in stato di waiting\n", email);
}


/**
 * @brief Rimuove un utente dallo stato di Ordering sul database.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_user_stop_ordering(int sockfd, PGconn *conn){

    char user_session_id[BUFFER_SIZE];

    // Ricevi l'ID della sessione dal client
    if(read_from_socket(sockfd, user_session_id, sizeof(user_session_id)) < 0) {
        perror("Errore nella lettura dell'ID della sessione dal client\n");
        return;
    }

    // Converti l'ID della sessione in un numero intero
    int session_id = atoi(user_session_id);

    // Controllo se il session id è valido
    if(session_id < 0){
        printf("[Stop-Ordering] Ho ricevuto un session id non valido: %d\n",session_id);
        return;
    }

    char *email = getEmailByUserId(session_id);

    // Controllo se è stata trovata l'email dell'utente
    if(email == NULL){
        printf("[Stop-Ordering] Non e' stato possibile recuperare l'utente nella struttura di utenti online\n");
        return;
    }

    // Aggiungo l'utente direttamente in serving
    handle_add_user_serving(sockfd,conn,email);
}

/**
 * @brief Rimuove un utente dallo stato di serving sul database.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_user_stop_serving(int sockfd, PGconn *conn){

    char user_session_id[BUFFER_SIZE];

    // Ricevi l'ID della sessione dal client
    if(read_from_socket(sockfd, user_session_id, sizeof(user_session_id)) < 0) {
        perror("Errore nella lettura dell'ID della sessione dal client\n");
        return;
    }

    // Converti l'ID della sessione in un numero intero
    int session_id = atoi(user_session_id);

    // Controllo se il session id è valido
    if(session_id < 0){
        printf("[Stop-Serving] Ho ricevuto un session id non valido: %d\n",session_id);
        return;
    }

    char *email = getEmailByUserId(session_id);

    // Controllo se è stata trovata l'email dell'utente
    if(email == NULL){
        printf("[Stop-Serving] Non e' stato possibile recuperare l'utente nella struttura di utenti online\n");
        return;
    }

    char setIdle[BUFFER_SIZE];
    sprintf(setIdle, "UPDATE users SET state='IDLE' WHERE email='%s';", email);
    PQexec(conn, setIdle);
    printf("[Stop-Serving] L'utente %s ha concluso la fase di serving ed e' ora in idle\n", email);

}


/**
 * @brief Inivia al client il numero di utenti attualmente in waiting.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void send_users_waiting(int sockfd, PGconn *conn){
    int number_of_users_waiting = check_state_waiting(conn);
    char str_total_users[50];
    int number_of_users_ordering = check_state_ordering(conn);
    int number_of_users_serving = check_state_serving(conn);
    int total_users = number_of_users_ordering + number_of_users_waiting + number_of_users_serving-2;

    sprintf(str_total_users, "%d", total_users);
    printf("Invio total users - 2 che equivale a: %s\n",str_total_users);
    write_to_socket(sockfd,str_total_users);
}



/**
 * @brief Gestisce le richieste dei vari client che si collegano.
 * @param socket_desc Il socket file descriptor.
 * @return void
 */
void *client_handler(void *socket_desc)
{
    int sockfd = *(int *)socket_desc;
    free(socket_desc);
    // Modifico alcune opzioni della socket
    int optval = 1;
    if (setsockopt(sockfd, SOL_SOCKET, SO_RCVBUF, &optval, sizeof(optval)) < 0)
    {
        perror("Errore nel settare le opzioni della socket\n");
        return -1;
    }
    char buffer[BUFFER_SIZE];
    char email[BUFFER_SIZE];

    PGconn *conn = connect_to_db();
    if (!conn)
    {
        close(sockfd);
        return NULL;
    }
    printf("[Connessione] per il client %d avvenuta con successo\n", sockfd);
    while (1)
    {
        int n = read_from_socket(sockfd, buffer, BUFFER_SIZE);        
        if (n <= 0)
        {
            break;
        }


        if (strncmp(buffer, "LOG_IN", strlen("LOG_IN")) == 0)
        {
            handle_login(sockfd, conn, buffer);
        }
        else if (strcmp(buffer, "SIGN_UP") == 0)
        {
            handle_signup(sockfd, conn);
        }
        else if (strcmp(buffer, "START_CHAT") == 0)
        {
            handle_chat(sockfd, conn);
        }
        else if (strcmp(buffer, "CHECK_USERS_ORDERING") == 0)
        {
            handle_welcoming(sockfd, conn);
        }
        else if (strcmp(buffer,"ADD_USER_ORDERING") == 0){
            handle_add_user_ordering(sockfd,conn);
        }
        else if (strcmp(buffer,"ADD_USER_WAITING") == 0){
            handle_add_user_waiting(sockfd,conn);
        }
        else if (strcmp(buffer,"UPDATE_QUEUE") == 0){
            send_users_waiting(sockfd,conn);
        }
        else if (strcmp(buffer,"DRINK_DESCRIPTION") == 0){
            send_drink_description(conn,sockfd);
        }
        else if (strcmp(buffer,"SUGG_DRINK") == 0){
            handle_suggest_drink_ordering(sockfd,conn);
        }
        else if(strcmp(buffer,"USER_STOP_ORDERING") == 0){
            handle_user_stop_ordering(sockfd,conn);
        }
        else if(strcmp(buffer,"USER_STOP_SERVING") == 0){
            handle_user_stop_serving(sockfd,conn);
        }
        else if (strcmp(buffer,"DRINK_LIST") == 0){
            send_drinks_list(sockfd,conn);
        }
        else if (strcmp(buffer,"USER_GONE") == 0){
            handle_gone(sockfd,conn);
        }
        else
        {
            write_to_socket(sockfd, "UNKNOWN_COMMAND");
        }
    }

    PQfinish(conn);
    close(sockfd);
    return NULL;
}

volatile sig_atomic_t interrupted = 0;  // Flag per il segnale

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

int main() {
    srand(time(NULL)); // per random drink da suggerire
    int sockfd, newsockfd, portno;
    socklen_t clilen;
    struct sockaddr_in serv_addr, cli_addr;
    char *IP = "195.231.38.118"; // Indirizzo IP del server

    // Registra il gestore di segnali
    signal(SIGINT, sigint_handler);

    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
        perror("Errore nell'apertura della socket\n");
        exit(1);
    }

    // Aggiungo l'opzione SO_REUSEADDR
    int opt = 1;
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        perror("Errore nel settare SO_REUSEADDR sulla socket\n");
        exit(1);
    }

    // Aggiungo l'opzione SO_REUSEPORT
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEPORT, &opt, sizeof(opt)) < 0) {
        perror("Errore nel settare SO_REUSEPORT sulla socket\n");
        exit(1);
    }

    bzero((char *)&serv_addr, sizeof(serv_addr));
    portno = PORT_NUMBER;

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(portno);
    serv_addr.sin_addr.s_addr = inet_addr(IP);

    if (bind(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1) {
        perror("Errore nella fase di binding\n");
        exit(1);
    }

    listen(sockfd, 5);
    clilen = sizeof(cli_addr);
    printf("Server in ascolto sulla porta %d\n", portno);

    fd_set readfds;
    struct timeval timeout;

    while (!interrupted) {
        FD_ZERO(&readfds);
        FD_SET(sockfd, &readfds);
        
        timeout.tv_sec = 1;  // Controlla ogni secondo
        timeout.tv_usec = 0;

        int activity = select(sockfd + 1, &readfds, NULL, NULL, &timeout);

        if (activity < 0) {
            perror("\n[INTERRUPT] Il server è stato interrotto correttamente");
            exit(1);
        }

        if (activity == 0) {
            // Nessuna attività, controlla il flag e continua
            continue;
        }

        if (FD_ISSET(sockfd, &readfds)) {
            newsockfd = accept(sockfd, (struct sockaddr *)&cli_addr, &clilen);
            if (newsockfd < 0) {
                perror("Errore sulla accept della socket\n");
                exit(1);
            }

            pthread_t client_thread;
            int *new_sock_ptr = malloc(sizeof(int));
            *new_sock_ptr = newsockfd;
            if (pthread_create(&client_thread, NULL, client_handler, new_sock_ptr) < 0) {
                perror("Errore nella creazione del thread\n");
                return 1;
            }
        }
    }

    // Chiamata alla funzione di chiusura alla fine del main
    cleanup_and_exit(sockfd);
    return 0;
}
