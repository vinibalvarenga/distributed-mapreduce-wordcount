import getpass
import os
import paramiko
from paramiko import SSHClient
from scp import SCPClient

# Prompt for the login
login = input("Enter login: ")

# Prompt for the password securely
password = getpass.getpass("Enter password for {}: ".format(login))

node_project_name = "node"
master_project_name = "master"
file_suffix = "-1-jar-with-dependencies.jar"
remote_folder = "/tmp/{}/".format(login)

# List of computers
computers = ["tp-m5-00", "tp-m5-01", "tp-m5-02", "tp-m5-03",
             "tp-m5-04", "tp-m5-05", "tp-m5-06", "tp-m5-08",
             "tp-m5-09"]
master = "tp-m5-10"

# Clear the terminal
os.system("clear")

allComputers = computers + [master]

for c in allComputers:
    try:
        ssh = SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh.connect(c, username=login, password=password)

        ssh.exec_command("pkill -u {}".format(login))

        ssh.connect(c, username=login, password=password)
        # Remove and recreate the remote folder
        ssh.exec_command("rm -rf {}".format(remote_folder))
        ssh.exec_command("mkdir -p {}".format(remote_folder))

        project_name = master_project_name if c == master else node_project_name

        # Copy the jar file to the remote folder
        with SCPClient(ssh.get_transport()) as scp:
            scp.put("./{}/target/{}{}".format(project_name, project_name, file_suffix),
                    "{}{}{}".format(remote_folder, project_name, file_suffix))

        if c != master:
            print("Successfully deployed on {}".format(c))
            ssh.exec_command(
                "cd {}; java -jar {}{}".format(remote_folder, project_name, file_suffix))

        else:
            print("Deploying master")
            # if is master add a small delay to ensure that all nodes are ready
            ssh.exec_command("sleep 1")

            # Attach the terminal to the process and print all output until java process ends
            for i in range(0, len(computers)):
                stdin, stdout, stderr = ssh.exec_command(
                    "cd {}; java -jar {}{} {}".format(
                        remote_folder, project_name, file_suffix, ",".join(computers[:(i + 1)])))
                for line in stdout:
                    print(line, end="")
                for line in stderr:
                    print(line, end="")

            # Save results.csv file to disk and delete old results.csv
            metrics_file_name = "results.csv"
            with SCPClient(ssh.get_transport()) as scp:
                scp.get("{}{}".format(remote_folder, metrics_file_name), "./")
            ssh.exec_command("rm -rf {}".format(remote_folder))

    except paramiko.AuthenticationException:
        print("Authentication failed for {}".format(c))
    except paramiko.SSHException as ssh_exception:
        print("SSH connection failed for {}: {}".format(c, str(ssh_exception)))

    finally:
        ssh.close()
