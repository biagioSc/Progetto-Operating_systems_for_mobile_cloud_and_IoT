# Robot-Interaction

• n utenti (client) devono interagire con un robot (server) • L'interazione tra un utente e un robot è gestita in base ad una state machine che mappa l'intera interazione dall'inizio alla fine. • I possibili stati di un robot possono essere:

• New : nuovo utente è arrivato • Welcoming: il robot lo saluta • Waiting: attende il suo turno • Ordering: il robot suggerisce un drink, il client può confermare o richiedere un altro • Serving: preparazione del drink • Interacting: durante la preparazione del drink, il robot può chiacchierare con l’utente di un qualche argomento • Non-interacting: il robot prepara il drink senza chiacchierare perché l’utente non è interessato a interagire oltre • Farewelling: il drink è pronto, il robot lo dice all’utente e saluta • Gone: il cliente è andato via • Out-of-sight: il cliente si è allontanato

• Solo due utenti alla volta possono trovarsi nello stato di “ordering” e "serving" • Tutti gli altri utenti ricevono un messaggio di benvenuto, ma restano in attesa • Applicazione per cellulare o tablet

• [Opzionale] Per una complessità maggiore, e quindi punti extra, si può: • Aggiungere registrazione/login degli utenti usando un database Mysql or PostgreSQL • Aggiungere alla registrazione un piccolo questionario per prendere info degli utenti, quali preferenze di drink o argomenti per la conversazione da usare nello stato di “ordering” e “interacting” rispettivamente • Gestione degli stati a partire dallo stato iniziale e da tutti gli stati
