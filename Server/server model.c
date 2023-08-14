

/*
    [INFO] Codice attualmente gestito in un file unico per questioni di praticità.

    Il server è utilizzato per la comunicazione con un client Android in java e deve occuparsi di:

    1. Ricevere un messaggio che può essere: "LOG_IN" oppure "SIGN_UP" oppure "START_CHAT" oppure "CHECK_NEXT_STATE" (da definirne altri o modificarli se necessario)
    2. Se il messaggio è "LOG_IN" dovrà ricevere un email e una password.
        2.i     A questo punto dovrà collegarsi ad un database Postgresql e verificare se i dati sono corretti (il database si trova sulla stessa macchina server)
        2.ii    Se i dati sono corretti invia un messaggio di "WELCOMING"
    3. Se il messaggio è "CHECK_NEXT_STATE" , il server invia "ORDERING" se non ci sono altri utenti che si trovano in quello stato, altrimenti "WAITING" se ci sono altri utenti che si trovano in quello stato. (max 2 utenti in ordering, verificabile dal db nella entry "state")
        3.i Se il messaggio CHECK_WAITING il server controlla il numero di utenti in coda e lo invia al client
            Se il numero in coda è <2 il server invia "WAIT_OVER" è passa alla fase di "ORDERING"
    4. Se il messaggio è "SIGN_UP" il server dovrà ricevere: nome, cognome, email, password, un arraylist di stringhe di drink preferiti e un arraylist di stringhe di argomenti preferiti.
        3.i     Il server avvierà una connessione con il database per registrare il nuovo utente con i dati forniti. Se tutto è andato a buon fine restituisce il messaggio al client "SIGN_UP_SUCCESS" altrimenti "SIGN_UP_ERROR"
        3.ii    Se l'utente prova a registrarsi con una email già presente nel db il messaggio impostato di errore è "EMAIL_ALREADY_USED"
    5. Se il messaggio è "START_CHAT" si inizia la comunicazione, con il primo messaggio del server che dà il benvenuto e suggerisce il drink [INCOMPLETO O MANCANTE]
    6. Nella fase di waiting il server dovrà recuperare il numero di utenti nello stato di ordering in real-time e inviarlo al client che potrà gestire la coda di attesa. [INCOMPLETO O MANCANTE]

    [INFO] Il codice non è stato testato ed è in continuo aggiornamento
*/

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

#define BUFFER_SIZE 256
#define MAX_TOPICS 100
#define PORT_NUMBER 8080



// Struttura Lista utilizzata per suggerire drinks e topics
// in maniera appropriata per ogni utente e per mantenere la sessione
typedef struct User {
    int id;
    char email[BUFFER_SIZE];
    struct User* next;
} User;


// Variabile globale di riferimento
User* usersList = NULL;

int currentUserId = 0; 




void addUserToList(const char* email) {
    User* newUser = (User*) malloc(sizeof(User));
    newUser->id = ++currentUserId;   
    strcpy(newUser->email, email);
    newUser->next = usersList;
    usersList = newUser;
}


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



int getUserIdByEmail(const char* email) {
    User* currentUser = usersList;
    while (currentUser) {
        if (strcmp(currentUser->email, email) == 0) {
            return currentUser->id;
        }
        currentUser = currentUser->next;
    }
    return -1;  // Restituisce -1 se l'utente con l'email fornita non viene trovato
}


const char* getEmailByUserId(int userId) {
    User* currentUser = usersList;
    while (currentUser) {
        if (currentUser->id == userId) {
            return currentUser->email;
        }
        currentUser = currentUser->next;
    }
    return NULL;  // Restituisce NULL se non viene trovato un utente con l'ID fornito
}


// Funzione per rimuovere un utente dalla lista
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
 * @brief Connects to the PostgreSQL database.
 * @return PGconn* The database connection handle.
 */
PGconn *connect_to_db()
{
    PGconn *conn = PQconnectdb("dbname=robotapp user=postgres password=WalterBalzano01! hostaddr=195.231.38.118 port=5432");
    if (PQstatus(conn) == CONNECTION_BAD)
    {
        fprintf(stderr, "Connection to database failed: %s\n", PQerrorMessage(conn));
        PQfinish(conn);
        return NULL;
    }
    return conn;
}

