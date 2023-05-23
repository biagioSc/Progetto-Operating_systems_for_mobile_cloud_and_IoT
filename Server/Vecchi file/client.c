#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <arpa/inet.h>

// I client in c e solo di debug

#define BUFSIZE 4096

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
 * @brief Funzione per la ricezione di dati
 * 
 * @param sock Socket di ricezione
 * @param data Dati in ingresso
 * @param size Dimensione
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
 * @brief Funzione per la ricezione di stringhe
 * 
 * @param sock Socket di ricezione
 * @param s Dati in ingresso
 * @return int Interno di ritorno di succeso o meno
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

  char *ip = "127.0.0.1";
  int port = 5566;
  int sock;

  struct sockaddr_in addr;
  socklen_t addr_size;
  char buffer[BUFSIZE];
  char scelta[10];
  char *puntatoreProva = NULL;
  int n;

  sock = socket(AF_INET, SOCK_STREAM, 0);
  if (sock < 0)
  {
    perror("[-]Socket error");
    exit(1);
  }
  printf("[+]TCP server socket created.\n");

  memset(&addr, '\0', sizeof(addr));
  addr.sin_family = AF_INET;
  addr.sin_port = port;

  addr.sin_addr.s_addr = inet_addr(ip);

  controllaFunzione(connect(sock, (struct sockaddr *)&addr, sizeof(addr)), "[ERRORE] Non sono riuscito a connettermi al client");
  printf("Connected to the server.\n");

  // Bevenuto
  puntatoreProva = readString(sock);
  printf("Server: %s\n", puntatoreProva);
  free(puntatoreProva);

  // Attendi di essere servito
  puntatoreProva = readString(sock);
  printf("Server: %s\n", puntatoreProva);
  free(puntatoreProva);

  // Drink proposto
  puntatoreProva = readString(sock);
  printf("Server: %s\n", puntatoreProva);
  free(puntatoreProva);

  printf("Vuoi confermare il tuo drink  ? 1=(Si)|0=(No) ");
  fgets(buffer, BUFSIZE, stdin);
  sendString(sock, buffer);
  fflush(stdin);


  if (atoi(buffer) == 1) // Drink vabbene
  {
    // Grazie per aver ordinato
    puntatoreProva = readString(sock);
    printf("Server: %s\n", puntatoreProva);
    free(puntatoreProva);
  }
  else // Drink da rifare
  {
    puntatoreProva = readString(sock);
    printf("Server: %s\n", puntatoreProva);
    free(puntatoreProva);

    printf("Inserisci il drink : ");
    fgets(buffer, BUFSIZE, stdin);
    sendString(sock, buffer);
  }

  // Ricevo il messaggio di conferma
  puntatoreProva = readString(sock);
  printf("Server: %s\n", puntatoreProva);
  free(puntatoreProva);

  // Messaggio Domanda interazione
  puntatoreProva = readString(sock);
  printf("Server: %s \n", puntatoreProva);
  free(puntatoreProva);
  
  fflush(stdin);
  fgets(buffer, BUFSIZE, stdin);
  sendString(sock, buffer);

  if (atoi(buffer) == 1) // Interazione
  {
    // Ciao Umano
    puntatoreProva = readString(sock);
    printf("Server: %s\n", puntatoreProva);
    free(puntatoreProva);

    fgets(buffer, BUFSIZE, stdin);
    sendString(sock, buffer);

    // Certo
    puntatoreProva = readString(sock);
    printf("Server: %s\n", puntatoreProva);
    free(puntatoreProva);

    fgets(buffer, BUFSIZE, stdin);
     sendString(sock, buffer);

    // Beato te
    puntatoreProva = readString(sock);
    printf("Server: %s\n", puntatoreProva);
    free(puntatoreProva);
    fgets(buffer, BUFSIZE, stdin);
     sendString(sock, buffer);
  }
  else // Non interazione
  {
    puntatoreProva = readString(sock);
    printf("Server: %s\n", puntatoreProva);
    free(puntatoreProva);
  }

  // Saluto cliente
  puntatoreProva = readString(sock);
  printf("Server: %s\n", puntatoreProva);
  free(puntatoreProva);

  close(sock);
  printf("Disconesso dal server.\n");

  return 0;
}