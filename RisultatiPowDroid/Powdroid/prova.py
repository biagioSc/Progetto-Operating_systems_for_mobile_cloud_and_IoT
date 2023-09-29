import subprocess
import os
import sys

def list_installed_packages():
    result = subprocess.run(['adb', 'shell', 'pm', 'list', 'packages'], capture_output=True, text=True)
    return result.stdout.splitlines()

def is_package_installed(package_name):
    packages = list_installed_packages()
    for pkg in packages:
        if package_name in pkg:
            return True
    return False

def install_app(apk_path):
    subprocess.run(['adb', 'install', '-r', apk_path])

def run_test(test_name):
    subprocess.run(["adb", "shell", "am", "instrument", "-w", "-e", "class", test_name, "com.example.robotinteractionOttimizzata.test/androidx.test.runner.AndroidJUnitRunner"])


def build_apks():
    # Ottieni il percorso dello script corrente
    current_script_path = os.path.dirname(os.path.abspath(__file__))

    # Calcola il percorso del progetto in base al percorso dello script e normalizzalo
    project_path = os.path.normpath(os.path.join(current_script_path, "..", "Codice_RobotDrinkOttimizzata"))

    # Determina il sistema operativo e scegli il comando appropriato
    if sys.platform == "win32":
        gradle_command = "gradlew.bat"
    else:
        gradle_command = "./gradlew"

    # Usa il percorso completo al comando gradle e imposta la directory corrente con `cwd`
    subprocess.run([os.path.join(project_path, gradle_command), "assembleDebug", "assembleDebugAndroidTest"], cwd=project_path)


def main():
    package_name = "com.example.robotinteractionOttimizzata"
    project_path = os.path.join(os.getcwd(), "..", "Codice_RobotDrinkOttimizzata")
    apk_path_app = os.path.join(project_path, "app", "build", "outputs", "apk", "debug", "app-debug.apk")
    apk_path_test = os.path.join(project_path, "app", "build", "outputs", "apk", "androidTest", "debug", "app-debug-androidTest.apk")
    test_name = "com.example.robotinteractionOttimizzata.testScenario2Opt"

    # Costruisci gli APK
    build_apks()

    # Installa l'APK dell'app e l'APK di test se non sono già installati
    if not is_package_installed(package_name):
        install_app(apk_path_app)
        install_app(apk_path_test)

    # Esegui i test
    run_test(test_name)

if __name__ == "__main__":
    main()
