

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
#include <signal.h>

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

static int currentUserId = 0; 



// Funzione per rimuovere un utente dalla lista in base all'email
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

void addUserToList(const char* email) {

    // Controllo se l'utente è già presente
    // Ciò può verificarsi in due situazioni:
    // 1. Viene effettuato il login dello stesso utente su altri dispositivi contemporaneamente
    // 2. E' stato premuto il pulsante stop su android studio e quindi il server non ha avuto modo
    //      di eliminare dalla lista degli utenti online quell'utente.

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





int getUserIdByEmail(const char* email) {
    User* currentUser = usersList;
    while (currentUser) {
        if (strcmp(currentUser->email, email) == 0) {
            return currentUser->id;
        }
        currentUser = currentUser->next;
    }


    // Ritorno -1 se non ho trovato l'id dell'utente
    return -1;
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





// Funzione per rimuovere un utente dalla lista in base al suo id
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

    printf("\n[Check-Waiting] Numero di utenti in attesa: %d",countWaiting);

    PQclear(resWaiting);

    if(countWaiting >= 0)
        return countWaiting;
    return -1;
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
 * @brief Gestisce il processo di login di un utente.
 * 
 * @param sockfd Il socket descriptor utilizzato per comunicare con il client.
 * @param conn Il connettore alla base di dati.
 * @param buffer Il buffer contenente il comando del client e le credenziali.
 * 
 * @return Ritorna 0 se il login ha successo, -1 altrimenti.
 */
int handle_login(int sockfd, PGconn *conn, char *buffer)
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
        printf("Nessun dato trovato\n");
        PQclear(resLogin); // Libera la memoria del risultato
        write_to_socket(sockfd, "LOG_IN_ERROR"); // Informa il client dell'errore
        return -1;
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
            return -1;
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
            printf("Nessun dato trovato\n");
            PQclear(resName); // Libera la memoria del risultato
            write_to_socket(sockfd, "LOG_IN_ERROR"); // Informa il client dell'errore
            return -1;
        }

        // Estrae il nome dall'esito della query
        char *name = PQgetvalue(resName, 0, 0);

        // Prepara la risposta da inviare al client nel formato: "MSG ID Name"
        char response[BUFFER_SIZE];
        sprintf(response, "LOG_IN_SUCCESS %s %s", idString, name);

        printf("Invio: %s\n", response);

        write_to_socket(sockfd, response); // Invia la risposta al client
        PQclear(resName);  // Libera la memoria del risultato
    }
    else
    {
        printf("[Login] Password errata per l'utente: %s \n", email);
        PQclear(resLogin); // Libera la memoria del risultato
        write_to_socket(sockfd, "LOG_IN_ERROR"); // Informa il client dell'errore
        return -1;
    }

    PQclear(resLogin); // Libera la memoria del risultato
    return 0; // Indica che la funzione è stata eseguita correttamente
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

    // Controllo utenti in ordering
    int users_in_ordering_state = check_state(conn);
    char users_in_ordering_state_string[10];

    printf("[Welcoming] Il numero di utenti in ordering e': %d\n",users_in_ordering_state);

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

    read_from_socket(sockfd, email, sizeof(email));

    if (check_user(conn, email) != 0)
    {
        printf("\n[SignUp] L'utente con email: %s e' gia' presente nel sistema\n", email);
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

        char query[BUFFER_SIZE * 4];
        sprintf(query, "INSERT INTO users (name, surname, email, password, favdrink , favtopics, state) VALUES ('%s', '%s', '%s', '%s', '%s', '%s', 'IDLE');",
                name, surname, email, password, drinks, topics);
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
            write_to_socket(sockfd, "SIGN_UP_SUCCESS");
            printf("\n[SignUp] Registrazione per l'utente %s %s (email: %s | password: %s) andata a buon fine\n", name, surname, email, password);
        }
        else
        {
            printf("\n[SignUp] Registrazione non andata a buon fine\n");
            write_to_socket(sockfd, "SIGN_UP_ERROR");
        }
    }

    return 0; // Returns 0 if everything went well
}


/**
 * @brief Suggests a drink from the database list of favourite_drinks of the user.
 * @param conn The database connection handle.
 * @return char* A random drink from the favourite_drinks_list of the user.
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
 * @brief Ottiene tutti i topics preferiti di un utente
 * @param conn Il gestore di connessione al database.
 * @param email L'email dell'utente.
 * @return char* La stringa con tutti gli argomenti preferiti separati da ",".
 */
