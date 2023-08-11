

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
 * @brief Handles the login request.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 */
void handle_queue(int sockfd, PGconn *conn, char *email)
{
    int count = check_state(conn);
    if (count < 2)
    {
        char updateState[BUFFER_SIZE];
        sprintf(updateState, "UPDATE users SET state='ORDERING' WHERE email='%s';", email);    
        PGresult *resUpdateState = PQexec(conn, updateState);
        PQclear(resUpdateState);
        write_to_socket(sockfd, "WAIT_OVER");
    }
    else
    {
        char countOrderingString[BUFFER_SIZE];
        count = check_stateWaiting(conn);//Torno il numero di utenti in coda
        sprintf(countOrderingString, "%d", count);
        printf("Numero di utenti in coda che aspettando : %s\n", countOrderingString);
        write_to_socket(sockfd, countOrderingString);
    }
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

    if (PQresultStatus(resLogin) != PGRES_TUPLES_OK || PQntuples(resLogin) != 1)
    {
        printf("Nessun dato trovato\n");
        PQclear(resLogin);
        write_to_socket(sockfd, "LOG_IN_ERROR");
        // PQfinish(conn);
        // close(sockfd); Se chiudiamo la socket non diamo possibilità all utente di riprovare a loggarsi
        return -1;
    }
    if (strcmp(password, PQgetvalue(resLogin, 0, 0)) == 0)
    {
        printf("User %s logged in\n", email);
        write_to_socket(sockfd, "WELCOMING");
    }
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
int handle_welcoming(int sockfd, PGconn *conn, char *email)
{


    int users_in_ordering_state = check_state(conn);
    char users_in_ordering_state_string[10];

    //trasformo il numero degli utenti in stringa
    sprintf(users_in_ordering_state_string,"%d",users_in_ordering_state);
    //mando il numero degli utenti al client
    write_to_socket(sockfd,users_in_ordering_state_string);



    // Check number of users in ordering state:
    // If the number is < 2 we set the state of the user to "ORDERING" and send "ORDERING" to the client
    // Else we set the state of the user to "WAITING" and send "WAITING" to the client
    if (users_in_ordering_state < 2)
    {

        char setOrdering[BUFFER_SIZE];
        sprintf(setOrdering, "UPDATE users SET state='ORDERING' WHERE email='%s';", email);
        PQexec(conn, setOrdering);
        printf("User %s is ordering\n", email);
    }
    else if (users_in_ordering_state >= 2)
    {

        char setWaiting[BUFFER_SIZE];
        sprintf(setWaiting, "UPDATE users SET state='WAITING' WHERE email='%s';", email);
        PQexec(conn, setWaiting);
        printf("User %s is waiting\n", email);
    }
    else
    {
        return -1;
    }

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
            write_to_socket(sockfd, "SIGN_UP_SUCCESS");
        else
            write_to_socket(sockfd, "SIGN_UP_ERROR");
    }

    return 0; // Returns 0 if everything went well
}

/**
 * @brief Suggests a drink from the database list of drinks.
 * @param conn The database connection handle.
 * @return char* The suggested drink.
 */
