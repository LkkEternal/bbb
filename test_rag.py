import urllib.request, json, sys

# 1. Login
login = json.dumps({"phone":"13800138000","password":"password123"}).encode()
req = urllib.request.Request("http://localhost:8080/api/auth/login/password", data=login,
    headers={"Content-Type":"application/json"})
resp = json.loads(urllib.request.urlopen(req).read())
token = resp["data"]["accessToken"]
print("登录成功 ✅", file=sys.stderr)

# 2. RAG Q&A
qa = json.dumps({"question":"柑橘溃疡病","model":"qwen-local"}).encode()
req2 = urllib.request.Request("http://localhost:8080/api/knowledge/qa", data=qa,
    headers={"Content-Type":"application/json", "Authorization":"Bearer "+token})
try:
    resp2 = json.loads(urllib.request.urlopen(req2).read())
    print("回答:", json.dumps(resp2, ensure_ascii=False, indent=2)[:1500])
except urllib.error.HTTPError as e:
    print("错误:", e.code, e.read().decode())