/**
 * @brief Reads data from the socket.
 * @param sockfd The socket file descriptor.
 * @param buffer The buffer to read data into.
 * @param bufsize The size of the buffer.
 * @return int The number of bytes read, or -1 if an error occurred.
 */
int read_from_socket(int sockfd, char *buffer, int bufsize)
{
    bzero(buffer, bufsize);
    int n = read(sockfd, buffer, bufsize - 1);

    if (n < 0)
    {
        perror("Error reading from socket");
        return -1;
    }

    // Remove newline character if present
    buffer[strcspn(buffer, "\n")] = '\0';
    printf("Received: %s\n", buffer);
    return n;
}

/**
 * @brief Writes data to the socket and forces immediate data transmission.
 * @param sockfd The socket file descriptor.
 * @param message The data to write.
 * @return int The number of bytes written, or -1 if an error occurred.
 *
 *
 *
 */
int write_to_socket(int sockfd, const char *message)
{
    int message_length = strlen(message);
    char *full_message = malloc(message_length + 2); // Aggiungi spazio per \n e \0
    strcpy(full_message, message);
    strcat(full_message, "\n"); // Aggiungi \n

    int n = send(sockfd, full_message, message_length + 2, 0);
    free(full_message);

    if (n < 0)
    {
        perror("Error sending data to Android");
        return -1;
    }
    fflush(stdout); // Forza l'invio immediato dei dati sulla socket
    return n;
}

/**
 * @brief Check ho many users are in the state of waiting. 
 * @param conn The database connection handle.
 * @return int The number of users in the state of waiting.
 */
int check_stateWaiting(PGconn *conn)
{
    char checkWaiting[BUFFER_SIZE];
    sprintf(checkWaiting, "SELECT COUNT(*) FROM users WHERE state='WAITING';");
    PGresult *resWaiting = PQexec(conn, checkWaiting);

    int countWaiting = atoi(PQgetvalue(resWaiting, 0, 0));

    PQclear(resWaiting);

    return countWaiting;
}
/**
 * @brief Checks how many users are in the state of ordering.
 * @param conn The database connection handle.
 * @return int The number of users in the state of ordering.
 */
int check_state(PGconn *conn)
{

    char checkOrdering[BUFFER_SIZE];
    sprintf(checkOrdering, "SELECT COUNT(*) FROM users WHERE state='ORDERING';");
    PGresult *resOrdering = PQexec(conn, checkOrdering);

    int count = atoi(PQgetvalue(resOrdering, 0, 0));

    PQclear(resOrdering);

    return count;
}

/**
 * @brief Checks if there is another user registered with the same email.
 * @param conn The database connection handle.
 * @param email The email to check.
 * @return int The number of users that are registered with the same email.
 */
int check_user(PGconn *conn, char *email)
{

    // Check if the email already exists
    char checkEmailQuery[BUFFER_SIZE * 2];
    sprintf(checkEmailQuery, "SELECT COUNT(*) FROM users WHERE email='%s';", email);
    PGresult *resCheckEmail = PQexec(conn, checkEmailQuery);
    int counterEmailCheck = atoi(PQgetvalue(resCheckEmail, 0, 0));

    PQclear(resCheckEmail);

    return counterEmailCheck;
}

int handle_login(int sockfd, PGconn *conn, char *email)
{
    // Reads the email and password from the client
    char password[BUFFER_SIZE];
    read_from_socket(sockfd, email, BUFFER_SIZE);
    read_from_socket(sockfd, password, BUFFER_SIZE);

    printf("Email e password: %s - %s\n", email, password);
    // Verify the credentials with the database.
    char query[BUFFER_SIZE * 2];
    sprintf(query, "SELECT password FROM users WHERE email='%s';", email);
    PGresult *resLogin = PQexec(conn, query);


    // Nessun dato trovato
    if (PQresultStatus(resLogin) != PGRES_TUPLES_OK || PQntuples(resLogin) != 1)
    {
        printf("Nessun dato trovato\n");
        PQclear(resLogin);
        write_to_socket(sockfd, "LOG_IN_ERROR");
        // PQfinish(conn);
        // close(sockfd); Se chiudiamo la socket non diamo possibilità all utente di riprovare a loggarsi
        return -1;
    }


    // Utente loggato correttamente
    if (strcmp(password, PQgetvalue(resLogin, 0, 0)) == 0)
    {
        printf("User %s logged in\n", email);
        write_to_socket(sockfd, "LOG_IN_SUCCESS");

        // Aggiungo l'utente alla lista di sessione e gli assegno l'id
        addUserToList(email);

        // Recupero il valore dell'ID assegnato
        int id = getUserIdByEmail(email);

        // Invio l'id al client dopo averlo trasformato in stringa
        char idString[BUFFER_SIZE];
        sprintf(idString,"%d",id);
        write_to_socket(sockfd,idString);

        // Il client provvederà a portare avanti l'id tramite le Intent
        // fino alla chiusura dell'applicazione
    }
    // Errore nel Login: password non valida
    else
    {
        printf("Password non valida per questa email %s \n", email);
        PQclear(resLogin);
        write_to_socket(sockfd, "LOG_IN_ERROR");
        // PQfinish(conn);
        // close(sockfd); Se chiudiamo la socket non diamo possibilità all utente di riprovare a loggarsi
        return -1;
    }

    PQclear(resLogin);
    return 0;
}