char *get_topics(PGconn *conn, char *email)
{
    if (!conn || !email) {
        printf("\n[Get-Topics] Connessione al database o email non fornita.\n");
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
 * @brief Handles the chat start procedure.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 * @return int 0 if successful, or -1 if an error occurred.
 */
void handle_chat(int sockfd, PGconn *conn)
{
    char buffer[BUFFER_SIZE];
    char session_id_str[BUFFER_SIZE];
    char message[BUFFER_SIZE];
    
    // Leggi il sessionId dell'utente dal socket.

    if(read_from_socket(sockfd,session_id_str, sizeof(session_id_str)) < 0){
        perror("Errore nella lettura del drink inviato dal client");
        return;
    }

    int session_id = atoi(session_id_str);

    // Controllo se il session id è valido
    if(session_id < 0){
        printf("\n[Chat] Ho ricevuto un session id non valido: %d\n");
        return;
    }
    
    // Ottieni l'email associata all'ID di sessione.
    char *email = getEmailByUserId(session_id);

    // Controllo se l'utente è stato trovato nella struttura degli utenti online
    if (email == NULL) {
        printf("\n[Chat] Non e' stato possibile trovare l'utente nella struttura degli utenti online\n");
        return;
    }
    
    // Ottieni la stringa dei topics.
    char *unique_string_topics = get_topics(conn,email);

    if (unique_string_topics == NULL) {
        printf("\n[Chat] Non e' stato possibile recuperare la stringa unica dei topics\n");
        perror("Errore durante il recupero dei topics");
        return;
    }
    
    printf("[Chat] I topics preferiti dell'utente %s sono: %s\n", email,unique_string_topics);

    // Invia la stringa dei topics al client.
    write_to_socket(sockfd, unique_string_topics);
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
    char drink_name[30];
    PGresult *res;
    char *description;

    // Ricevo il nome del drink dal client
    if (read_from_socket(sockfd, drink_name, sizeof(drink_name)) < 0) {
        perror("Errore nella lettura del drink inviato dal client");
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
        
        // Invio la descrizione al client
        if (write_to_socket(sockfd, description) < 0) {
            // Stampo un messaggio di errore in caso di problemi nell'invio
            perror("Errore nell'invio della descrizione del drink al client");
        }

        printf("[Drink-Description] Descrizione inoltrata con successo");

        // Libero la memoria allocata da strdup
        free(description);
        
    } else {
        // Se non è stata trovata una descrizione, print di debug
        printf("[Drink-Description] Non e' stato possibile trovare la descrizione del drink: %s\n",drink_name);
    }

    PQclear(res);

    return;
}



void send_drinks_list(int sockfd, PGconn *conn) {
    // Ottieni la lista dei drink dal database
    char *drinks_string = get_drinks_name(conn);

    if (drinks_string != NULL) {

        printf("[Drinks-List] Inoltro lista dei drink: %s\n",drinks_string);

        // Invia la lista dei drink al client
        if (write_to_socket(sockfd, drinks_string) < 0) {
            perror("Errore nell'invio della lista dei drink al client");
        }

        // Libera la memoria della stringa dei drink
        free(drinks_string);
    }else{
        printf("[Drinks-List] Errore nella stringa unificata dei drink\n");
    }
    
}





void handle_suggest_drink_ordering(int sockfd, PGconn *conn) {
    char session_id_str[BUFFER_SIZE];

    // Ricevo il sessionID dal client
    if(read_from_socket(sockfd, session_id_str, sizeof(session_id_str)) < 0) {
        perror("Errore nella lettura del sessionID del client");
        return;
    }

    // Trasformo il session id in un numero intero
    int session_id = atoi(session_id_str);

    // Controllo se il session id è valido o se atoi ha fallito
    if(session_id <= 0) {
        printf("\n[Sugg-Drink] Ho ricevuto un session id non valido: %d\n", session_id);
        return;
    }

    // Effettuo la ricerca dell'email dell'utente in base al suo session ID
    char *email = getEmailByUserId(session_id);

    // Controllo se è stata trovata l'email dell'utente nella struttura
    if(email == NULL) {
        printf("\n[Sugg-Drink] Non e' stato possibile trovare l'utente nella struttura degli utenti online\n");
        return;
    }

    // Prendo il drink da suggerire
    char *drink_to_suggest = suggest_drink(conn, email);
    if(drink_to_suggest) {

        // Invio il drink suggerito in base alle sue preferenze e controllo eventuali errori
        if(write_to_socket(sockfd, drink_to_suggest) < 0) {
            // Stampo un messaggio di errore in caso di problemi nell'invio
            perror("Errore nell'invio del drink suggerito al client");
        }

        free(drink_to_suggest); // Libera la memoria allocata in suggest_drink
    }
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

    // Controllo se l'utente si è scollegato senza loggarsi
    if(session_id == -1){
        printf("\n[Gone] Utente non loggato disconnesso correttamente");
    }

    // Controllo se il session id è valido
    if(session_id < 0){
        printf("\n[Gone] Ho ricevuto un session id non valido: %d\n",session_id);
        return;
    }


    // Prendo l'email dell'utente in base al suo session id
    const char *email = getEmailByUserId(session_id);
    char email_buffer[BUFFER_SIZE];


   // Controllo se è stato trovato l'utente nella struttura
   if(email == NULL){
    printf("\n[Gone] Non e' stato possibile trovare l'utente nella struttura per gli utenti online\n");
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


void handle_add_user_waiting(int sockfd,PGconn *conn){
    char user_session_id[BUFFER_SIZE];

    // Ricevi l'ID della sessione dal client
    if(read_from_socket(sockfd, user_session_id, sizeof(user_session_id)) < 0) {
        perror("Errore nella lettura dell'ID della sessione dal client");
        return;
    }


    // Converti l'ID della sessione in un numero intero
    int session_id = atoi(user_session_id);

    // Controllo se il session id è valido
    if(session_id < 0){
        printf("\n[Add-Waiting] Ho ricevuto un session id non valido: %d\n",session_id);
        return;
    }

    char *email = getEmailByUserId(session_id);


    // Controllo se è stata trovata l'email dell'utente
    if(email == NULL){
        printf("\n[Add-Waiting] Non e' stato possibile recuperare l'utente nella struttura di utenti online\n");
        return;
    }

    char setWaiting[BUFFER_SIZE];
    sprintf(setWaiting, "UPDATE users SET state='WAITING' WHERE email='%s';", email);
    PQexec(conn, setWaiting);
    printf("\n[Add-Waiting] L'utente %s e' ora in stato di waiting\n", email);
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

    char setIdle[BUFFER_SIZE];
    sprintf(setIdle, "UPDATE users SET state='IDLE' WHERE email='%s';", email);
    PQexec(conn, setIdle);
    printf("[Stop-Ordering] L'utente %s ha concluso la fase di ordering ed e' ora in idle\n", email);
}


void send_users_waiting(int sockfd, PGconn *conn){
    int number_of_users_waiting = check_stateWaiting(conn);
    char str_total_users[50];
    int number_of_users_ordering = check_state(conn);
    int total_users = number_of_users_ordering + number_of_users_waiting - 2;


    sprintf(str_total_users, "%d", total_users);
    printf("\nInvio total users - 2 che equivale a: %s\n",str_total_users);
    write_to_socket(sockfd,str_total_users);



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
            printf(" ERRORE NELLA LETTURA DELLA SOCKET\n");
            break;
        }

        // Usa strncmp() per vedere se il buffer inizia con "LOG_IN"
        if (strncmp(buffer, "LOG_IN", strlen("LOG_IN")) == 0)
        {
            handle_login(sockfd, conn, buffer);
        }
        else if (strcmp(buffer, "SIGN_UP") == 0)
        {
            printf("[SignUp] Il client ha avviato la fase di registrazione...\n");
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



volatile sig_atomic_t interrupted = 0;  // Flag per il segnale

// Funzione di chiusura dedicata
void cleanup_and_exit(int sockfd) {
    printf("\nRicevuto SIGINT o terminazione del programma. Chiudo la socket...\n");
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
        perror("Error opening socket");
        exit(1);
    }

    // Add SO_REUSEADDR option
    int opt = 1;
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        perror("Error setting SO_REUSEADDR on socket");
        exit(1);
    }

    // Set SO_REUSEPORT
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEPORT, &opt, sizeof(opt)) < 0) {
        perror("Error setting SO_REUSEPORT on socket");
        exit(1);
    }

    bzero((char *)&serv_addr, sizeof(serv_addr));
    portno = PORT_NUMBER;

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(portno);
    serv_addr.sin_addr.s_addr = inet_addr(IP);

    if (bind(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1) {
        perror("Error on binding");
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
                perror("Error on accept");
                exit(1);
            }

            pthread_t client_thread;
            int *new_sock_ptr = malloc(sizeof(int));
            *new_sock_ptr = newsockfd;
            if (pthread_create(&client_thread, NULL, client_handler, new_sock_ptr) < 0) {
                perror("Error creating thread");
                return 1;
            }
            // pthread_detach(client_thread); // Detach thread to prevent memory leaks
        }
    }

    // Chiamata alla funzione di chiusura alla fine del main
    cleanup_and_exit(sockfd);
    
    // Questa riga non sarà mai raggiunta a causa della chiamata a exit() nella funzione cleanup_and_exit
    return 0;  
}
