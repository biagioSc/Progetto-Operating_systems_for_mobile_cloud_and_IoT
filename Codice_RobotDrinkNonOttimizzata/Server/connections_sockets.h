#ifndef CONNECTIONS_H
#define CONNECTIONS_H


/**
 * @brief Connessione al database Postgre
 * @return PGconn* Il gestore della connessione al database
 */
PGconn *connect_to_db();

/**
 * @brief Legge i dati dalla socket.
 * @param sockfd Il socket file descriptor.
 * @param buffer Il buffer utilizzato per i dati da leggere.
 * @param bufsize La dimensione del buffer.
 * @return int Il numero di bytes letti, -1 in caso di errore.
 */
int read_from_socket(int sockfd, char *buffer, int bufsize);

/**
 * @brief Scrive i dati sulla socket e forza l'immediata trasmissione
 * @param sockfd Il socket file descriptor.
 * @param message I dati da inviare.
 * @return int Il numero di bytes scritti, -1 in caso di errore.
 */
int write_to_socket(int sockfd, const char *message);

#endif