/**
 * @brief Handles the welcoming procedure.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 * @param email The email of the logged user to update his state in the db.
 * @return 0 if the check state went well, -1 otherwise.
 */
int handle_welcoming(int sockfd, PGconn *conn)
{


    int users_in_ordering_state = check_state(conn);
    char users_in_ordering_state_string[10];

    //trasformo il numero degli utenti in stringa
    sprintf(users_in_ordering_state_string,"%d",users_in_ordering_state);
    //mando il numero degli utenti al client
    write_to_socket(sockfd,users_in_ordering_state_string);

    return 0;
}

/**
 * @brief Handles the signup procedure.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 * @return int 0 if successful, or -1 if an error occurred.
 */
int handle_signup(int sockfd, PGconn *conn)
{
    char email[BUFFER_SIZE], password[BUFFER_SIZE];
    char name[BUFFER_SIZE], surname[BUFFER_SIZE];
    char drinks[BUFFER_SIZE], topics[BUFFER_SIZE];
    char drinksCounterStr[BUFFER_SIZE], topicsCounterStr[BUFFER_SIZE];
    int signUpDone = 1;
    int drinksCounter, topicsCounter;

    read_from_socket(sockfd, email, BUFFER_SIZE);

    if (check_user(conn, email) != 0)
    {
        write_to_socket(sockfd, "EMAIL_ALREADY_USED");
    }
    else
    {
        //ricevo il nome dell'utente
        read_from_socket(sockfd, name, BUFFER_SIZE);
        printf("Name: %s\n", name);

        //ricevo il cognome dell'utente
        read_from_socket(sockfd, surname, BUFFER_SIZE);
        printf("Surname: %s\n", surname);

        //ricevo la password e il numero di drink preferiti
        read_from_socket(sockfd, password, BUFFER_SIZE);
        read_from_socket(sockfd, drinksCounterStr, BUFFER_SIZE);

        //trasformo il numero dei drink da stringa ad intero
        drinksCounter = atoi(drinksCounterStr);
        printf("Drinks counter: %d\n", drinksCounter);

        //terminazione per la stringa di drinks
        drinks[0] = '\0'; 


        //ricevo i drink e li separo con una virgola
        for (int i = 0; i < drinksCounter; i++)
        {
            char drink[BUFFER_SIZE];
            read_from_socket(sockfd, drink, BUFFER_SIZE);
            strcat(drinks, drink);

            // Add separator between drinks
            if (i < drinksCounter - 1)
            {
                strcat(drinks, ",");
            }
        }


        //ricevo il numero di argomenti preferiti
        read_from_socket(sockfd, topicsCounterStr, BUFFER_SIZE);
        topicsCounter = atoi(topicsCounterStr);

        // Unite topics into a single string
        topics[0] = '\0'; // Initialize the topics string


        //ricevo gli argomenti e li separo con una virgola
        for (int i = 0; i < topicsCounter; i++)
        {
            char topic[BUFFER_SIZE];
            read_from_socket(sockfd, topic, BUFFER_SIZE);
            strcat(topics, topic);

            // Add separator between topics
            if (i < topicsCounter - 1)
            {
                strcat(topics, ",");
            }
        }

        // name, surname, email, password, favDrinks, favTopics INSERTION
        char query[BUFFER_SIZE * 4];
        sprintf(query, "INSERT INTO users (name, surname, email, password, \"favDrink\" , \"favTopics\", state) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', 'IDLE');",
                name, surname, email, password, drinks, topics);
        printf("%s\n", query);
        PGresult *res = PQexec(conn, query);

        if (PQresultStatus(res) != PGRES_COMMAND_OK)
        {
            signUpDone = 0;
            fprintf(stderr, "Insert personal datas failed: %s", PQerrorMessage(conn));
            PQclear(res);
            PQfinish(conn);
            return 1;
        }

        if (signUpDone == 1)
        {
            // Invio messaggio di successo al client
            write_to_socket(sockfd, "SIGN_UP_SUCCESS");
        }
        else
            write_to_socket(sockfd, "SIGN_UP_ERROR");
    }

    return 0; // Returns 0 if everything went well
}

