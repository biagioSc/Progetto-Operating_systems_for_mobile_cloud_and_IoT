#include <stdio.h>      //Input ed output di base
#include <stdlib.h>     //Standard libray
#include <string.h>     //Manipolazione Stringhe
#include <sys/socket.h> //Gestione delle socket
#include <arpa/inet.h>  //Gestione di altre attivita web
#include <pthread.h>    //Gestione dei Theread
#include <unistd.h>     //Accesso ad api POSIX

// Code By Antonio Lanuto Matricola :N86003762
// Server in C

//! Definizione macro

#define SERVERPORT 5566 // Porta di ascolto standard di TCP
#define BUFSIZE 4096    // Buffer Size

//! Ridifinizioni Strutture
typedef struct sockaddr_in SA_IN;
typedef struct sockaddr SA;

//! Variabili Globali
int numeroThread = 0;                                           // Variabile Globale per il Numero corrente del Thread
int servizio = 0;                                               // Variabile per indicare gli unici 2 thread che devono lavorare
pthread_mutex_t semaforo1 = PTHREAD_MUTEX_INITIALIZER;          // Variabile per la gestione del semaforo1
pthread_mutex_t semaforo2 = PTHREAD_MUTEX_INITIALIZER;          // Variabile per la gestione del semaforo1
pthread_cond_t condizioneDiSblocco1 = PTHREAD_COND_INITIALIZER; // Variabile per la gestione della variabile di sblocco
pthread_cond_t condizioneDiSblocco2 = PTHREAD_COND_INITIALIZER; // Variabile per la gestione della variabile di sblocco

//! Funzioni

// Funzioni di Supporto per Mandare e Ricevere

/**
 * @brief Funzione per l'invio di dati che legge dalla riga
 *
 * @param sock Socket di invio
 * @param data Dati da inviare
 * @param size Dimensione
 * @return int Interno di ritorno di succeso o meno
 */
int sendRaw(int sock, const void *data, int size)
{
    const char *buffer = (const char *)data;
    while (size > 0)
    {
        ssize_t sent = send(sock, buffer, size, 0);
        if (sent < 0)
            return 0;
        buffer += sent;
        size -= sent;
    }
    return 1;
}

/**
 * @brief Funzione per l'invio di stringhe su socket
 *
 * @param sock Socket di invio dei dati
 * @param s Dati in ingresso da inviare
 * @return int Interno di ritorno di succeso o meno
 */
int sendString(int sock, const char *s)
{
    int32_t len = strlen(s) + 1;
    return sendRaw(sock, s, len);
}

/**
 * @brief Funzione per la ricezione della riga
 *
 * @param sock Socket di ricezione dati
 * @param data Dati da leggere
 * @param size Dimensioee dati
 * @return int Interno di ritorno di succeso o meno
 */
int readRaw(int sock, void *data, int size)
{
    char *buffer = (char *)data;
    while (size > 0)
    {
        ssize_t recvd = recv(sock, buffer, size, 0);
        if (recvd < 0)
            return -1;
        if (recvd == 0)
            return 0;
        buffer += recvd;
        size -= recvd;
    }
    return 1;
}
/**
 * @brief Funzione per la ricezione di stringhe che delimita solo i dati contenuti effettivamente
 *
 * @param sock Socket dove ricevere i dati
 * @return char* Ritorna dell array con i dati al intenro
 */
char *readString(int sock)
{
    char *ret = NULL, *tmp;
    size_t len = 0, cap = 0;
    char ch;

    do
    {
        if (readRaw(sock, &ch, 1) <= 0)
        {
            free(ret);
            return NULL;
        }

        if (ch == '\0')
            break;

        if (len == cap)
        {
            cap += 100;
            tmp = (char *)realloc(ret, cap);
            if (!tmp)
            {
                free(ret);
                return NULL;
            }
            ret = tmp;
        };
        ret[len] = ch;
        ++len;
    } while (1);

    if (len == cap)
    {
        tmp = (char *)realloc(ret, cap + 1);
        if (!tmp)
        {
            free(ret);
            return NULL;
        }
        ret = tmp;
    }

    ret[len] = '\0';
    return ret;
}

