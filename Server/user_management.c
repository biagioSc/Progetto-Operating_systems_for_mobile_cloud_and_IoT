
#include "imports.h"
#include "user_management.h"


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