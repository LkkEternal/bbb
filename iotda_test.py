"""IoTDA command test - try multiple formats"""
import json
import requests

endpoint = "bf593413cf.st1.iotda-app.cn-south-1.myhuaweicloud.com"
project_id = "019e966a75dd7184ab7e7e69f2ad38a5"
access_key = "HPUA9WPRCR06ZD8MZHYB"
secret_key = "nirRWipIYIWHHmy7oBjjOSKRtEF0oqL2YfgMmN5F"
device_id = "6a2e6c41c9429d337f50a67c_Equipment01"

# Step 1: Get IAM Token
print("=== Step 1: Get IAM Token ===")
resp = requests.post(
    "https://iam.cn-south-1.myhuaweicloud.com/v3/auth/tokens?nocatalog=true",
    json={
        "auth": {
            "identity": {
                "methods": ["hw_ak_sk"],
                "hw_ak_sk": {
                    "access": {"key": access_key},
                    "secret": {"key": secret_key}
                }
            },
            "scope": {
                "project": {"name": "cn-south-1"}
            }
        }
    },
    timeout=10
)
token = resp.headers.get("X-Subject-Token", "")
print(f"Token: {token[:40]}...")
print(f"Status: {resp.status_code}")

# Also get device info first
print("\n=== Device Info ===")
url = f"https://{endpoint}/v5/iot/{project_id}/devices/{device_id}"
resp = requests.get(url, headers={"X-Auth-Token": token}, timeout=10)
print(f"Status: {resp.status_code}")
print(json.dumps(resp.json(), indent=2)[:1000])

# Try async command endpoint (more commonly supported)
print("\n=== Async Command ===")
url = f"https://{endpoint}/v5/iot/{project_id}/devices/{device_id}/async-commands"
body = {
    "service_id": "DiseaseMonitor",
    "command_name": "firmware_upgrade",
    "paras": {
        "url": "https://orange-86ee.obs.cn-south-1.myhuaweicloud.com/ota/test_01.bin",
        "version": "v1.0.0",
        "file_size": 36668
    },
    "expire_time": 3600,
    "send_strategy": "immediately"
}
print(f"Body: {json.dumps(body, indent=2)}")
resp = requests.post(url, headers={"Content-Type": "application/json", "X-Auth-Token": token}, json=body, timeout=15)
print(f"Status: {resp.status_code}")
print(f"Response: {resp.text[:500]}")
