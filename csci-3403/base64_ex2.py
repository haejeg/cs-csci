import base64

def decode_xor_base64(encoded_data, key):
    # Step 1: Base64 Decode
    # The result is a bytes object
    ciphertext_bytes = base64.b64decode(encoded_data)

    # Step 2: Decode the key from Base64 (assuming key is also Base64 encoded)
    key_bytes = base64.b64decode(key)
    key_len = len(key_bytes)
    
    # Step 3: XOR Decrypt
    decrypted_bytes = []

    for i in range(len(ciphertext_bytes)):
        # XOR each byte of the ciphertext with the corresponding byte of the key
        # key_bytes[i % key_len] ensures the key repeats if shorter than the data
        decrypted_byte = ciphertext_bytes[i] ^ key_bytes[i % key_len]
        decrypted_bytes.append(decrypted_byte)

    # Convert the list of decrypted bytes back into a readable string (assuming UTF-8)
    try:
        return bytes(decrypted_bytes).decode('utf-8')
    except UnicodeDecodeError:
        # If UTF-8 decoding fails, return the hex representation
        return bytes(decrypted_bytes).hex()

# Example usage (replace with your actual data and key)
encoded_string = "03di+WGvyPfFJWZMEtf6Gul7Z7Bor8K23W1mHxLP6Bq7bH28JafZo9kicQUFx/0MpDhBsWC/jKXQI3ADHNn5Df45"
secret_key = "mxgV2QXGrNexTQNsca6Yfw==" # Replace with your actual key

decoded_result = decode_xor_base64(encoded_string, secret_key)
print(decoded_result)