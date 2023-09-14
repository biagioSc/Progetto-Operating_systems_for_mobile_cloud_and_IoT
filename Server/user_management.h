

/**
 * @brief Rimuove un utente dalla struttura dati locale in base alla sua email 
 * @param email L'email dell'utente
 * @return void
 */
void removeUserByEmail(const char* email);

/**
 * @brief Recupera un utente dalla struttura dati locale in base alla sua email 
 * @param email L'email dell'utente
 * @return User* L'utente nella struttura se questo esiste, Null altrimenti
 */
User* findUserByEmail(const char* email);


/**
 * @brief Aggiunge un utente alla struttura dati locale 
 * @param email L'email dell'utente
 * @return void
 */
void addUserToList(const char* email);


/**
 * @brief Recupera l'id di sessione dell'utente in base alla sua email
 * @param email L'email dell'utente
 * @return int l'id di sessione dell'utente, -1 in caso di utente non trovato
 */
int getUserIdByEmail(const char* email);


/**
 * @brief Recupera l'email di un utente in base al suo session id
 * @param userId Il session id dell'utente
 * @return char* L'email dell'utente, Null altrimenti
 */
const char* getEmailByUserId(int userId);


/**
 * @brief Rimuove un utente dalla struttura dati locale in base al suo session id
 * @param session_id Il session id dell'utente
 * @return void
 */
void removeUserFromList(int session_id);