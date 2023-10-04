import subprocess
import time

def check_adb_devices():
    adb_output = subprocess.check_output(['adb', 'devices']).decode('utf-8')
    devices = [line.split('\t')[0] for line in adb_output.splitlines() if '\tdevice' in line]
    return devices

def send_enter_powdroid(powdroid):
    powdroid.stdin.write(b'\n')
    powdroid.stdin.flush()

def execute_powdroid():
    print("[EXECUTE] Eseguo powdroid.py")
    script_path = 'powdroid.py'
    powdroid_process = subprocess.Popen(['python', script_path],
                                        stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

    return powdroid_process

def connection_adb():
    print("[SETUP] Si prega di attivare il wireless dubugging sul dispositivo android.")
    indirizzo_ip_cellulare = input("[IPADDRESS] Inserisci indirizzo IP cellulare: (es. 192.168.5.33:34829)")

    while True:
        try:
            subprocess.run(["adb", "connect", indirizzo_ip_cellulare], check=True)
            break
        except subprocess.CalledProcessError:
            # Se si verifica un errore, mostra un messaggio di errore
            print("[ERROR] Errore di connessione adb. Assicurati che l'indirizzo IP sia corretto.")
            # Chiedi all'utente di reinserire l'indirizzo IP
            indirizzo_ip_cellulare = input("[IPADDRESS] Inserisci indirizzo IP cellulare: ")

    # Il ciclo esce solo quando la connessione adb ha successo
    print("[CONNECTION] Connessione adb riuscita.")

def testOptimizeApp():
    scenario2 = "com.example.robotinteractionOttimizzata.testScenario2Opt"
    scenario3 = "com.example.robotinteractionOttimizzata.testScenario3Opt"
    scenario5 = "com.example.robotinteractionOttimizzata.testScenario5Opt"

    print("Test disponibili:")
    print("[SCENARIO2] testScenario2Opt.java")
    print("[SCENARIO3] testScenario3Opt.java")
    print("[SCENARIO5] testScenario5Opt.java")

    scelta = input("Inserisci un numero tra 2, 3 e 5 per scegliere lo scenario di test: ")

    if scelta == "2":
        print("[EXECUTE] Eseguo testScenario2Opt.java")
        subprocess.run(["adb", "shell", "am", "instrument", "-w",
                        "-e", "class", scenario2,
                        "com.example.robotinteractionOttimizzata.test/androidx.test.runner.AndroidJUnitRunner"])

    elif scelta == "3":
        print("[EXECUTE] Eseguo testScenario3Opt.java")
        subprocess.run(["adb", "shell", "am", "instrument", "-w",
                        "-e", "class", scenario3,
                        "com.example.robotinteractionOttimizzata.test/androidx.test.runner.AndroidJUnitRunner"])

    elif scelta == "5":
        print("[EXECUTE] Eseguo testScenario5Opt.java")
        subprocess.run(["adb", "shell", "am", "instrument", "-w",
                        "-e", "class", scenario5,
                        "com.example.robotinteractionOttimizzata.test/androidx.test.runner.AndroidJUnitRunner"])

def testOptimizeParzApp1():
    scenario2 = "com.example.robotinteractionOttimizzataParz.testScenario2OptParz"
    scenario3 = "com.example.robotinteractionOttimizzataParz.testScenario3OptParz"
    scenario5 = "com.example.robotinteractionOttimizzataParz.testScenario5OptParz"

    print("Test disponibili:")
    print("[SCENARIO2] testScenario2OptParz.java")
    print("[SCENARIO3] testScenario3OptParz.java")
    print("[SCENARIO5] testScenario5OptParz.java")

    scelta = input("Inserisci un numero tra 2, 3 e 5 per scegliere lo scenario di test: ")

    if scelta == "2":
        print("[EXECUTE] Eseguo testScenario2OptParz.java")
        subprocess.run(["adb", "shell", "am", "instrument", "-w",
                        "-e", "class", scenario2,
                        "com.example.robotinteractionOttimizzataParz.test/androidx.test.runner.AndroidJUnitRunner"])

    elif scelta == "3":
        print("[EXECUTE] Eseguo testScenario3OptParz.java")
        subprocess.run(["adb", "shell", "am", "instrument", "-w",
                        "-e", "class", scenario3,
                        "com.example.robotinteractionOttimizzataParz.test/androidx.test.runner.AndroidJUnitRunner"])

    elif scelta == "5":
        print("[EXECUTE] Eseguo testScenario5OptParz.java")
        subprocess.run(["adb", "shell", "am", "instrument", "-w",
                        "-e", "class", scenario5,
                        "com.example.robotinteractionOttimizzataParz.test/androidx.test.runner.AndroidJUnitRunner"])

def testNoOptimizeApp():
    scenario2 = "com.example.robotinteraction.testScenario2NoOpt"
    scenario3 = "com.example.robotinteraction.testScenario3NoOpt"
    scenario5 = "com.example.robotinteraction.testScenario5NoOpt"


    print("Test disponibili:")
    print("[SCENARIO2] testScenario2NoOpt.java")
    print("[SCENARIO3] testScenario3NoOpt.java")
    print("[SCENARIO5] testScenario5NoOpt.java")


    scelta = input("Inserisci un numero tra 2, 3 e 5 per scegliere lo scenario di test: ")

    if scelta == "2":
        print("[EXECUTE] Eseguo testScenario2NoOpt.java")
        subprocess.run(["adb", "shell", "am", "instrument", "-w",
                        "-e", "class", scenario2,
                        "com.example.robotinteraction.test/androidx.test.runner.AndroidJUnitRunner"])

    elif scelta == "3":
        print("[EXECUTE] Eseguo testScenario3NoOpt.java")
        subprocess.run(["adb", "shell", "am", "instrument", "-w",
                        "-e", "class", scenario3,
                        "com.example.robotinteraction.test/androidx.test.runner.AndroidJUnitRunner"])

    elif scelta == "5":
        print("[EXECUTE] Eseguo testScenario5NoOpt.java")
        subprocess.run(["adb", "shell", "am", "instrument", "-w",
                        "-e", "class", scenario5,
                        "com.example.robotinteraction.test/androidx.test.runner.AndroidJUnitRunner"])

def openDirectory():
    import subprocess
    import sys

    # Specifica il percorso della cartella da aprire
    percorso_cartella = "output"

    # Verifica il sistema operativo
    sistema_operativo = sys.platform

    # Comando per aprire la cartella in base al sistema operativo
    if sistema_operativo == "win32":
        # Windows
        comando = ["explorer", percorso_cartella]
    elif sistema_operativo == "darwin":
        # macOS
        comando = ["open", percorso_cartella]
    else:
        # Altri sistemi operativi (presume che sia Linux o Unix-like)
        comando = ["xdg-open", percorso_cartella]

    # Esegui il comando per aprire la cartella
    try:
        subprocess.Popen(comando)
    except Exception as e:
        print(f"Si è verificato un errore: {e}")


def main():
    subprocess.run(["adb", "disconnect"])
    print("[SETUP] Si prega di attivare la modalità sviluppatore sul dispositivo android.")
    print("[SETUP] Si prega di attivare l'usb dubugging sul dispositivo android.")
    print("[SETUP] Si prega di attivare regolare la luminosità al 50% sul dispositivo android.")
    print("[SETUP] Si prega di attivare regolare l'audio al 50% sul dispositivo android.")
    print("[SETUP] Si prega di disattivare le applicazioni in background sul dispositivo android.")
    print("[SETUP] Si prega di disattivare lo spegnimento automatico dello schermo sul dispositivo android.")
    print("[SETUP] Si prega di non tenere in carica il dispositivo android durante l'esecuzione del test.")
    print("[SETUP] Si prega di chiudere tutte le app aperte sul dispositivo android.")
    print("[INFO] Ogni test potrebbe durare 150 secondi (2.30 minuti).")

    time.sleep(5)

    install = input("[APP INSTALL] Hai installato l'app? Ricorda che ci sono due tipi app a seconda del test che si vuole fare. (y/n) ")

    while install == "n":
        install = input(
            "[APP INSTALL] Hai installato l'app? Ricorda che ci sono due tipi app a seconda del test che si vuole fare. (y/n) ")
    testChoise = "y"

    while testChoise == "y":
        while len(check_adb_devices()) != 1:
            print("[CONNECT] Si prega di collegare un solo dispositivo Android.")
            time.sleep(5)

        # Esecuzione di powdroid.py
        powdroid_process = execute_powdroid()

        # Invia un invio a powdroid.py
        send_enter_powdroid(powdroid_process)

        while len(check_adb_devices())!=0:
            print("[UNPLUG] Si prega di scollegare il dispositivo Android.")
            time.sleep(5)

        connection_adb()

        # Invia un invio a powdroid.py x avviare sessione
        send_enter_powdroid(powdroid_process)

        print("[TEST1] App ottimizzata")
        print("[TEST2] App ottimizzata parz1")
        print("[TEST3] App non ottimizzata")

        test = input("[SCELTA] Quale test vuoi eseguire? ")

        if test == "1":
            testOptimizeApp()

        elif test == "2":
            testOptimizeParzApp1()

        elif test == "3":
            testNoOptimizeApp()

        # Invia un invio a powdroid.py x stoppare sessione
        send_enter_powdroid(powdroid_process)

        subprocess.run(["adb", "disconnect"])

        while len(check_adb_devices()) != 1:
            print("[CONNECT] Si prega di collegare il dispositivo Android per salvare i dati di powdroid.")
            time.sleep(5)

        print("[WAIT] Powdroid save data...")
        send_enter_powdroid(powdroid_process)

        powdroid_process.wait()  # Attendi che powdroid.py termini

        powdroid_process.communicate()

        print("[COMPLETE] Tutte le operazioni sono state completate.")
        print("[DATA] Dati disponibili nella cartella output.")
        openDirectory()
        time.sleep(10)
        testChoise = input("Vuoi fare un altro test? (y/n): ").lower()


if __name__ == '__main__':
    main()