/**
 * @brief Suggests a drink from the database list of favourite_drinks of the user.
 * @param conn The database connection handle.
 * @return char* A random drink from the favourite_drinks_list of the user.
 */
char *suggest_drink(PGconn *conn, const char *email) {
    char query[BUFFER_SIZE];

    // Suggerimento del drink basato sullep preferenze inserite dall'utente durante l'interview
    sprintf(query, "SELECT favDrink FROM users WHERE email='%s'", email); 
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("Query execution failed: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    // Estraggo la lista dei drink preferiti
    char *favDrinksStr = PQgetvalue(res, 0, 0);
    char *drinks[15];
    int numDrinks = 0;

    // Suddivido la stringa in singoli drink usando strtok (la suddivisione avviene tramite parametro ',')
    char *token = strtok(favDrinksStr, ",");
    while (token) {
        drinks[numDrinks++] = token;
        token = strtok(NULL, ",");
    }

    // Seleziono un drink casuale da suggerire all'utente
    if (numDrinks > 0) {
        int randomIndex = rand() % numDrinks;
        char *suggestedDrink = strdup(drinks[randomIndex]);
        PQclear(res);
        return suggestedDrink;
    } else {
        PQclear(res);
        return NULL;
    }
}


/**
 * Unisce le stringhe dei drink separandole con virgole.
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
        perror("Errore nell'allocazione della memoria");
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
 * Estrae tutti i nomi dei drink dal database.
 * @param conn Connessione al database.
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
 * @brief Gets all the topics from the database list of topics.
 * @param conn The database connection handle.
 * @return char * The string with all the topics separated by ",".
 */
char *get_topics(PGconn *conn, char *email)
{
    char query[BUFFER_SIZE];

    // Costruisce la query SQL per estrarre i topics preferiti in base all'email fornita.
    sprintf(query, "SELECT favTopics FROM users WHERE email='%s'", email); 
    PGresult *res = PQexec(conn, query);

    // Controlla se l'esecuzione della query ha avuto successo.
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("L'esecuzione della query non è riuscita: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    // Estrae e ritorna direttamente la stringa dei topics preferiti.
    char *favTopicsStr = strdup(PQgetvalue(res, 0, 0));

    PQclear(res);
    return favTopicsStr;
}

/**
 * @brief Handles the chat start procedure.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 * @return int 0 if successful, or -1 if an error occurred.
 */
void handle_chat(int sockfd, PGconn *conn)
{
    char buffer[BUFFER_SIZE];
    char message[BUFFER_SIZE];
    
    // Leggi il sessionId dell'utente dal socket.
    char *userSessionId = read_from_socket(sockfd, buffer, BUFFER_SIZE);
    if (!userSessionId) {
        perror("Errore durante la lettura del sessionId");
        return;
    }
    
    // Ottieni l'email associata all'ID di sessione.
    char *email = getEmailByUserId(userSessionId);
    if (!email) {
        perror("Errore durante la ricerca dell'email");
        return;
    }
    
    // Ottieni la stringa dei topics.
    char *unique_string_topics = get_topics(conn,email);
    if (!unique_string_topics) {
        perror("Errore durante il recupero dei topics");
        return;
    }
    
    printf("Topics: %s\n", unique_string_topics);

    // Invia la stringa dei topics al client.
    if (!write_to_socket(sockfd, unique_string_topics)) {
        perror("Errore durante l'invio dei topics");
    }
}


/**
 * @brief Retrieves the description of a drink from the database based on the drink's name sent from the client.
 * 
 * @param conn A pointer to the PostgreSQL connection object.
 * @param sockfd The socket file descriptor used to communicate with the client.
 * 
 * @return void
 */
void send_drink_description(PGconn *conn, int sockfd) {
    
    char query[BUFFER_SIZE];
    char drink_name[10];
    PGresult *res;
    char *description = NULL;

    // Ricevo il nome del drink dal client
    if (read_from_socket(sockfd, drink_name, sizeof(drink_name)) < 0) {
        perror("Errore nella lettura del drink inviato dal client");
        return;
    }

    // Controllo che la stringa inviatami non contenga caratteri particolari
    for (int i = 0; drink_name[i]; i++) {
        if (!isalnum(drink_name[i])) {
            perror("Carattere non valido nel nome del drink");
            return;
        }
    }

    // Costruisco la query
    snprintf(query, sizeof(query), "SELECT description FROM drinks WHERE name='%s'", drink_name);

    // Eseguo la query
    res = PQexec(conn, query);

    // Verifico se la query ha avuto successo e se è stata restituita almeno una riga
    if (PQresultStatus(res) == PGRES_TUPLES_OK && PQntuples(res) > 0) {
        
        // Estraggo la descrizione dal risultato della query
        description = strdup(PQgetvalue(res, 0, 0));
        
        // Invio la descrizione al client
        if (write_to_socket(sockfd, description) < 0) {
            // Stampo un messaggio di errore in caso di problemi nell'invio
            perror("Errore nell'invio della descrizione del drink al client");
        }
    } else {
        // Se non è stata trovata una descrizione, invio un messaggio di "non trovato" al client
        write_to_socket(sockfd, "DRINK_DESCRIPTION_NOT_FOUND");
    }


    PQclear(res);

    return;
}


void send_drinks_list(int sockfd, PGconn *conn) {
    // Ottieni la lista dei drink dal database
    char *drinks_string = get_drinks_name(conn);

    if (drinks_string != NULL) {
        // Invia la lista dei drink al client
        if (write_to_socket(sockfd, drinks_string) < 0) {
            perror("Errore nell'invio della lista dei drink al client");
        }

        // Libera la memoria della stringa dei drink
        free(drinks_string);
    } else {
        // Gestisci l'errore se non è possibile ottenere la lista dei drink
        const char *error_msg = "ERROR_DRINKS_LIST";
        if (write_to_socket(sockfd, error_msg) < 0) {
            perror("Errore nell'invio del messaggio di errore al client");
        }
    }
}





void handle_ordering(int sockfd, PGconn *conn){

    
    char user_session_id[10];

    // Ricevo il sessionID dal client
    if(read_from_socket(sockfd,user_session_id,sizeof(user_session_id) < 0)){
        perror("Errore nella lettura del sessionID del client");
        return;
    }

    // Effettuo la ricerca dell'email dell'utente in base al suo session ID
    char *email = getEmailByUserId(user_session_id);

    // Prendo il drink da suggerire
    char *drink_to_suggest = suggest_drink(conn,email);

    // Invio il drink suggerito in base alle sue preferenze e controllo eventuali errori
    if(write_to_socket(sockfd,drink_to_suggest) < 0){
         // Stampo un messaggio di errore in caso di problemi nell'invio
        perror("Errore nell'invio del drink suggerito al client");
    }

    return;
}


void handle_gone(int sockfd, PGconn *conn) {
    char user_session_id[BUFFER_SIZE];

    // Ricevi l'ID della sessione dal client
    if(read_from_socket(sockfd, user_session_id, sizeof(user_session_id)) < 0) {
        perror("Errore nella lettura dell'ID della sessione dal client");
        return;
    }

    // Converti l'ID della sessione in un numero intero
    int session_id = atoi(user_session_id);

    // Trova e rimuovi l'utente dalla lista degli utenti connessi
    removeUserFromList(session_id);


    // Setto ad IDLE lo stato dell'utente
    char * email = getEmailByUserId(session_id);

    
    char setIdle[BUFFER_SIZE];
    sprintf(setIdle, "UPDATE users SET state='IDLE' WHERE email='%s';", email);
    PQexec(conn, setIdle);
    printf("User %s is now in idle\n", email);


    // Invia un messaggio di conferma al client
     write_to_socket(sockfd, "USER_REMOVED");
}


void handle_add_user_ordering(int sockfd,PGconn *conn){

    char user_session_id[BUFFER_SIZE];

    // Ricevi l'ID della sessione dal client
    if(read_from_socket(sockfd, user_session_id, sizeof(user_session_id)) < 0) {
        perror("Errore nella lettura dell'ID della sessione dal client");
        return;
    }

    // Converti l'ID della sessione in un numero intero
    int session_id = atoi(user_session_id);

    char *email = getEmailByUserId(session_id);


    char setOrdering[BUFFER_SIZE];
    sprintf(setOrdering, "UPDATE users SET state='ORDERING' WHERE email='%s';", email);
    PQexec(conn, setOrdering);
    printf("User %s is now in ordering state\n", email);

}


void handle_add_user_waiting(int sockfd,PGconn *conn){
    char user_session_id[BUFFER_SIZE];

    // Ricevi l'ID della sessione dal client
    if(read_from_socket(sockfd, user_session_id, sizeof(user_session_id)) < 0) {
        perror("Errore nella lettura dell'ID della sessione dal client");
        return;
    }

    // Converti l'ID della sessione in un numero intero
    int session_id = atoi(user_session_id);

    char *email = getEmailByUserId(session_id);

    char setWaiting[BUFFER_SIZE];
    sprintf(setWaiting, "UPDATE users SET state='WAITING' WHERE email='%s';", email);
    PQexec(conn, setWaiting);
    printf("User %s is now in waiting state\n", email);
}


void handle_user_stop_ordering(int sockfd, PGconn *conn){

    char user_session_id[BUFFER_SIZE];

    // Ricevi l'ID della sessione dal client
    if(read_from_socket(sockfd, user_session_id, sizeof(user_session_id)) < 0) {
        perror("Errore nella lettura dell'ID della sessione dal client");
        return;
    }

    // Converti l'ID della sessione in un numero intero
    int session_id = atoi(user_session_id);

    char *email = getEmailByUserId(session_id);

    char setIdle[BUFFER_SIZE];
    sprintf(setIdle, "UPDATE users SET state='IDLE' WHERE email='%s';", email);
    PQexec(conn, setIdle);
    printf("User %s now stopped ordering state and is in idle\n", email);
}


/**
 * @brief Handles client requests.
 * @param socket_desc The socket file descriptor.
 */
void *client_handler(void *socket_desc)
{
    int sockfd = *(int *)socket_desc;
    free(socket_desc);
    // Modifico alcune opzioni della socket
    int optval = 1;
    if (setsockopt(sockfd, SOL_SOCKET, SO_RCVBUF, &optval, sizeof(optval)) < 0)
    {
        perror("Error setting socket option");
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
            printf("ERRORE NELLA LETTURA DELLA SOCKET\n");
            break;
        }

        if (strcmp(buffer, "LOG_IN") == 0)
        {
            handle_login(sockfd, conn, email);
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
        else if (strcmp(buffer,"DRINK_DESCRIPTION") == 0){
            send_drink_description(conn,sockfd);
        }
        else if (strcmp(buffer,"ORDERING") == 0){
            handle_ordering(sockfd,conn);
        }
        else if( strcmp(buffer,"USER_STOP_ORDERING") == 0){
            handle_user_stop_ordering(sockfd,conn);
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

int main()
{
    int sockfd, newsockfd, portno;
    socklen_t clilen;
    struct sockaddr_in serv_addr, cli_addr;
    int n;
    char *IP = "195.231.38.118"; // Indirizzo IP del server

    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) == -1)
    {
        perror("Error opening socket");
        exit(1);
    }

    bzero((char *)&serv_addr, sizeof(serv_addr));
    portno = PORT_NUMBER;

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(portno);
    serv_addr.sin_addr.s_addr = inet_addr(IP);

    if (bind(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1)
    {
        perror("Error on binding");
        exit(1);
    }

    listen(sockfd, 5);
    clilen = sizeof(cli_addr);
    printf("Server in ascolto sulla porta %d\n", portno);
    while (1)
    {
        newsockfd = accept(sockfd, (struct sockaddr *)&cli_addr, &clilen);
        if (newsockfd < 0)
        {
            perror("Error on accept");
            exit(1);
        }

        pthread_t client_thread;
        int *new_sock_ptr = malloc(sizeof(int));
        *new_sock_ptr = newsockfd;
        if (pthread_create(&client_thread, NULL, client_handler, new_sock_ptr) < 0)
        {
            perror("Error creating thread");
            return 1;
        }
        // pthread_detach(client_thread); // Detach thread to prevent memory leaks
    }

    close(sockfd);
    return 0;
}
