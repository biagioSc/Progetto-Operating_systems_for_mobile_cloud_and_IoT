
#include "imports.h"
#include "auxiliary.h"
#include "states_management.h"
#include "connections_sockets.h"
#include "user_management.h"
#include "manager.h"


User* usersList = NULL; // Variabile globale di riferimento per la lista
int currentUserId = 0; // Contatore per il sessionID dell'utente

volatile sig_atomic_t interrupted = 0;  // Flag per il segnale

int main() {
    srand(time(NULL)); // per random drink da suggerire
    int sockfd, newsockfd, portno;
    socklen_t clilen;
    struct sockaddr_in serv_addr, cli_addr;
    char *IP = getenv("DB_HOSTADDR"); // Indirizzo IP del server

    // Registra il gestore di segnali
    signal(SIGINT, sigint_handler);

    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
        perror("Errore nell'apertura della socket\n");
        exit(1);
    }

    // Aggiungo l'opzione SO_REUSEADDR
    int opt = 1;
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        perror("Errore nel settare SO_REUSEADDR sulla socket\n");
        exit(1);
    }

    // Aggiungo l'opzione SO_REUSEPORT
    if (setsockopt(sockfd, SOL_SOCKET, SO_REUSEPORT, &opt, sizeof(opt)) < 0) {
        perror("Errore nel settare SO_REUSEPORT sulla socket\n");
        exit(1);
    }

    bzero((char *)&serv_addr, sizeof(serv_addr));
    portno = PORT_NUMBER;

    serv_addr.sin_family = AF_INET;
    serv_addr.sin_port = htons(portno);
    serv_addr.sin_addr.s_addr = inet_addr(IP);

    if (bind(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) == -1) {
        perror("Errore nella fase di binding\n");
        exit(1);
    }

    listen(sockfd, 5);
    clilen = sizeof(cli_addr);
    printf("Server in ascolto sulla porta %d\n", portno);

    fd_set readfds;
    struct timeval timeout;

    while (!interrupted) {
        FD_ZERO(&readfds);
        FD_SET(sockfd, &readfds);
        
        timeout.tv_sec = 1;  // Controlla ogni secondo
        timeout.tv_usec = 0;

        int activity = select(sockfd + 1, &readfds, NULL, NULL, &timeout);

        if (activity < 0) {
            perror("\n[INTERRUPT] Il server è stato interrotto correttamente");
            exit(1);
        }

        if (activity == 0) {
            // Nessuna attività, controlla il flag e continua
            continue;
        }

        if (FD_ISSET(sockfd, &readfds)) {
            newsockfd = accept(sockfd, (struct sockaddr *)&cli_addr, &clilen);
            if (newsockfd < 0) {
                perror("Errore sulla accept della socket\n");
                exit(1);
            }

            pthread_t client_thread;
            int *new_sock_ptr = malloc(sizeof(int));
            *new_sock_ptr = newsockfd;
            if (pthread_create(&client_thread, NULL, client_handler, new_sock_ptr) < 0) {
                perror("Errore nella creazione del thread\n");
                return 1;
            }
        }
    }

    // Chiamata alla funzione di chiusura alla fine del main
    cleanup_and_exit(sockfd);
    return 0;
}