// Funzione che esegue il check nel database postgresql della presenza effettiva del utente
int check(void *arg)
{
    int clientSocket = *(int *)arg; // Variabile per la gestione della socket del client
    int variabileFittizia = 1;      // Variabile per la gestione della presenza o meno del utente nel DB
    // Controllo Attuale : fittizzio per il momento manca la conessione al DB
    if (variabileFittizia == 1)
    {
        printf("[CHECK] Utente Presente nel DB\n");
        return 1;
    }
    else
    {
        printf("[CHECK] Utente non Presente nel DB\n");
        return 0;
    }
}

// Funzione che da il benvenuto a un nuovo utente e manda un messaggio al client di benvenuto
void *welcome(void *arg)
{
    int clientSocket = *(int *)arg; // Variabile per la gestione della socket del client
    char buffer[BUFSIZE];           // Buffer per la gestione dei messaggi
    int n;                          // Variabile per la gestione del numero di byte letti

    // Messaggio di benvenuto

    strcpy(buffer, "Benvenuto nel Bar di Boe  \0");
    sendString(clientSocket, buffer);
    return NULL;
}

// Funzione che manda un messaggio di wait al client
void *wait(void *arg)
{
    int clientSocket = *(int *)arg; // Variabile per la gestione della socket del client
    char buffer[BUFSIZE];           // Buffer per la gestione dei messaggi
    int n;                          // Variabile per la gestione del numero di byte letti

    // Messaggio di wait

    strcpy(buffer, "Attendi di essere Servito \0");
    sendString(clientSocket, buffer);
    return NULL;
}

// Funzione che suggerisce al utente un drink da bere e lo manda al client
void *ordering(void *arg)
{
    int clientSocket = *(int *)arg; // Variabile per la gestione della socket del client
    char buffer[BUFSIZE];           // Buffer per la gestione dei messaggi
    char *scelta = NULL;            // Ulteriore variabile per lettura di messaggi
    int n;                          // Variabile per la gestione del numero di byte letti

    // Messaggio di benvenuto
    strcpy(buffer, "Suggerimento del giorno : Gin tonic con Angostura \0");
    sendString(clientSocket, buffer);

    scelta = readString(clientSocket);
    if (atoi(scelta) == 1)
    {

        strcpy(buffer, "Grazie per aver ordinato \0");
        sendString(clientSocket, buffer);
    }
    else
    {

        strcpy(buffer, "Ok che drink allora vuoi avere : \0");
        sendString(clientSocket, buffer);
        // Ricevo il drink
        free(scelta);
        scelta = readString(clientSocket);
        printf("Il cliente ha scelto : %s \n", scelta);
    }
    free(scelta);
    return NULL;
}
// Funzione che conversa con l'utente
void *interacting(void *arg)
{
    int clientSocket = *(int *)arg; // Variabile per la gestione della socket del client
    char buffer[BUFSIZE];           // Buffer per la gestione dei messaggi
    char *scelta = NULL;
    int n; // Variabile per la gestione del numero di byte letti

    // Messaggi di interacting

    strcpy(buffer, "Ciao Umano ! Come va che mi racconti di bello ?\0");
    sendString(clientSocket, buffer);
    scelta = readString(clientSocket);
    free(scelta);

    strcpy(buffer, "Certo che hai sempre delle storie avvicenti , come mai stasera sei qui ? \0");
    sendString(clientSocket, buffer);
    scelta = readString(clientSocket);
    free(scelta);

    strcpy(buffer, "Beato te , invece noi robot facciamo solo drink qui , ah ecco mi sa che è forse è pronto\0");
    sendString(clientSocket, buffer);
    scelta = readString(clientSocket);
    free(scelta);

    return NULL;
}

// Funzione che non conversa con l'utente
void *non_interacting(void *arg)
{
    int clientSocket = *(int *)arg; // Variabile per la gestione della socket del client
    char buffer[BUFSIZE];           // Buffer per la gestione dei messaggi
    int n;                          // Variabile per la gestione del numero di byte letti

    // Messaggi di non interacting

    strcpy(buffer, "In teoria il tuo drink tra un po sara pronto\0");
    sendString(clientSocket, buffer);
    sleep(5);

    return NULL;
}

