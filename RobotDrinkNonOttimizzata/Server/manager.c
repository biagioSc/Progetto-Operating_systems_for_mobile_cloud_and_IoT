
#include "imports.h"
#include "manager.h"


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
