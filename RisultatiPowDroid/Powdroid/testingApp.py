import subprocess
import time

def check_adb_devices():
    adb_output = subprocess.check_output(['adb', 'devices']).decode('utf-8')
    devices = [line.split('\t')[0] for line in adb_output.splitlines() if '\tdevice' in line]
    return devices


# Controlla i dispositivi collegati
while len(check_adb_devices()) != 1:
    print("[CONNECT] Si prega di collegare un solo dispositivo Android.")
    time.sleep(10)

# Esecuzione di powdroid.py
print("[EXECUTE] Eseguo powdroid.py")
powdroid_process = subprocess.Popen(['python', r'C:\Users\biagi\Desktop\Progetto-Operating_systems_for_mobile_cloud_and_IoT\RisultatiPowDroid\Powdroid\powdroid.py'], stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE)

# Invia un invio a powdroid.py
powdroid_process.stdin.write(b'\n')
powdroid_process.stdin.flush()

while len(check_adb_devices())!=0:
    print("[UNPLUG] Si prega di scollegare il dispositivo Android.")
    time.sleep(10)

indirizzo_ip_cellulare = input("[IPADDRESS] Inserisci indirizzo IP cellulare: ")

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

# Invia un invio a powdroid.py x avviare sessione
powdroid_process.stdin.write(b'\n')
powdroid_process.stdin.flush()

scenario1 = "com.example.robotinteraction.testScenario1"
scenario2 = "com.example.robotinteraction.testScenario2"
scenario3 = "com.example.robotinteraction.testScenario3"
scenario4 = "com.example.robotinteraction.testScenario4"
scenario5 = "com.example.robotinteraction.testScenario5"
scenario6 = "com.example.robotinteraction.testScenario6"

print("Test disponibili:")
print("[SCENARIO1] " + scenario1)
print("[SCENARIO2] " + scenario2)
print("[SCENARIO3] " + scenario3)
print("[SCENARIO4] " + scenario4)
print("[SCENARIO5] " + scenario5)
print("[SCENARIO6] " + scenario6)


scelta = input("Inserisci un numero tra 1 e 5 per scegliere lo scenario di test: ")

if scelta == 1:
    print("[EXECUTE] Eseguo testScenario1.java")
    subprocess.run(["adb", "shell", "am", "instrument", "-w",
                    "-e", "class", scenario1,
                    "com.example.robotinteraction.test/androidx.test.runner.AndroidJUnitRunner"])

elif scelta == 2:
    print("[EXECUTE] Eseguo testScenario2.java")
    subprocess.run(["adb", "shell", "am", "instrument", "-w",
                    "-e", "class", scenario2,
                    "com.example.robotinteraction.test/androidx.test.runner.AndroidJUnitRunner"])

elif scelta == 3:
    print("[EXECUTE] Eseguo testScenario3.java")
    subprocess.run(["adb", "shell", "am", "instrument", "-w",
                    "-e", "class", scenario3,
                    "com.example.robotinteraction.test/androidx.test.runner.AndroidJUnitRunner"])

elif scelta == 4:
    print("[EXECUTE] Eseguo testScenario4.java")
    subprocess.run(["adb", "shell", "am", "instrument", "-w",
                    "-e", "class", scenario4,
                    "com.example.robotinteraction.test/androidx.test.runner.AndroidJUnitRunner"])

elif scelta == 5:
    print("[EXECUTE] Eseguo testScenario5.java")
    subprocess.run(["adb", "shell", "am", "instrument", "-w",
                    "-e", "class", scenario5,
                    "com.example.robotinteraction.test/androidx.test.runner.AndroidJUnitRunner"])

elif scelta == 6:
    print("[EXECUTE] Eseguo testScenario6.java")
    subprocess.run(["adb", "shell", "am", "instrument", "-w",
                    "-e", "class", scenario5,
                    "com.example.robotinteraction.test/androidx.test.runner.AndroidJUnitRunner"])


# Invia un invio a powdroid.py x stoppare sessione
powdroid_process.stdin.write(b'\n')
powdroid_process.stdin.flush()

subprocess.run(["adb", "disconnect"])

while len(check_adb_devices()) != 1:
    print("[CONNECT] Si prega di collegare il dispositivo Android.")
    time.sleep(10)

print("[WAIT] Powdroid save data...")
powdroid_process.stdin.write(b'\n')
powdroid_process.stdin.flush()
powdroid_process.wait()  # Attendi che powdroid.py termini

out, err = powdroid_process.communicate()

# Stampa ciò che powdroid.py ha stampato a video
print(out.decode())
print(err.decode())

print("[COMPLETE] Tutte le operazioni sono state completate.")
