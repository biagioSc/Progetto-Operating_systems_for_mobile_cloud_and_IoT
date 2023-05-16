#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include <time.h>

#define MAX_BUFFER_SIZE 4096
#define PORT 0000
#define ADDRESS "000.000.000"

void processMessage(SOCKET clientSocket) {

    //la funzione genera in maniera casuale 3 tipi di messaggi che possono
    //essere "tipologie" di risposte del server quando si è nella schermata di
    //login, al fine di simulare il comportamento in questa fase.

    //1: Error: login non andato a buon fine
    //2: Ordering: Login effettuato e non c'è da aspettare nessuno in coda
    //3: Waiting: Login effettuato e bisogna attendere il proprio turno


    char buffer[MAX_BUFFER_SIZE];
    srand(time(0)); 
    int randomResponse = rand() % 3;

    memset(buffer, 0, sizeof(buffer));
    recv(clientSocket, buffer, sizeof(buffer) - 1, 0);
    printf("Received email: %s\n", buffer);

    memset(buffer, 0, sizeof(buffer));
    recv(clientSocket, buffer, sizeof(buffer) - 1, 0);
    printf("Received password: %s\n", buffer);

    const char *responses[3] = {"Error", "Ordering", "Waiting"};
    const char *message = responses[randomResponse];
    send(clientSocket, message, strlen(message), 0);
}

int main() {
    WSADATA wsaData;
    SOCKET listenSocket, clientSocket;
    struct sockaddr_in serverAddr, clientAddr;

    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        printf("Failed to load Winsock");
        return -1;
    }

    listenSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (listenSocket == INVALID_SOCKET) {
        printf("Failed to create socket");
        WSACleanup();
        return -1;
    }

    serverAddr.sin_family = AF_INET;
    serverAddr.sin_port = htons(PORT);
    serverAddr.sin_addr.s_addr = inet_addr(ADDRESS);

    if (bind(listenSocket, (struct sockaddr*)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        printf("Bind failed");
        closesocket(listenSocket);
        WSACleanup();
        return -1;
    }

    if (listen(listenSocket, 5) == SOCKET_ERROR) {
        printf("Listen failed");
        closesocket(listenSocket);
        WSACleanup();
        return -1;
    }

    printf("Waiting for connections...\n");

    while(1) {
        int clientAddrSize = sizeof(clientAddr);
        clientSocket = accept(listenSocket, (struct sockaddr*)&clientAddr, &clientAddrSize);

        if (clientSocket == INVALID_SOCKET) {
            printf("Accept failed");
            continue;
        }

        printf("Client connected\n");

        processMessage(clientSocket);

        closesocket(clientSocket);
    }

    closesocket(listenSocket);
    WSACleanup();

    return 0;
}
