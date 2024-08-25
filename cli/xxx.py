
# import os
# print(os.stat("./.env"))
# print(os.getuid(), os.getgid())


import hmac
import json
import hashlib
import requests
from requests.structures import CaseInsensitiveDict

def generate_signature(secret, payload_body):
    """
    Generate the HMAC SHA-256 signature for the given payload and secret.
    """
    # Create HMAC object with the secret and payload
    hmac_object = hmac.new(secret.encode(), payload_body.encode(), hashlib.sha256)
    # Return the hexadecimal digest of the HMAC
    return 'sha256=' + hmac_object.hexdigest()
 
secret = "SOME_WEBHOOK_SECRET"
payload = {
    "hello": "world"
}
payload = json.dumps(payload)
print(payload)
print(generate_signature( secret,payload))