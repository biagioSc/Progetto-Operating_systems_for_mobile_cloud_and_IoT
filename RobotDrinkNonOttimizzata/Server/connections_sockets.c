
#include "imports.h"
#include "connections_sockets.h"

/**
 * @brief Connessione al database Postgre
 * @return PGconn* Il gestore della connessione al database
 */
PGconn *connect_to_db()
{
    char conninfo[512];
    
    snprintf(conninfo, sizeof(conninfo), 
             "dbname=%s user=%s password=%s hostaddr=%s port=%s", 
             getenv("DB_NAME"), 
             getenv("DB_USER"), 
             getenv("DB_PASSWORD"), 
             getenv("DB_HOSTADDR"), 
             getenv("DB_PORT"));
             
    PGconn *conn = PQconnectdb(conninfo);
    if (PQstatus(conn) == CONNECTION_BAD)
    {
        fprintf(stderr, "Connessione al database fallita: %s\n", PQerrorMessage(conn));
        PQfinish(conn);
        return NULL;
    }
    return conn;
}

/**
 * @brief Legge i dati dalla socket.
 * @param sockfd Il socket file descriptor.
 * @param buffer Il buffer utilizzato per i dati da leggere.
 * @param bufsize La dimensione del buffer.
 * @return int Il numero di bytes letti, -1 in caso di errore.
 */
int read_from_socket(int sockfd, char *buffer, int bufsize)
{
    bzero(buffer, bufsize);
    int n = read(sockfd, buffer, bufsize - 1);

    if (n < 0)
    {
        perror("Errore nella lettura della socket\n");
        return -1;
    }

    // Remove newline character if present
    buffer[strcspn(buffer, "\n")] = '\0';
    printf("[Read] %s\n",buffer);
    return n;
}

/**
 * @brief Scrive i dati sulla socket e forza l'immediata trasmissione
 * @param sockfd Il socket file descriptor.
 * @param message I dati da inviare.
 * @return int Il numero di bytes scritti, -1 in caso di errore.
 */
int write_to_socket(int sockfd, const char *message)
{
    int message_length = strlen(message);
    char *full_message = malloc(message_length + 2); // Aggiungo spazio per \n e \0
    strcpy(full_message, message);
    strcat(full_message, "\n"); // Aggiungo \n

    int n = send(sockfd, full_message, message_length + 2, 0);
    free(full_message);

    if (n < 0)
    {
        perror("Errore nella scrittura della socket\n");
        return -1;
    }
    printf("[Send] %s\n",message);
    fflush(stdout); // Forzo l'invio immediato dei dati sulla socket
    return n;
}