// Funzione che fa prepare al robot il drink
void *serving(void *arg)
{
    int clientSocket = *(int *)arg; // Variabile per la gestione della socket del client
    char buffer[BUFSIZE];           // Buffer per la gestione dei messaggi
    char *scelta = NULL;
    int n; // Variabile per la gestione del numero di byte letti

    // Messaggio di Preparazione

    strcpy(buffer, "Drink in preparazione\0");
    sendString(clientSocket, buffer);

    // Questionario interazione

    strcpy(buffer, "Vuoi interagire con il robot ? 1=(Si)|0=(No) \0");
    sendString(clientSocket, buffer);

    // Ricevo la riposta
    scelta = readString(clientSocket);
    if (atoi(scelta) == 1)
    {
        interacting(&clientSocket);
    }
    else
    {
        non_interacting(&clientSocket);
    }
    free(scelta);
}

// Funzione che avvisa utente che il drink è pronto il client
void *farewelling(void *arg)
{
    int clientSocket = *(int *)arg; // Variabile per la gestione della socket del client
    char buffer[BUFSIZE];           // Buffer per la gestione dei messaggi
    int n;                          // Variabile per la gestione del numero di byte letti

    // Messaggio di farewelling

    strcpy(buffer, "Ecco a te il tuo drink grazie per essere stato al bar di Boe\0");
    sendString(clientSocket, buffer);
    return NULL;
}

/**
 * @brief Funzione per la Gestione della connesione con socket
 *
 * @param clientSocket
 */

void *gestioneConnessioneDispari(void *p_clientSocket)
{
    // Operazioni Preliminari per la gestione
    int clientSocket = *(int *)p_clientSocket; // Riporto il puntatore a una variabile normale
    free(p_clientSocket);

    // Cambio Il numero dei thread imposto un semaforo cosi ne fa un cambio alla volta
    pthread_mutex_lock(&semaforo2);
    int numeroThreadLocale = ++numeroThread;
    pthread_mutex_unlock(&semaforo2);

    // Incremento il numero di Thread
    printf("\n\n[Thread n %d] Questo thread ha un tid di : %d \n", numeroThreadLocale, (int)pthread_self());
    welcome(&clientSocket); // Eseguo il welcome
    wait(&clientSocket);    // Metto utente in Fase di wait

    // Inizio Gestione
    //! Apro fase Critica
    pthread_mutex_lock(&semaforo2);

    printf("[Thread n %d] Sono entrato in fase critica \n", numeroThreadLocale);
    printf("[Thread n %d] Sono in attesa di essere sbloccato \n", numeroThreadLocale);

    while (servizio > 2) // Controllo se sono il thread che deve lavorare
    {
        pthread_cond_wait(&condizioneDiSblocco1, &semaforo2); // Attendo di essere sbloccato
    }
    servizio++; // Aumento di uno il servizio

    printf("[Thread n %d] Sono stato sbloccato \n", numeroThreadLocale);
    ordering(&clientSocket);    // Il cliente ordina il drink
    serving(&clientSocket);     // Al cliente viene preparato il drink / conversa con il robot
    farewelling(&clientSocket); // Il cliente prende il drink e saluta

    servizio--;                                    // Sblocco qualche altro thread
    pthread_cond_broadcast(&condizioneDiSblocco2); // Sblocco un dei thread in coda
    pthread_mutex_unlock(&semaforo2);              // Chiudo la fase critica

    //! Chiudo fase Critica
    // Fine Gestione

    close(clientSocket);
    printf("[Thread n %d] Chiuso la socket e ho finito la fase critica \n\n\n", numeroThreadLocale);
    numeroThread--;
    return NULL;
}

/**
 * @brief Funzione per la Gestione della connesione con socket Pari
 *
 * @param clientSocket
 */

