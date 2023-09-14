

/**
 * @brief Controlla se esiste un utente sul database registrato con la stessa email.
 * @param conn Il gestore della connessione al database.
 * @param email L'email dell'utente da verificare.
 * @return int Il numero di utenti che hanno l'email inserita.
 */
int check_email_already_used(PGconn *conn, char *email);

/**
 * @brief Suggerisce un drink tra i preferiti dell'utente
 * @param conn Il gestore della connessione con il database.
 * @return char* Un drink randomico tra la lista dei drink preferiti.
 */
char *suggest_drink(PGconn *conn, const char *email);

/**
 * @brief Unisce le stringhe dei drink separandole con virgole.
 * @param drinks Array di stringhe contenenti i nomi dei drink.
 * @return char* Stringa con i nomi dei drink separati da virgole.
 */
char *join_drinks(char **drinks);

/**
 * @brief Estrae tutti i nomi dei drink dal database.
 * @param conn Il gestore della connessione al database.
 * @return char* Stringa con i nomi dei drink separati da virgole.
 */
char *get_drinks_name(PGconn *conn);

/**
 * @brief Ottiene tutti i topics preferiti di un utente
 * @param conn Il gestore di connessione al database.
 * @param email L'email dell'utente.
 * @return char* La stringa con tutti gli argomenti preferiti separati da ",".
 */
char *get_topics(PGconn *conn, char *email);

void cleanup_and_exit(int sockfd);

void sigint_handler(int signum);