char *suggest_drink(PGconn *conn)
{
    // Query to get the drink's list
    char query[BUFFER_SIZE];
    sprintf(query, "SELECT name FROM drinks;");
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        printf("Query execution failed: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    int num_drinks = PQntuples(res);

    // Randomly select a drink to suggest
    srand(time(NULL));
    int random_index = rand() % num_drinks;
    char *suggested_drink = PQgetvalue(res, random_index, 0);

    // Allocate memory and copy the suggested drink
    char *suggested_drink_copy = malloc(strlen(suggested_drink) + 1);
    strcpy(suggested_drink_copy, suggested_drink);

    PQclear(res);

    return suggested_drink_copy;
}

/**
 * @brief Combines an array of strings into a single comma-separated string.
 * 
 * @param drinks A NULL-terminated array of strings representing the drinks.
 * 
 * @return char* A newly dynamically allocated string containing all drink names
 *               separated by commas.
 *               Returns NULL on memory allocation errors.
 */
char *split_drinks(char **drinks){

    // Calcola la lunghezza totale necessaria
    int total_length = 0;
    for (int i = 0; drinks[i] != NULL; i++) {
        total_length += strlen(drinks[i]) + 1; // +1 per la virgola o il terminatore di stringa
    }

    char *result = malloc(total_length);
    if (!result) {
        perror("Memory allocation failed");
        return NULL;
    }

    result[0] = '\0'; // Inizia con una stringa vuota

    for (int i = 0; drinks[i] != NULL; i++) {
        strcat(result, drinks[i]);
        if (drinks[i + 1] != NULL) {
            strcat(result, ",");
        }
    }

    return result;
}

/**
 * @brief Retrieves all drink names from the database.
 * @param conn The database connection handle.
 * @return char* The splitted string of drinks.
 */
char *get_drinks_name(PGconn *conn)
{
    // Query to get the drink's list
    char query[BUFFER_SIZE];
    sprintf(query, "SELECT name FROM drinks;");
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        printf("Query execution failed: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    int num_drinks = PQntuples(res);
    
    // Allocate memory for drink names
    char **drinks = malloc(sizeof(char *) * (num_drinks + 1)); // +1 for a NULL terminator

    for (int i = 0; i < num_drinks; i++)
    {
        char *drink_name = PQgetvalue(res, i, 0);
        drinks[i] = strdup(drink_name); // Allocate and copy string
    }

    drinks[num_drinks] = NULL; // NULL-terminate the array

    PQclear(res);

    char *splitted_drinks = split_drinks(drinks);

    // Free the memory of drinks
    for (int i = 0; i < num_drinks; i++)
    {
        free(drinks[i]);
    }
    free(drinks);

    return splitted_drinks;
}


/**
 * @brief Gets all the topics from the database list of topics.
 * @param conn The database connection handle.
 * @return char * The string with all the topics separated by ",".
 */
char *get_topics(PGconn *conn)
{
    char query[BUFFER_SIZE];
    sprintf(query, "SELECT name FROM topics;");
    PGresult *res = PQexec(conn, query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK)
    {
        printf("Query execution failed: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    int num_topics = PQntuples(res);
    size_t total_length = 0;

    // Calcola la lunghezza totale necessaria per la stringa dei temi
    for (int i = 0; i < num_topics; i++)
    {
        total_length += strlen(PQgetvalue(res, i, 0));
    }

    // Calcola la lunghezza totale dei separatori
    size_t separators_length = 2 * (num_topics - 1);

    // Calcola la dimensione totale necessaria per la stringa finale
    size_t final_length = total_length + separators_length + 1;

    // Alloca memoria per la stringa finale
    char *topics = malloc(final_length);
    memset(topics, 0, final_length);

    // Costruisci la stringa finale dei temi
    for (int i = 0; i < num_topics; i++)
    {
        strcat(topics, PQgetvalue(res, i, 0));
        if (i < num_topics - 1)
        {
            strcat(topics, ", ");
        }
    }
    PQclear(res);

    return topics;
}

/**
 * @brief Splits a string of topics separated by ",".
 * @param topics The string of topics.
 * @return char** The matrix of the splitted topic's strings.
 */
char **split_topics(char *topics)
{

    char **result = malloc(MAX_TOPICS * sizeof(char *));
    int count = 0;

    char *token = strtok(topics, ", ");
    while (token != NULL && count < MAX_TOPICS)
    {
        result[count] = malloc(strlen(token) + 1); // +1 per il carattere di terminazione
        strcpy(result[count], token);
        count++;

        token = strtok(NULL, ", ");
    }

    result[count] = NULL; // segna la fine dell'array

    return result;
}

char *getTopicText(PGconn *conn, const char *topicName)
{
    char query[256];
    sprintf(query, "SELECT testo FROM topics WHERE name = '%s'", topicName);

    PGresult *result = PQexec(conn, query);

    if (PQresultStatus(result) != PGRES_TUPLES_OK)
    {
        fprintf(stderr, "Query fallita: %s\n", PQerrorMessage(conn));
        PQclear(result);
        exit(1);
    }

    int numRows = PQntuples(result);
    char *text = NULL;

    if (numRows > 0)
    {
        text = strdup(PQgetvalue(result, 0, 0));
    }

    PQclear(result);
    return text;
}




/**
 * @brief Manages user interaction for chat.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 * @param unique_string_topics String of topics for chat.
 * @return void
 */
void interacting(int sockfd, PGconn *conn, char *unique_string_topics) {
    char buffer[BUFFER_SIZE];
    char message[BUFFER_SIZE];
    int n = read_from_socket(sockfd, buffer, BUFFER_SIZE);

    if (n > 0) 
    {
        if (strcasecmp(buffer, "si") == 0) 
        {
            write_to_socket(sockfd, "Bene! Scegli l'argomento tra i seguenti:");
            write_to_socket(sockfd, unique_string_topics);
            free(unique_string_topics);
            n = read_from_socket(sockfd, buffer, BUFFER_SIZE);

            //prende il testo della domanda e lo scrive
            strcpy(buffer, getTopicText(conn, buffer));
            write_to_socket(sockfd, buffer);

            //seleziona dal database le risposte possibili e le invia.....


            //leggo la risposta dell'utente e la confronto con la risposta corretta
            int n = read_from_socket(sockfd, buffer, BUFFER_SIZE);

            if(n>0)
            {
                    //confronto la risposta


                    //se la risposta è corretta
                    write_to_socket(sockfd,"Risposta esatta, complimenti!");


                    //permettere nuove domande e continuare interacting
            }


        } else if (strcasecmp(buffer, "no") == 0) {
            write_to_socket(sockfd, "Va bene, il tuo ordine è in preparazione!");
            sleep(30);
            write_to_socket(sockfd, "Il tuo drink è pronto. Clicca su 'Ritirato' per confermare il ritiro.");
            int n = read_from_socket(sockfd,buffer,BUFFER_SIZE);
            if(n>0)
            {
                if(strcasecmp(buffer,"ritirato") == 0)
                {
                    write_to_socket(sockfd,"Grazie per aver ordinato da RoboDrink. Arrivederci!");
                }

                //altrimenti possibile out_of_sight
            }
        }
    }
}


/**
 * @brief Handles the chat start procedure.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 * @return int 0 if successful, or -1 if an error occurred.
 */
int handle_chat(int sockfd, PGconn *conn)
{
    char *suggested_drink = suggest_drink(conn);
    char *unique_string_topics = get_topics(conn);
    char buffer[BUFFER_SIZE];
    char message[BUFFER_SIZE];
    printf("Suggested drink: %s\n", suggested_drink);
    printf("Topics: %s\n", unique_string_topics);

    sprintf(message, "Benvenuto nella chat di RoboDrink! Ti andrebbe il drink: %s?", suggested_drink);
    write_to_socket(sockfd, message);
    int n = read_from_socket(sockfd, buffer, BUFFER_SIZE);

    if (n > 0)
    {
        if (strcasecmp(buffer, "si") == 0)
        {
            write_to_socket(sockfd, "Ottimo! Il tuo ordine è stato registrato. Ti andrebbe di chiacchierare?");
            interacting(sockfd,conn,unique_string_topics);
        }
        else if (strcasecmp(buffer, "no") == 0)
        {
            write_to_socket(sockfd, "Va bene, allora seleziona tra i drink disponibili.");
            
            //selezionare i drink disponibili ed inviarli al client
            char *available_drinks_string = get_drinks_name(conn);

            //controllo che la stringa non sia null
            if(available_drinks_string){
                write_to_socket(sockfd,available_drinks_string);
                free(available_drinks_string);
            }
            
            n = read_from_socket(sockfd, buffer, BUFFER_SIZE);
            write_to_socket(sockfd, "Ottimo! Il tuo ordine è stato registrato. Ti andrebbe di chiacchierare?");
            interacting(sockfd,conn,unique_string_topics);
        }

    }
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
        else if (strcmp(buffer, "CHECK_NEXT_STATE") == 0)
        {
            handle_welcoming(sockfd, conn, email);
        }
        else if (strcmp(buffer, "CHECK_WAITING") == 0)
        {
            handle_queue(sockfd, conn, email);
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