void *gestioneConnessionePari(void *p_clientSocket)
{
    // Operazioni Preliminari per la gestione
    int clientSocket = *(int *)p_clientSocket; // Riporto il puntatore a una variabile normale
    free(p_clientSocket);

    // Cambio Il numero dei thread imposto un semaforo cosi ne fa un cambio alla volta

    int numeroThreadLocale = ++numeroThread;

    // Incremento il numero di Thread
    printf("\n\n[Thread n %d] tid - %d - socket %d \n", numeroThreadLocale, (int)pthread_self(), clientSocket);
    welcome(&clientSocket); // Eseguo il welcome
    wait(&clientSocket);    // Metto utente in Fase di wait

    // Inizio Gestione
    //! Apro fase Critica
    pthread_mutex_lock(&semaforo1);

    printf("[Thread n %d] Sono entrato in fase critica \n", numeroThreadLocale);
    printf("[Thread n %d] Sono in attesa di essere sbloccato \n", numeroThreadLocale);

    while (servizio > 2) // Controllo se sono il thread che deve lavorare
    {
        pthread_cond_wait(&condizioneDiSblocco1, &semaforo1); // Attendo di essere sbloccato
    }
    servizio++; // Aumento di uno il servizio

    printf("[Thread n %d] Sono stato sbloccato \n", numeroThreadLocale);
    ordering(&clientSocket);    // Il cliente ordina il drink
    serving(&clientSocket);     // Al cliente viene preparato il drink / conversa con il robot
    farewelling(&clientSocket); // Il cliente prende il drink e saluta

    servizio--;                                    // Sblocco qualche altro thread
    pthread_cond_broadcast(&condizioneDiSblocco1); // Sblocco un dei thread in coda
    pthread_mutex_unlock(&semaforo1);              // Chiudo la fase critica

    //! Chiudo fase Critica
    // Fine Gestione

    close(clientSocket);
    printf("[Thread n %d] Chiuso la socket e ho finito la fase critica \n\n\n", numeroThreadLocale);
    numeroThread--;

    return NULL;
}

/**
 * @brief Funzione per il controllo di una qualsiasi funzione , Creo una sorta di ambiente sicuro dove far girare la mia funzione
 *
 * @param exp Ritorno del risultato della mia funzione
 * @param msg Messaggio d Errore di debug da far uscire a video
 * @return int
 */
int controllaFunzione(int exp, const char *msg)
{
    if (exp < 0)
    {
        perror(msg);
        exit(EXIT_FAILURE);
    }
    return exp;
}

int main()
{
    // Dichiarazione Variabili di supporto
    int serverSocket, clientSocket;
    int contatore = 0;
    SA_IN serverAddr, clientAddr;
    socklen_t addr_size;

    char *IP = "127.0.0.1"; // Indirizzo IP del server

    // Creo il mio Socket Server
    controllaFunzione(serverSocket = socket(AF_INET, SOCK_STREAM, 0), "[ERRORE] Non sono riuscito a creare la socket");

    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = SERVERPORT;
    serverAddr.sin_addr.s_addr = inet_addr(IP);

    // Bind del mio socket
    controllaFunzione(bind(serverSocket, (SA *)&serverAddr, sizeof(serverAddr)), "[Errore] Bind non riuscito");
    controllaFunzione(listen(serverSocket, 3), "[Errore] Listen non riuscito\0");

    //Ciclo infinito per la gestione delle connessioni
    while (1)
    {
        printf("[LISTEN] In Attesa di una connessione...\n");
        addr_size = sizeof(clientAddr);
        controllaFunzione(clientSocket = accept(serverSocket, (SA *)&clientAddr, &addr_size), "[Errore] Accept non riuscito");
        printf("[ACCEPT] Client Connesso : %s and port: %i \n", inet_ntoa(clientAddr.sin_addr), ntohs(clientAddr.sin_port));

        //Controllo di utente presente nel database
        pthread_t thread1;
        int *p_newSock = malloc(sizeof(int));
        *p_newSock = clientSocket;
        
        if (checkClient(&clientSocket) == 1)
        {
            //Controllo effettuato con successo
            //Smisto l'utente in 2 code di thread diversi
            if (contatore % 2 == 0)
            {
                pthread_create(&thread1, NULL, gestioneConnessionePari, p_newSock);
                contatore++;
            }
            else
            {
                pthread_create(&thread1, NULL, gestioneConnessioneDispari, p_newSock);
                contatore++;
            }
        }
        else
        {
            printf("[ERRORE] Autenticazione non riuscita chiusura del client \n");
            close(clientSocket);
        }
        
    }
    close(serverSocket);
    return 0;
}
