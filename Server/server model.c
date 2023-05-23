


/*
    [INFO] Codice attualmente gestito in un file unico per questioni di praticità.

    Il server è utilizzato per la comunicazione con un client Android in java e deve occuparsi di:

    1. Ricevere un messaggio che può essere: "LOG_IN" oppure "SIGN_UP" oppure "START_CHAT" oppure "CHECK_NEXT_STATE" (da definirne altri o modificarli se necessario)
    2. Se il messaggio è "LOG_IN" dovrà ricevere un email e una password.
        2.i     A questo punto dovrà collegarsi ad un database Postgresql e verificare se i dati sono corretti (il database si trova sulla stessa macchina server)
        2.ii    Se i dati sono corretti invia un messaggio di "WELCOMING"
    3. Se il messaggio è "CHECK_NEXT_STATE" , il server invia "ORDERING" se non ci sono altri utenti che si trovano in quello stato, altrimenti "WAITING" se ci sono altri utenti che si trovano in quello stato. (max 2 utenti in ordering, verificabile dal db nella entry "state")
    4. Se il messaggio è "SIGN_UP" il server dovrà ricevere: nome, cognome, email, password, un arraylist di stringhe di drink preferiti e un arraylist di stringhe di argomenti preferiti. 
        3.i     Il server avvierà una connessione con il database per registrare il nuovo utente con i dati forniti. Se tutto è andato a buon fine restituisce il messaggio al client "SIGN_UP_SUCCESS" altrimenti "SIGN_UP_ERROR"
        3.ii    Se l'utente prova a registrarsi con una email già presente nel db il messaggio impostato di errore è "EMAIL_ALREADY_USED"
    5. Se il messaggio è "START_CHAT" si inizia la comunicazione, con il primo messaggio del server che dà il benvenuto e suggerisce il drink [INCOMPLETO O MANCANTE]
    6. Nella fase di waiting il server dovrà recuperare il numero di utenti nello stato di ordering in real-time e inviarlo al client che potrà gestire la coda di attesa. [INCOMPLETO O MANCANTE]

    [INFO] Il codice non è stato testato ed è in continuo aggiornamento
*/



#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <libpq-fe.h>

#define BUFFER_SIZE 256
#define MAX_TOPICS 100





/**
 * @brief Connects to the PostgreSQL database.
 * @return PGconn* The database connection handle.
 */
