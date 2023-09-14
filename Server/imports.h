#ifndef IMPORTS_H
#define IMPORTS_H

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include <strings.h>
#include <unistd.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <arpa/inet.h>
#include "libpq-fe.h"
#include <signal.h>

#define BUFFER_SIZE 256
#define MAX_TOPICS 100
#define PORT_NUMBER 8080

/**
 * @brief Struttura per mantenere le informazioni di sessione di un utente
 * 
 */
typedef struct User {
    int id;
    char email[BUFFER_SIZE];
    struct User* next;
} User;


User* usersList = NULL; // Variabile globale di riferimento per la lista
static int currentUserId = 0; // Contatore per il sessionID dell'utente

volatile sig_atomic_t interrupted = 0;  // Flag per il segnale

#endif