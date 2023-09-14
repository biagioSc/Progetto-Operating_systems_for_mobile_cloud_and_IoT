#ifndef MANAGER_H
#define MANAGER_H

/**
 * @brief Gestisce le richieste dei vari client che si collegano.
 * @param socket_desc Il socket file descriptor.
 * @return void
 */
void *client_handler(void *socket_desc);

#endif