PGconn *connect_to_db() {
    PGconn *conn = PQconnectdb("dbname=Robot-Database user=postgres password=postgres hostaddr=195.231.38.118 port=5432");
    if (PQstatus(conn) == CONNECTION_BAD) {
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
int read_from_socket(int sockfd, char *buffer, int bufsize) {
    bzero(buffer, bufsize);
    int n = read(sockfd, buffer, bufsize - 1);
    if (n < 0) {
        perror("Error reading from socket");
        return -1;
    }
    return n;
}



/**
 * @brief Writes data to the socket.
 * @param sockfd The socket file descriptor.
 * @param buffer The data to write.
 * @return int The number of bytes written, or -1 if an error occurred.
 */
int write_to_socket(int sockfd, const char *buffer) {
    int n = write(sockfd, buffer, strlen(buffer));
    if (n < 0) {
        perror("Error writing to socket");
        return -1;
    }
    return n;
}



/**
 * @brief Checks how many users are in the state of ordering.
 * @param conn The database connection handle.
 * @return int The number of users in the state of ordering.
*/
int check_state(PGconn *conn){

    char checkOrdering[BUFFER_SIZE];
    sprintf(checkOrdering, "SELECT COUNT(*) FROM users WHERE state='ORDERING';");
    PGresult *resOrdering = PQexec(conn, checkOrdering);

    int countOrdering = atoi(PQgetvalue(resOrdering,0,0));

    PQclear(resOrdering);

    return countOrdering;
}

/**
 * @brief Checks if there is another user registered with the same email.
 * @param conn The database connection handle.
 * @param email The email to check.
 * @return int The number of users that are registered with the same email.
*/
int check_user(PGconn *conn, char *email){

    // Check if the email already exists
    char checkEmailQuery[BUFFER_SIZE * 2];
    sprintf(checkEmailQuery, "SELECT COUNT(*) FROM users WHERE email='%s';", email);
    PGresult *resCheckEmail = PQexec(conn, checkEmailQuery);
    int counterEmailCheck = atoi(PQgetvalue(resCheckEmail,0,0));

    PQclear(resCheckEmail);

    return counterEmailCheck;
}

/**
 * @brief Handles the login procedure.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 * @return char* The email of the user logged in for welcoming state.
 */
char * handle_login(int sockfd, PGconn *conn) {

    // Reads the email and password from the client
    char email[BUFFER_SIZE], password[BUFFER_SIZE];
    read_from_socket(sockfd, email, BUFFER_SIZE);
    read_from_socket(sockfd, password, BUFFER_SIZE);

    // Verify the credentials with the database.

    
    char query[BUFFER_SIZE * 2];
    sprintf(query, "SELECT password FROM users WHERE email='%s';", email);
    PGresult *resLogin = PQexec(conn, query);

    if (PQresultStatus(resLogin) != PGRES_TUPLES_OK || PQntuples(resLogin) != 1) {
        printf("No data retrieved\n");        
        PQclear(resLogin);
        PQfinish(conn);
        close(sockfd);
        return NULL;
    }  

   
    if (strcmp(password, PQgetvalue(resLogin, 0, 0)) != 0) {
        printf("Invalid password\n");
        PQclear(resLogin);
        PQfinish(conn);
        write_to_socket(sockfd,"LOG_IN_ERROR");
        close(sockfd);
        return NULL;
    }

    PQclear(resLogin);

    write_to_socket(sockfd,"WELCOMING");


    return email;
}

/**
 * @brief Handles the welcoming procedure.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 * @param email The email of the logged user to update his state in the db.
 * @return 0 if the check state went well, -1 otherwise.
*/
int handle_welcoming(int sockfd, PGConn *conn, char *email){

    // Check number of users in ordering state:
    // If the number is < 2 we set the state of the user to "ORDERING" and send "ORDERING" to the client
    // Else we set the state of the user to "WAITING" and send "WAITING" to the client
    if(check_state(conn) < 2){

        char setOrdering[BUFFER_SIZE];
        sprintf(setOrdering, "UPDATE users SET state='ORDERING' WHERE email='%s';", email);
        PQexec(conn, setOrdering);
        write_to_socket(sockfd,"ORDERING");

    }else if(check_state(conn) >= 2){

        char setWaiting[BUFFER_SIZE];
        sprintf(setWaiting, "UPDATE users SET state='WAITING' WHERE email='%s';", email);
        PQexec(conn, setWaiting);
        write_to_socket(sockfd,"WAITING");
    }else 
        return -1;

    return 0;
}



/**
 * @brief Handles the signup procedure.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 * @return int 0 if successful, or -1 if an error occurred.
 */
int handle_signup(int sockfd, PGconn *conn) {
    
    char email[BUFFER_SIZE], password[BUFFER_SIZE];
    char name[BUFFER_SIZE], surname[BUFFER_SIZE];
    char drinks[BUFFER_SIZE], char topics[BUFFER_SIZE];
    char drinksCounterStr[BUFFER_SIZE], char topicsCounterStr[BUFFER_SIZE];
    int signUpDone = 1;
    int drinksCounter, topicsCounter;


    read_from_socket(sockfd, email, BUFFER_SIZE);

    if(check_user(conn,email) != 0){
        write_to_socket(sockfd,"EMAIL_ALREADY_USED");
    }else{
        read_from_socket(sockfd,name,BUFFER_SIZE);
        read_from_socket(sockfd,surname,BUFFER_SIZE);
        read_from_socket(sockfd,password,BUFFER_SIZE);
        read_from_socket(sockfd,drinksCounterStr,BUFFER_SIZE);
        drinksCounter = atoi(drinksCounterStr);

        for(int i = 0; i < drinksCounter; i++){
            read_from_socket(sockfd,drinks[i],BUFFER_SIZE);
        }

        read_from_socket(sockfd,topicsCounterString,BUFFER_SIZE);
        topicsCounter = atoi(topicsCounterString);

        for(int i = 0; i < topicsCounter; i++){
            read_from_socket(sockfd,topics[i],BUFFER_SIZE);
        }

        // name, surname, email, password INSERTION

        char query[BUFFER_SIZE * 4];
        sprintf(query, sizeof(query),"INSERT INTO users (name, surname, email, password) VALUES ('%s', '%s', '%s', '%s');", name, surname, email, password);
        PGresult *res = PQexec(conn, query);

        if (PQresultStatus(res) != PGRES_COMMAND_OK) 
        {
            signUpDone = 0;
            fprintf(stderr,"Insert personal datas failed: %s",PQerrorMessage(conn));
            PQclear(res);
            PQfinish(conn);
            return NULL;
        } 

        // favourite_drinks INSERTION

        for(int i = 0; i < drinksCounter; i++){

            snprintf(query,sizeof(query),"INSERT INTO users (favourite_drinks) VALUES ('%s');",drinks[i]);
            res = PQexec(conn,query);

            if(PQresultStatus(res) != PGRES_COMMAND_OK){
                signUpDone = 0;
                fprintf(stderr, "Insert drink preference failed: %s",PQerrorMessage(conn));
                PQclear(res);
                PQfinish(conn);
                return NULL;
            }

            PQclear(res);
        }

        // favourite_topics INSERTION

        for(int i = 0; i < topicsCounter; i++){
            snprintf(query,sizeof(query),"INSERT INTO users (favourite_topics) VALUES ('%s');",topics[i]);
            res = PQexec(conn,query);

            if(PQresultStatus(res) != PGRES_COMMAND_OK){
                signUpDone = 0;
                fprintf(stderr, "Insert topic preference failed: %s",PQerrorMessage(conn));
                PQclear(res);
                PQfinish(conn);
                return NULL;
            }

            PQclear(res);
        }


        if(signUpDone == 1)
            write_to_socket(sockfd,"SIGN_UP_SUCCESS");
        else 
            write_to_socket(sockfd,"SIGN_UP_ERROR");

    }
    
    return 0; // Returns 0 if everything went well
}




/**
 * @brief Suggests a drink from the database list of drinks.
 * @param conn The database connection handle.
 * @return char* The suggested drink.
 */
char* suggest_drink(PGconn *conn) {
    // Query to get the drink's list
    char query[BUFFER_SIZE];
    sprintf(query, "SELECT name FROM drinks;");
    PGresult *res = PQexec(conn, query);
    
    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("Query execution failed: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }
    
    int num_drinks = PQntuples(res);

    // Randomly select a drink to suggest
    srand(time(NULL));
    int random_index = rand() % num_drinks;
    char* suggested_drink = PQgetvalue(res, random_index, 0);
    
    PQclear(res);

    return suggested_drink;
}

/**
 * @brief Gets all the topics from the database list of topics.
 * @param conn The database connection handle.
 * @return char * The string with all the topics separated by ",".
*/
char *get_topics(PGconn *conn){

    char query[BUFFER_SIZE];
    sprintf(query,"SELECT name FROM topics;");
    PGresult *res = PQexec(conn,query);

    if (PQresultStatus(res) != PGRES_TUPLES_OK) {
        printf("Query execution failed: %s\n", PQerrorMessage(conn));
        PQclear(res);
        return NULL;
    }

    int num_topics = PQntuples(res);
    char *topics = (char *)malloc(MAX_TOPICS);
    memset(topics,0,MAX_TOPICS);

    for(int i = 0; i < num_topics; i++){
        strncat(topics, PQgetvalue(res,i,0), MAX_TOPICS - strlen(topics) - 1);
        if( i < num_topics - 1){
            strncat(topics, ", ", MAX_TOPICS - strlen(topics) - 1);
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
char** split_topics(char* topics) {

    char** result = malloc(MAX_TOPICS * sizeof(char*));
    int count = 0;

    char *token = strtok(topics, ", ");
    while(token != NULL && count < MAX_TOPICS) {
        result[count] = malloc(strlen(token) + 1); // +1 per il carattere di terminazione
        strcpy(result[count], token);
        count++;

        token = strtok(NULL, ", ");
    }

    result[count] = NULL; // segna la fine dell'array

    return result;
}



/**
 * @brief Handles the chat start procedure.
 * @param sockfd The socket file descriptor.
 * @param conn The database connection handle.
 * @return int 0 if successful, or -1 if an error occurred.
 */
int handle_chat(int sockfd, PGconn *conn) {

    char *suggested_drink = suggest_drink(conn);
    char *unique_string_topics = get_topics(conn);
    char **splitted_topics = split_topics(unique_string_topics);
    char buffer[BUFFER_SIZE];
    
    write_to_socket(sockfd,"Benvenuto nella chat di RoboDrink! Ti andrebbe il drink: %s?",suggest_drink);
    int n = read_from_socket(sockfd,buffer,BUFFER_SIZE);

    if(n > 0){
        if(strcasecmp(buffer,"si") == 0 || strcasecmp(buffer,"ok") == 0 || strcasecmp(buffer,"va bene") == 0){

            write_to_socket(sockfd,"Ottimo! Il tuo drink è in preparazione. Ti andrebbe di chiacchierare?");
            n = read_from_socket(sockfd,buffer,BUFFER_SIZE);

            if(n > 0){
                
                if(strcasecmp(buffer,"si") == 0 || strcasecmp(buffer,"ok") == 0 || strcasecmp(buffer,"va bene") == 0){
                    
                    write_to_socket(sockfd,"Bene! Scegli l'argomento tra i seguenti: %s",unique_string_topics);
                    free(unique_string_topics);

                    //prendo la risposta, verifico che la risposta è tra gli argomenti splitted_topics e continuo...

                    
                }
            }

        } else if(strcasecmp(buffer,"no") == 0){


        } else{
            write_to_socket(sockfd,"CHAT_MESSAGE_NOT_VALID");
        }
    }

}



/**
 * @brief Handles client requests.
 * @param socket_desc The socket file descriptor.
 */
void* client_handler(void *socket_desc) {
    int sockfd = *(int*)socket_desc;
    free(socket_desc);

    char buffer[BUFFER_SIZE];
    char email[BUFFER_SIZE];

    PGconn *conn = connect_to_db();
    if (!conn) {
        close(sockfd);
        return NULL;
    }

    while (1) {
        int n = read_from_socket(sockfd, buffer, BUFFER_SIZE);
        if (n <= 0) {
            break;
        }

        if (strcmp(buffer, "LOG_IN") == 0) {
            email = handle_login(sockfd, conn);
        } else if (strcmp(buffer, "SIGN_UP") == 0) {
            handle_signup(sockfd, conn);
        } else if(strcmp(buffer,"START_CHAT") == 0){
            handle_chat(sockfd,conn);
        } else if(strcmp(buffer,"CHECK_NEXT_STATE") == 0){
            handle_welcoming(sockfd, conn, email);
        } else {
            write_to_socket(sockfd, "UNKNOWN_COMMAND");
        }
    }

    PQfinish(conn);
    close(sockfd);
    return NULL;
}




int main() {
    int sockfd, newsockfd, portno;
    socklen_t clilen;
    struct sockaddr_in serv_addr, cli_addr;
    int n;

    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
        perror("Error opening socket");
        exit(1);
    }

    bzero((char *) &serv_addr, sizeof(serv_addr));
    portno = PORT_NUMBER;

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_addr.s_addr = INADDR_ANY;
    serv_addr.sin_port = htons(portno);

    if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
        perror("Error on binding");
        exit(1);
    }

    listen(sockfd, 5);
    clilen = sizeof(cli_addr);

    while (1) {
        newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
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
        pthread_detach(client_thread);  // Detach thread to prevent memory leaks
    }

    close(sockfd);
    return 0;
}



















