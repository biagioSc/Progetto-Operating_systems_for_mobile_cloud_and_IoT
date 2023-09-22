import os
import subprocess
import csv
from datetime import datetime
import time

# Indirizzo IP e porta dell'emulatore Android Studio
emulator_ip = "192.168.232.2"


# Connessione all'emulatore tramite ADB
def connect_to_emulator(ip):
    try:
        subprocess.check_output(["adb", "connect", ip], stderr=subprocess.STDOUT)
        print("[PowDroid] Connected to emulator at", ip)
    except subprocess.CalledProcessError as e:
        print("[PowDroid] Error connecting to emulator:", e.output.decode())


# Esecuzione di comandi ADB
def run_adb_command(command):
    try:
        result = subprocess.check_output(["adb"] + command, stderr=subprocess.STDOUT)
        return result.decode()
    except subprocess.CalledProcessError as e:
        return e.output.decode()


# Pulizia dei dati della batteria
def clear_battery_stats():
    run_adb_command(["shell", "dumpsys", "battery", "unplug"])
    run_adb_command(["shell", "dumpsys", "battery", "reset"])


# Registrazione dei dati della batteria
def record_battery_stats():
    print("[PowDroid] Unplug your phone and press ENTER")
    input()
    run_adb_command(["shell", "dumpsys", "battery", "set", "ac", "0"])
    start_time = datetime.now().timestamp() * 1000

    print("[PowDroid] Recording...")

    print("[PowDroid] Press ENTER once you finished your test session")
    input()
    run_adb_command(["shell", "dumpsys", "battery", "set", "ac", "1"])
    stop_time = datetime.now().timestamp() * 1000

    print("[PowDroid] Test session finished!")

    return start_time, stop_time


# Dump dei dati della batteria
def dump_battery_stats():
    run_adb_command(["shell", "dumpsys", "batteryinfo", "--checkin"])
    run_adb_command(["bugreport", "collect-battery-stats", "/sdcard/batteryinfo.txt"])
    run_adb_command(["pull", "/sdcard/batteryinfo.txt", "batteryinfo.txt"])
    run_adb_command(["shell", "rm", "/sdcard/batteryinfo.txt"])


# Conversione in formato CSV
def convert_to_csv(input_file, output_file):
    with open(input_file, "r") as infile, open(output_file, "w", newline="") as outfile:
        csv_writer = csv.writer(outfile)
        csv_writer.writerow(["Time (ms)", "Battery Level (%)"])

        in_data = False
        for line in infile:
            if in_data:
                data = line.strip().split(",")
                if len(data) == 2:
                    timestamp, battery_level = data
                    csv_writer.writerow([timestamp, battery_level])
            elif line.startswith("Battery History"):
                in_data = True


# Funzione principale
def main():
    # Connessione all'emulatore
    connect_to_emulator(emulator_ip)

    print("[PowDroid] ** Please, plug the phone to the USB port **")
    print("[PowDroid] Press ENTER when you are ready to record your test session")
    input()

    clear_battery_stats()
    start_time, stop_time = record_battery_stats()
    dump_battery_stats()

    input_file = "batteryinfo.txt"
    output_file = "batteryinfo.csv"

    convert_to_csv(input_file, output_file)

    print("[PowDroid] Power readings recorded and saved in", output_file)


if __name__ == "__main__":
    main()
