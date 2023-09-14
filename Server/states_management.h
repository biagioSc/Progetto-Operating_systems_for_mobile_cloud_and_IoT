
/**
 * @brief Gestisce il processo di login di un utente.
 * @param sockfd Il socket descriptor utilizzato per comunicare con il client.
 * @param conn Il gestore della connessione con il database.
 * @param buffer Il buffer contenente il comando del client e le credenziali.
 * @return void
 */
void handle_login(int sockfd, PGconn *conn, char *buffer);


/**
 * @brief Gestisce la procedura di welcome fornendo il numero di utenti totali tra waiting-ordering-serving.
 * @param sockfd Il socket file descriptor.
 * @param conn Il gestore della connessione con il database.
 * @return void
 */
void handle_welcoming(int sockfd, PGconn *conn);

/**
 * @brief Gestisce la procedura di signup
 * @param sockfd Il socket file descriptor.
 * @param conn Il gestore della connessione con il database.
 * @return void
 */
void handle_signup(int sockfd, PGconn *conn);


/**
 * @brief Gestisce la procedura di start-chat inviando i topics preferiti dell'utente.
 * @param sockfd Il socket file descriptor.
 * @param conn Il gestore della connessione con il database.
 * @return void
 */
void handle_chat(int sockfd, PGconn *conn);


/**
 * @brief Recupera la descrizione dei drink in base al nome.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void send_drink_description(PGconn *conn, int sockfd);


/**
 * @brief Invia la lista dei drink al client.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void send_drinks_list(int sockfd, PGconn *conn);

/**
 * @brief Gestisce la procedura di suggerimento del drink in ordering.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_suggest_drink_ordering(int sockfd, PGconn *conn);


/**
 * @brief Gestisce la disconnessione di un client, rimuovendolo dalla lista degli utenti online.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_gone(int sockfd, PGconn *conn);


/**
 * @brief Aggiunge un utente nello stato di ordering sul database.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_add_user_ordering(int sockfd,PGconn *conn);

/**
 * @brief Aggiunge un utente nello stato di serving sul database.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @param email L'email dell'utente
 * @return void
 */
void handle_add_user_serving(int sockfd, PGconn *conn, char *email);


/**
 * @brief Aggiunge un utente nello stato di waiting sul database.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_add_user_waiting(int sockfd,PGconn *conn);


/**
 * @brief Rimuove un utente dallo stato di Ordering sul database.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_user_stop_ordering(int sockfd, PGconn *conn);

/**
 * @brief Rimuove un utente dallo stato di serving sul database.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void handle_user_stop_serving(int sockfd, PGconn *conn);


/**
 * @brief Inivia al client il numero di utenti attualmente in attesa.
 * @param conn Il gestore della connessione con il database.
 * @param sockfd Il socket file descriptor.
 * @return void
 */
void send_users_waiting(int sockfd, PGconn *conn);

/**
 * @brief Controlla quanti utenti si trovano in stato di waiting. 
 * @param conn Il gestore della connessione al database.
 * @return int Il numero di utenti nello stato di waiting.
 */
int check_state_waiting(PGconn *conn);

/**
 * @brief Controlla quanti utenti si trovano in stato di ordering.
 * @param conn Il gestore della connessione al database.
 * @return int Il numero di utenti nello stato di ordering.
 */
int check_state_ordering(PGconn *conn);

/**
 * @brief Controlla quanti utenti si trovano in stato di serving.
 * @param conn Il gestore della connessione al database.
 * @return int Il numero di utenti nello stato di serving.
 */
int check_state_serving(PGconn *conn);