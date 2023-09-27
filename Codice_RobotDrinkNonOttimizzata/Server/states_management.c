
#include "imports.h"
#include "states_management.h"

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

    if(users_in_ordering_state+users_in_serving_state+users_in_waiting_state == 0)
    {
        sprintf(users_in_ordering_state_string,"%d",users_in_ordering_state+users_in_serving_state+users_in_waiting_state);
        write_to_socket(sockfd,users_in_ordering_state_string);
        printf("[Welcoming] Nessun utente in attesa, procedere con la fase di ordering.\n");
    }else{

        printf("[Welcoming] Il numero di utenti in attesa e': %d\n",total_users);

        //trasformo il numero degli utenti in stringa
        sprintf(users_in_ordering_state_string,"%d",total_users);

        //mando il numero degli utenti al client
        write_to_socket(sockfd,users_in_ordering_state_string);
    }

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
 * @brief Inivia al client il numero di utenti attualmente in attesa.
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
    printf("[Queue-Update] Utenti in attesa: %s\n",str_total_users);
    write_to_socket(sockfd,str_total_users);
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