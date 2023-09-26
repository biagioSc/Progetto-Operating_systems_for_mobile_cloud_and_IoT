import subprocess
import time
import threading

# Funzione per eseguire il comando adb e controllare i dispositivi collegati
def check_adb_devices():
    adb_output = subprocess.check_output(['adb', 'devices']).decode('utf-8')
    devices = [line.split('\t')[0] for line in adb_output.splitlines() if '\tdevice' in line]
    return devices

# Funzione per eseguire powdroid.py
def run_powdroid():
    return subprocess.Popen(['python',
                             r'C:\Users\biagi\Desktop\Progetto-Operating_systems_for_mobile_cloud_and_IoT\RisultatiPowDroid\Powdroid\powdroid.py'],
                            stdin=subprocess.PIPE)

# Funzione per eseguire il codice testautomatico.java in Android Studio
def run_test_code():

    # Esegui il test espresso utilizzando adb shell am instrument
    subprocess.Popen(['adb', 'shell', 'am', 'instrument', '-w',
                      'com.example.robotinteraction/androidx.test.runner.AndroidJUnitRunner', '-e', 'class',
                      'com.example.robotinteraction.Activity1_NewTest2'])


# Funzione per inviare un invio a powdroid.py
def send_enter_to_powdroid(process):
    process.stdin.write(b'\n')
    process.stdin.flush()

# Attendi la fine del processo e cattura l'output
def wait_for_process(process):
    process.wait()

# Controlla i dispositivi collegati
devices = check_adb_devices()

if len(devices) != 1:
    print("Si prega di collegare un solo dispositivo Android.")
else:
    # Esegui powdroid.py
    powdroid_process = run_powdroid()

    # Esegui il codice di test in Android Studio in un thread separato
    test_process = run_test_code()
    test_thread = threading.Thread(target=wait_for_process, args=(test_process,))
    test_thread.start()

    # Attendi che powdroid.py si avvii
    time.sleep(5)  # Puoi regolare il tempo di attesa a seconda delle tue esigenze

    # Invia un invio a powdroid.py
    send_enter_to_powdroid(powdroid_process)

    # Attendi la fine del test espresso
    test_thread.join()

    # Invia un altro invio a powdroid.py
    send_enter_to_powdroid(powdroid_process)

    # Chiudi Android Studio e qualsiasi emulatore connesso (implementa questa parte in base alle tue esigenze)
