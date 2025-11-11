import base64
from pathlib import Path
from cryptography.hazmat.primitives.ciphers.aead import AESGCM

k = base64.b64decode("av5ErNYAvza1A7Nw1DI21Q==")

def decrypt_file(fp):
    f = Path(fp)
    enc_data = f.read_bytes()
    n = enc_data[:12]
    ct = enc_data[12:]
    c = AESGCM(k)
    dec_data = c.decrypt(n, ct, None)
    f.write_bytes(dec_data)

def main():
    import sys
    decrypt_file(sys.argv[1])

if __name__ == "__main__":
    main()
